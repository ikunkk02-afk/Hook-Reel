package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import com.ikunkk02afk.hookandreel.tag.ModBlockTags;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class GrappleBlockController {
	private GrappleBlockController() {
	}

	public static boolean tryBegin(FishingHook hook, BlockHitResult hitResult) {
		if (!(hook.level() instanceof ServerLevel level) || !(hook.getPlayerOwner() instanceof ServerPlayer player)) {
			return false;
		}
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		HookReelConfig config = HookReelConfigManager.get();
		BlockPos pos = hitResult.getBlockPos();
		BlockState expectedState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (
			!access.hookAndReel$isGrapple()
				|| access.hookAndReel$getLaunchMode() != GrappleMode.PULL
				|| !config.blockPullingEnabled
				|| !isEligible(level, pos, expectedState, blockEntity, config)
				|| !hasBreakPermission(level, player, pos, expectedState, access.hookAndReel$getLaunchRod())
		) {
			return false;
		}

		if (!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, expectedState, blockEntity)) {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, player, pos, expectedState, blockEntity);
			return false;
		}

		PulledBlockEntity pulled = PulledBlockEntity.pending(
			level,
			expectedState,
			pos,
			level.dimension(),
			player.getUUID(),
			hook.getUUID()
		);
		if (!level.addFreshEntity(pulled)) {
			return false;
		}

		if (!level.getBlockState(pos).equals(expectedState)) {
			pulled.discard();
			return false;
		}
		if (!level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) {
			pulled.discard();
			return false;
		}

		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		pulled.markCarrying();
		access.hookAndReel$startPull(level.getGameTime(), pulled.position());
		access.hookAndReel$attachTarget(pulled, GrappleTargetType.BLOCK);
		return true;
	}

	public static boolean isEligible(
		ServerLevel level,
		BlockPos pos,
		BlockState state,
		@Nullable BlockEntity blockEntity,
		HookReelConfig config
	) {
		if (
			state.isAir()
				|| state.getBlock() instanceof LiquidBlock
				|| state.is(ModBlockTags.GRAPPLE_IMMOVABLE)
				|| state.is(ModBlockTags.GRAPPLE_MULTIBLOCK_UNSAFE)
				|| blockEntity != null
				|| state.getBlock().asItem() == Items.AIR
		) {
			return false;
		}
		float hardness = state.getDestroySpeed(level, pos);
		return hardness >= 0.0F && hardness <= config.maximumBlockHardness;
	}

	private static boolean hasBreakPermission(
		ServerLevel level,
		ServerPlayer player,
		BlockPos pos,
		BlockState state,
		ItemStack launchRod
	) {
		if (player.isSpectator() || launchRod.isEmpty() || !level.mayInteract(player, pos)) {
			return false;
		}
		if (!player.getAbilities().mayBuild && !launchRod.canBreakBlockInAdventureMode(new BlockInWorld(level, pos, false))) {
			return false;
		}
		return launchRod.getItem().canAttackBlock(state, level, pos, player);
	}

	public static void tick(PulledBlockEntity pulled) {
		if (!(pulled.level() instanceof ServerLevel level) || pulled.isRemoved()) {
			return;
		}
		if (pulled.getOwnershipState() == PulledBlockEntity.OwnershipState.PENDING) {
			pulled.discard();
			return;
		}
		if (pulled.getOwnershipState() != PulledBlockEntity.OwnershipState.CARRYING) {
			return;
		}

		ServerPlayer player = findPlayer(level, pulled.getPlayerUuid());
		FishingHook hook = findHook(level, pulled.getHookUuid());
		if (
			player == null
				|| !player.isAlive()
				|| player.isRemoved()
				|| player.level() != level
				|| hook == null
				|| hook.isRemoved()
				|| hook.getHookedIn() != pulled
		) {
			recover(pulled, EndReason.LIFECYCLE, hook, true);
			return;
		}

		HookReelConfig config = HookReelConfigManager.get();
		int elapsedTicks = pulled.incrementAndGetSurvivalTicks();
		if (elapsedTicks >= GrappleEnchantmentLogic.secondsToTicks(config.maxBlockPullDurationSeconds)) {
			recover(pulled, EndReason.TIMEOUT, hook, true);
			return;
		}

		double boxDistance = GrappleMath.boundingBoxDistance(player.getBoundingBox(), pulled.getBoundingBox());
		if (boxDistance <= config.blockPullStopDistance) {
			if (placeNearCurrentPosition(pulled, player)) {
				finishResolved(pulled, hook, player, EndReason.SUCCESS, true);
			} else {
				recover(pulled, EndReason.MANUAL_CANCEL, hook, true);
			}
			return;
		}

		Vec3 direction = player.getBoundingBox().getCenter().subtract(pulled.getBoundingBox().getCenter());
		double multiplier = config.blockPullSpeedMultiplier;
		Vec3 velocity = GrappleMath.pulledVelocity(
			pulled.getDeltaMovement(),
			direction,
			direction.length(),
			config.blockPullStopDistance,
			config.pullStrength * multiplier,
			config.maximumPullSpeed * multiplier
		);
		pulled.setDeltaMovement(velocity);
		pulled.move(MoverType.SELF, velocity);
		pulled.hasImpulse = true;
		pulled.hurtMarked = true;
	}

	public static void manualCancel(FishingHook hook, PulledBlockEntity pulled) {
		recover(pulled, EndReason.MANUAL_CANCEL, hook, true);
	}

	public static void lifecycleAbort(FishingHook hook, PulledBlockEntity pulled) {
		recover(pulled, EndReason.LIFECYCLE, hook, true);
	}

	public static void onHookRemoved(FishingHook hook, Entity.RemovalReason reason) {
		if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
			return;
		}
		if (hook.getHookedIn() instanceof PulledBlockEntity pulled) {
			recover(pulled, EndReason.LIFECYCLE, hook, false);
		}
	}

	public static void onPulledBlockRemoved(PulledBlockEntity pulled) {
		FishingHook hook = pulled.level() instanceof ServerLevel level
			? findHook(level, pulled.getHookUuid())
			: null;
		recover(pulled, EndReason.LIFECYCLE, hook, true);
	}

	private static void recover(
		PulledBlockEntity pulled,
		EndReason reason,
		@Nullable FishingHook hook,
		boolean discardHook
	) {
		if (pulled.getOwnershipState() != PulledBlockEntity.OwnershipState.CARRYING) {
			return;
		}
		ServerPlayer player = pulled.level() instanceof ServerLevel current
			? findPlayer(current, pulled.getPlayerUuid())
			: null;
		if (!restoreOriginalPosition(pulled, player)
			&& !placeNearCurrentPosition(pulled, player)
			&& !dropExactlyOneItem(pulled)) {
			return;
		}
		finishResolved(pulled, hook, player, reason, discardHook);
	}

	private static boolean restoreOriginalPosition(PulledBlockEntity pulled, @Nullable ServerPlayer player) {
		if (!(pulled.level() instanceof ServerLevel current)) {
			return false;
		}
		ServerLevel originLevel = current.getServer().getLevel(pulled.getOriginDimension());
		BlockPos origin = pulled.getOriginPos();
		return originLevel != null
			&& originLevel.isLoaded(origin)
			&& originLevel.getBlockState(origin).isAir()
			&& canPlace(originLevel, origin, pulled.getBlockState(), pulled)
			&& placeAt(originLevel, origin, pulled.getBlockState(), player);
	}

	private static boolean placeNearCurrentPosition(PulledBlockEntity pulled, @Nullable ServerPlayer player) {
		if (!(pulled.level() instanceof ServerLevel level)) {
			return false;
		}
		BlockState state = pulled.getBlockState();
		BlockPos position = BlockPlacementSearch.find(
			pulled.blockPosition(),
			candidate -> canPlace(level, candidate, state, pulled)
		);
		return position != null && placeAt(level, position, state, player);
	}

	private static boolean canPlace(ServerLevel level, BlockPos pos, BlockState state, PulledBlockEntity pulled) {
		if (
			!level.isLoaded(pos)
				|| level.isOutsideBuildHeight(pos)
				|| !level.getWorldBorder().isWithinBounds(pos)
				|| !level.getBlockState(pos).canBeReplaced()
				|| !state.canSurvive(level, pos)
		) {
			return false;
		}
		return level.noCollision(pulled, new AABB(pos));
	}

	private static boolean placeAt(
		ServerLevel level,
		BlockPos pos,
		BlockState state,
		@Nullable ServerPlayer player
	) {
		if (!level.setBlock(pos, state, Block.UPDATE_ALL) || !level.getBlockState(pos).equals(state)) {
			return false;
		}
		level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
		return true;
	}

	private static boolean dropExactlyOneItem(PulledBlockEntity pulled) {
		if (!(pulled.level() instanceof ServerLevel level)) {
			return false;
		}
		ItemStack item = new ItemStack(pulled.getBlockState().getBlock().asItem(), 1);
		if (item.isEmpty()) {
			return false;
		}
		ItemEntity dropped = new ItemEntity(level, pulled.getX(), pulled.getY(), pulled.getZ(), item);
		dropped.setDefaultPickUpDelay();
		return level.addFreshEntity(dropped);
	}

	private static void finishResolved(
		PulledBlockEntity pulled,
		@Nullable FishingHook hook,
		@Nullable ServerPlayer player,
		EndReason reason,
		boolean discardHook
	) {
		pulled.markResolved();
		if (hook != null && !hook.isRemoved()) {
			applyConsequences(hook, player, reason);
		}
		pulled.discard();
		if (discardHook && hook != null && !hook.isRemoved()) {
			hook.discard();
		}
	}

	private static void applyConsequences(FishingHook hook, @Nullable ServerPlayer player, EndReason reason) {
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		ItemStack rod = access.hookAndReel$getLaunchRod();
		HookReelConfig config = HookReelConfigManager.get();
		if (!rod.isEmpty()) {
			int cooldownTicks = switch (reason) {
				case SUCCESS, TIMEOUT -> HookAbilityCooldownManager.secondsToTicks(config.grapplingHookCooldownSeconds);
				case MANUAL_CANCEL -> config.grapplingHookCooldownSeconds > 0.0D
					? HookAbilityCooldownManager.PULL_CANCEL_COOLDOWN_TICKS
					: 0;
				case LIFECYCLE -> 0;
			};
			HookAbilityCooldownManager.set(rod, hook.level(), HookAbilityCooldown.PULL, cooldownTicks);
		}
		if (
			reason == EndReason.SUCCESS
				&& player != null
				&& hook.level() instanceof ServerLevel level
				&& !rod.isEmpty()
		) {
			rod.hurtAndBreak(
				config.blockPullDurabilityCost,
				level,
				player,
				item -> player.onEquippedItemBroken(item, LivingEntity.getSlotForHand(access.hookAndReel$getLaunchHand()))
			);
		}
	}

	@Nullable
	private static ServerPlayer findPlayer(ServerLevel level, @Nullable UUID uuid) {
		return uuid == null ? null : level.getServer().getPlayerList().getPlayer(uuid);
	}

	@Nullable
	private static FishingHook findHook(ServerLevel level, @Nullable UUID uuid) {
		if (uuid == null) {
			return null;
		}
		Entity entity = level.getEntity(uuid);
		return entity instanceof FishingHook hook ? hook : null;
	}

	private enum EndReason {
		SUCCESS,
		TIMEOUT,
		MANUAL_CANCEL,
		LIFECYCLE
	}
}
