package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.component.GrappleModeComponent;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.grapple.ClimbableSurfaceDetector.SurfaceContact;
import com.ikunkk02afk.hookandreel.network.WallClingStatePayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class WallClingController {
	public static final int RELEASE_CAPTURE_TICKS = 6;
	private static final int INPUT_TIMEOUT_TICKS = 10;
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	private static final Map<UUID, PendingCapture> PENDING_CAPTURES = new HashMap<>();

	private WallClingController() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(WallClingController::tick);
		ServerPlayerEvents.LEAVE.register(WallClingController::clearWithoutCooldown);
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			clearWithoutCooldown(oldPlayer);
			clearWithoutCooldown(newPlayer);
		});
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
			(player, origin, destination) -> clearWithoutCooldown(player)
		);
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer player) {
				clearWithoutCooldown(player);
			}
		});
	}

	public static boolean start(
		ServerPlayer player,
		ItemStack rod,
		InteractionHand hand,
		SurfaceContact contact
	) {
		HookReelConfig config = HookReelConfigManager.get();
		int durationTicks = GrappleEnchantmentLogic.secondsToTicks(config.wallClingDurationSeconds);
		if (durationTicks <= 0 || rod.isEmpty() || !isRodValid(player, rod, hand, config)) {
			clearWithoutCooldown(player);
			return false;
		}
		long endGameTime = player.level().getGameTime() + durationTicks;
		SESSIONS.put(player.getUUID(), new Session(rod, hand, endGameTime, contact));
		PENDING_CAPTURES.remove(player.getUUID());
		player.resetFallDistance();
		sendState(player, true, endGameTime);
		return true;
	}

	public static void armReleaseCapture(ServerPlayer player, ItemStack rod, InteractionHand hand) {
		if (isActive(player) || rod.isEmpty()) {
			return;
		}
		long endGameTime = player.level().getGameTime() + RELEASE_CAPTURE_TICKS;
		PENDING_CAPTURES.put(player.getUUID(), new PendingCapture(rod, hand, endGameTime));
	}

	public static void onSuccessfulAnchor(ServerPlayer player) {
		clear(player);
	}

	public static void clear(ServerPlayer player) {
		clear(player, true);
	}

	private static void clearWithoutCooldown(ServerPlayer player) {
		clear(player, false);
	}

	private static void clear(ServerPlayer player, boolean applyCooldown) {
		PENDING_CAPTURES.remove(player.getUUID());
		Session session = SESSIONS.remove(player.getUUID());
		if (session != null) {
			if (applyCooldown && !session.rod.isEmpty()) {
				int ticks = HookAbilityCooldownManager.secondsToTicks(
					HookReelConfigManager.get().anchorHookCooldownSeconds
				);
				HookAbilityCooldownManager.set(
					session.rod,
					player.level(),
					HookAbilityCooldown.ANCHOR,
					ticks
				);
			}
			sendState(player, false, 0L);
		}
	}

	public static boolean isActive(ServerPlayer player) {
		return SESSIONS.containsKey(player.getUUID());
	}

	public static long remainingTicks(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		return session == null ? 0L : Math.max(0L, session.endGameTime - player.level().getGameTime());
	}

	public static void updateInput(ServerPlayer player, float leftImpulse, float forwardImpulse) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) {
			return;
		}
		session.leftImpulse = Math.clamp(leftImpulse, -1.0F, 1.0F);
		session.forwardImpulse = Math.clamp(forwardImpulse, -1.0F, 1.0F);
		session.lastInputGameTime = player.level().getGameTime();
	}

	public static boolean wallJump(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) {
			return false;
		}
		HookReelConfig config = HookReelConfigManager.get();
		Optional<SurfaceContact> current = ClimbableSurfaceDetector.findNearest(
			player.serverLevel(),
			player,
			config.wallDetectionDistance
		);
		if (current.isEmpty()) {
			clear(player);
			return false;
		}
		Vec3 velocity = WallClingMath.wallJumpVelocity(
			player.getDeltaMovement(),
			current.get().outwardNormal(),
			config
		);
		clear(player);
		player.setDeltaMovement(velocity);
		player.hasImpulse = true;
		player.hurtMarked = true;
		player.connection.send(new ClientboundSetEntityMotionPacket(player));
		return true;
	}

	public static void tickPlayer(ServerPlayer player) {
		tickPendingCapture(player);
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) {
			return;
		}
		HookReelConfig config = HookReelConfigManager.get();
		if (!isSessionValid(player, session, config)) {
			clear(player);
			return;
		}
		if (!ClimbableSurfaceDetector.isCollisionSurfaceValid(player.serverLevel(), player, session.contact.blockPos())) {
			clear(player);
			return;
		}
		Optional<SurfaceContact> detected = ClimbableSurfaceDetector.findNearest(
			player.serverLevel(),
			player,
			config.wallDetectionDistance
		);
		if (detected.isEmpty()) {
			clear(player);
			return;
		}
		session.contact = detected.get();

		long gameTime = player.level().getGameTime();
		boolean freshInput = session.lastInputGameTime != Long.MIN_VALUE
			&& gameTime - session.lastInputGameTime <= INPUT_TIMEOUT_TICKS;
		float left = freshInput ? session.leftImpulse : 0.0F;
		float forward = freshInput ? session.forwardImpulse : 0.0F;
		Vec3 original = player.getDeltaMovement();
		Vec3 next = WallClingMath.clingVelocity(
			original,
			session.contact.outwardNormal(),
			session.contact.distance(),
			left,
			forward,
			player.getYRot(),
			player.isShiftKeyDown(),
			config
		);
		player.setDeltaMovement(next);
		player.resetFallDistance();
		if (!next.equals(original)) {
			player.hasImpulse = true;
			player.hurtMarked = true;
			player.connection.send(new ClientboundSetEntityMotionPacket(player));
		}
	}

	private static void tickPendingCapture(ServerPlayer player) {
		PendingCapture pending = PENDING_CAPTURES.get(player.getUUID());
		if (pending == null || isActive(player)) {
			return;
		}
		HookReelConfig config = HookReelConfigManager.get();
		long gameTime = player.level().getGameTime();
		if (gameTime >= pending.endGameTime || !isRodValid(player, pending.rod, pending.hand, config)) {
			PENDING_CAPTURES.remove(player.getUUID());
			return;
		}
		ClimbableSurfaceDetector.findNearest(
			player.serverLevel(),
			player,
			config.wallDetectionDistance
		).ifPresent(contact -> start(player, pending.rod, pending.hand, contact));
	}

	private static void tick(MinecraftServer server) {
		for (UUID playerId : new ArrayList<>(SESSIONS.keySet())) {
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null) {
				SESSIONS.remove(playerId);
			} else {
				tickPlayer(player);
			}
		}
		for (UUID playerId : new ArrayList<>(PENDING_CAPTURES.keySet())) {
			if (SESSIONS.containsKey(playerId)) {
				continue;
			}
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null) {
				PENDING_CAPTURES.remove(playerId);
			} else {
				tickPendingCapture(player);
			}
		}
	}

	private static boolean isSessionValid(ServerPlayer player, Session session, HookReelConfig config) {
		return player.level().getGameTime() < session.endGameTime
			&& !player.isRemoved()
			&& player.isAlive()
			&& !player.isSpectator()
			&& !player.getAbilities().flying
			&& !player.isSwimming()
			&& !player.isFallFlying()
			&& !player.isPassenger()
			&& isRodValid(player, session.rod, session.hand, config);
	}

	private static boolean isRodValid(
		ServerPlayer player,
		ItemStack rod,
		InteractionHand hand,
		HookReelConfig config
	) {
		ItemStack currentRod = player.getItemInHand(hand);
		return config.anchorHookEnabled
			&& currentRod == rod
			&& !currentRod.isEmpty()
			&& AnchorHookLogic.getLevel(player.level(), currentRod) > 0
			&& GrappleModeComponent.getAndRepair(player.level(), currentRod) == GrappleMode.SWING;
	}

	private static void sendState(ServerPlayer player, boolean active, long endGameTime) {
		if (ServerPlayNetworking.canSend(player, WallClingStatePayload.TYPE)) {
			ServerPlayNetworking.send(player, new WallClingStatePayload(active, endGameTime));
		}
	}

	private static final class Session {
		private final ItemStack rod;
		private final InteractionHand hand;
		private final long endGameTime;
		private SurfaceContact contact;
		private float leftImpulse;
		private float forwardImpulse;
		private long lastInputGameTime = Long.MIN_VALUE;

		private Session(ItemStack rod, InteractionHand hand, long endGameTime, SurfaceContact contact) {
			this.rod = rod;
			this.hand = hand;
			this.endGameTime = endGameTime;
			this.contact = contact;
		}
	}

	private record PendingCapture(ItemStack rod, InteractionHand hand, long endGameTime) {
	}
}
