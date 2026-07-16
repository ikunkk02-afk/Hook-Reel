package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.grapple.GrappleMath;
import com.ikunkk02afk.hookandreel.grapple.HookAbilityCooldownManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class FishingEntityPullController {
	private static final Map<UUID, PullSession> SESSIONS = new HashMap<>();

	private FishingEntityPullController() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(FishingEntityPullController::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
	}

	public static void start(ServerPlayer player, Mob entity, boolean large) {
		if (entity.isRemoved() || !entity.isAlive() || entity.level() != player.level()) {
			return;
		}
		if (large) {
			applyFishingCatchVelocity(player, entity, HookReelConfigManager.get());
			return;
		}
		SESSIONS.put(
			entity.getUUID(),
			new PullSession(player.serverLevel(), player.getUUID(), entity.getUUID(), player.level().getGameTime())
		);
	}

	public static boolean isPulling(Entity entity) {
		return SESSIONS.containsKey(entity.getUUID());
	}

	private static void tick(MinecraftServer server) {
		Iterator<PullSession> iterator = SESSIONS.values().iterator();
		while (iterator.hasNext()) {
			PullSession session = iterator.next();
			ServerPlayer player = server.getPlayerList().getPlayer(session.playerUuid);
			Entity target = session.level.getEntity(session.entityUuid);
			if (
				!(target instanceof Mob mob)
					|| player == null
					|| player.isRemoved()
					|| !player.isAlive()
					|| mob.isRemoved()
					|| !mob.isAlive()
					|| player.level() != session.level
					|| mob.level() != session.level
			) {
				iterator.remove();
				continue;
			}

			HookReelConfig config = HookReelConfigManager.get();
			int maximumDuration = HookAbilityCooldownManager.secondsToTicks(config.maxPullDurationSeconds);
			if (session.level.getGameTime() - session.startGameTime >= maximumDuration) {
				iterator.remove();
				continue;
			}
			double distance = GrappleMath.boundingBoxDistance(player.getBoundingBox(), mob.getBoundingBox());
			if (distance <= config.pullStopDistance) {
				iterator.remove();
				continue;
			}

			applyFishingCatchVelocity(player, mob, config);
		}
	}

	private static void applyFishingCatchVelocity(
		ServerPlayer player,
		Mob mob,
		HookReelConfig config
	) {
		Vec3 direction = player.getBoundingBox().getCenter().subtract(mob.getBoundingBox().getCenter());
		Vec3 velocity = GrappleMath.pulledVelocity(
			mob.getDeltaMovement(),
			direction,
			direction.length(),
			config.pullStopDistance,
			config.pullStrength,
			config.maximumPullSpeed
		);
		mob.setDeltaMovement(velocity);
		mob.hasImpulse = true;
		mob.hurtMarked = true;
	}

	private record PullSession(
		ServerLevel level,
		UUID playerUuid,
		UUID entityUuid,
		long startGameTime
	) {
	}
}
