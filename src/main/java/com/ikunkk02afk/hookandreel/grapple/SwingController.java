package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.grapple.ClimbableSurfaceDetector.SurfaceContact;
import com.ikunkk02afk.hookandreel.grapple.ClimbableSurfaceDetector.MovementChannels;
import com.ikunkk02afk.hookandreel.tag.ModBlockTags;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SwingController {
	private static final double ANCHOR_SURFACE_OFFSET = 0.01D;
	private static final double ANCHOR_RANGE_TOLERANCE = 1.0D;
	private static final double STABLE_GROUND_VERTICAL_SPEED = 0.08D;
	private static final int AUTO_DETACH_GROUND_TICKS = 20;
	private static final int INPUT_TIMEOUT_TICKS = 10;
	private static final int MAXIMUM_ROPE_MESSAGE_INTERVAL_TICKS = 40;

	private SwingController() {
	}

	public static void tickFlying(FishingHook hook) {
		if (hook.level().isClientSide) {
			return;
		}
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		if (
			access.hookAndReel$getLaunchMode() != GrappleMode.SWING
				|| access.hookAndReel$getHookState() != HookState.HOOK_FLYING
		) {
			return;
		}
		Player owner = hook.getPlayerOwner();
		if (
			!HookReelConfigManager.get().anchorHookEnabled
				|| !(owner instanceof ServerPlayer player)
				|| !isOwnerValid(hook, player, access)
		) {
			lifecycleAbort(hook);
			return;
		}
		Vec3 fromOwner = hook.position().subtract(player.getEyePosition());
		double maximumRange = Math.max(1.0D, access.hookAndReel$getMaximumRange());
		if (fromOwner.lengthSqr() >= maximumRange * maximumRange) {
			Vec3 velocity = hook.getDeltaMovement();
			if (velocity.dot(fromOwner) > 0.0D) {
				hook.setDeltaMovement(0.0D, Math.min(velocity.y, 0.0D), 0.0D);
			}
		}
	}

	public static boolean tryAnchor(FishingHook hook, BlockHitResult hitResult) {
		if (hook.level().isClientSide) {
			return false;
		}
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		HookReelConfig config = HookReelConfigManager.get();
		BlockPos blockPos = hitResult.getBlockPos();
		if (
			access.hookAndReel$getLaunchMode() != GrappleMode.SWING
				|| access.hookAndReel$getHookState() != HookState.HOOK_FLYING
				|| !config.anchorHookEnabled
				|| !isValidAnchor(hook.level(), blockPos)
		) {
			return false;
		}

		Player owner = hook.getPlayerOwner();
		if (!(owner instanceof ServerPlayer player) || !isOwnerValid(hook, player, access)) {
			lifecycleAbort(hook);
			return false;
		}

		Direction direction = hitResult.getDirection();
		Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
		Vec3 anchor = hitResult.getLocation().add(normal.scale(ANCHOR_SURFACE_OFFSET));
		Vec3 attachment = player.getBoundingBox().getCenter();
		double distance = attachment.distanceTo(anchor);
		double maximumRopeLength = access.hookAndReel$getMaximumRopeLength();
		if (
			maximumRopeLength <= 0.0D
				|| distance > maximumRopeLength + ANCHOR_RANGE_TOLERANCE
				|| distance > access.hookAndReel$getMaximumRange() + ANCHOR_RANGE_TOLERANCE
		) {
			return false;
		}

		double ropeLength = distance;
		if (anchor.y <= attachment.y + 1.5D) {
			ropeLength += config.rappelInitialSlack;
		}
		ropeLength = Math.min(maximumRopeLength, ropeLength);
		access.hookAndReel$anchor(blockPos, anchor, direction, ropeLength);
		hook.setPos(anchor);
		hook.setDeltaMovement(Vec3.ZERO);
		hook.hasImpulse = true;

		ItemStack rod = access.hookAndReel$getLaunchRod();
		if (config.anchorDurabilityCost > 0 && !rod.isEmpty()) {
			rod.hurtAndBreak(
				config.anchorDurabilityCost,
				(ServerLevel) hook.level(),
				player,
				item -> player.onEquippedItemBroken(
					item,
					LivingEntity.getSlotForHand(access.hookAndReel$getLaunchHand())
				)
			);
		}
		if (rod.isEmpty()) {
			lifecycleAbort(hook);
			return false;
		}
		WallClingController.onSuccessfulAnchor(player);
		return true;
	}

	public static void handleStartReelRequest(ServerPlayer player) {
		if (WallClingController.wallJump(player)) {
			return;
		}
		FishingHook hook = player.fishing;
		if (!(hook instanceof GrapplingBobberAccess access)) {
			return;
		}
		HookReelConfig config = HookReelConfigManager.get();
		BlockPos anchorBlock = access.hookAndReel$getAnchorBlockPos();
		Vec3 anchor = access.hookAndReel$getAnchorPosition();
		Direction anchorFace = access.hookAndReel$getAnchorFace();
		if (
			!config.anchorHookEnabled
				|| access.hookAndReel$getLaunchMode() != GrappleMode.SWING
				|| access.hookAndReel$getHookState() != HookState.ANCHORED_IDLE
				|| anchorBlock == null
				|| anchor == null
				|| anchorFace == null
				|| !isValidAnchor(hook.level(), anchorBlock)
				|| !isOwnerValid(hook, player, access)
		) {
			return;
		}
		ReelTargetCalculator.ReelTarget target = ReelTargetCalculator.calculate(
			player.serverLevel(),
			player,
			anchorBlock,
			anchor,
			anchorFace,
			config
		);
		access.hookAndReel$startReeling(target.position(), hook.level().getGameTime());
	}

	public static void tickAnchored(FishingHook hook) {
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		if (!access.hookAndReel$getHookState().isAnchored()) {
			return;
		}
		if (hook.level().isClientSide) {
			hook.setDeltaMovement(Vec3.ZERO);
			return;
		}

		HookReelConfig config = HookReelConfigManager.get();
		Player owner = hook.getPlayerOwner();
		BlockPos anchorBlock = access.hookAndReel$getAnchorBlockPos();
		Vec3 anchor = access.hookAndReel$getAnchorPosition();
		if (
			!config.anchorHookEnabled
				|| !(owner instanceof ServerPlayer player)
				|| !isOwnerValid(hook, player, access)
				|| anchorBlock == null
				|| anchor == null
				|| !isValidAnchor(hook.level(), anchorBlock)
		) {
			lifecycleAbort(hook);
			return;
		}

		hook.setPos(anchor);
		hook.setDeltaMovement(Vec3.ZERO);
		hook.hasImpulse = true;
		HookState state = access.hookAndReel$getHookState();
		if (state == HookState.REELING_UP && player.isShiftKeyDown()) {
			finishReeling(access, player, anchor, config);
			state = HookState.ANCHORED_IDLE;
		} else if (state != HookState.REELING_UP) {
			if (player.isShiftKeyDown() && config.rappelEnabled) {
				access.hookAndReel$setHookState(HookState.RAPPELLING);
				state = HookState.RAPPELLING;
				payOutRope(hook, player, access, config);
			} else if (state == HookState.RAPPELLING) {
				access.hookAndReel$setHookState(HookState.ANCHORED_IDLE);
				state = HookState.ANCHORED_IDLE;
			}
		}

		player.resetFallDistance();
		if (state == HookState.REELING_UP) {
			tickReeling(hook, player, access, anchor, config);
			return;
		}

		applyRopeConstraint(hook, player, access, anchor, config);
		if (config.autoDetachOnGround && player.onGround() && Math.abs(player.getDeltaMovement().y) < STABLE_GROUND_VERTICAL_SPEED) {
			if (access.hookAndReel$incrementGroundedTicks() >= AUTO_DETACH_GROUND_TICKS) {
				detach(hook, SwingDetachReason.AUTO_GROUND);
			}
		} else {
			access.hookAndReel$resetGroundedTicks();
		}
	}

	private static void tickReeling(
		FishingHook hook,
		ServerPlayer player,
		GrapplingBobberAccess access,
		Vec3 anchor,
		HookReelConfig config
	) {
		Vec3 target = access.hookAndReel$getReelTargetPosition();
		long startTime = access.hookAndReel$getReelStartGameTime();
		if (target == null || startTime < 0L) {
			finishReeling(access, player, anchor, config);
			return;
		}
		Vec3 attachment = player.getBoundingBox().getCenter();
		Optional<SurfaceContact> surface = ClimbableSurfaceDetector.findNearest(
			player.serverLevel(),
			player,
			config.wallDetectionDistance
		);
		double distanceToTarget = attachment.distanceTo(target);
		boolean nearTarget = distanceToTarget <= Math.max(1.5D, config.reelArrivalDistance * 2.0D);
		boolean nearAnchor = attachment.distanceTo(anchor) <= Math.max(2.0D, config.reelArrivalDistance + 1.25D);
		if (surface.isPresent() && (nearTarget || nearAnchor || player.horizontalCollision)) {
			enterWallCling(hook, player, access, surface.get());
			return;
		}

		long elapsed = hook.level().getGameTime() - startTime;
		int maximumTicks = GrappleEnchantmentLogic.secondsToTicks(config.maximumReelUpDurationSeconds);
		if (elapsed >= maximumTicks) {
			finishReeling(access, player, anchor, config);
			return;
		}

		Vec3 originalVelocity = player.getDeltaMovement();
		float sidewaysInput = currentReelingSidewaysInput(hook, access);
		Vec3 strafeDirection = ReelMovementMath.strafeDirection(attachment, target, player.getYRot());
		Vec3 forwardDirection = new Vec3(target.x - attachment.x, 0.0D, target.z - attachment.z);
		MovementChannels channels = config.reelingLateralControlEnabled && Math.abs(sidewaysInput) > 1.0E-4F
			? ClimbableSurfaceDetector.probeMovementChannels(
				player.serverLevel(),
				player,
				strafeDirection,
				forwardDirection,
				config.reelingLateralDetectionDistance
			)
			: new MovementChannels(true, true, true);
		ReelMovementMath.ReelResult result = ReelMovementMath.apply(
			attachment,
			target,
			originalVelocity,
			player.horizontalCollision,
			sidewaysInput,
			player.getYRot(),
			channels.requestedSideClear(sidewaysInput),
			!channels.forwardClear(),
			config
		);
		if (result.arrived()) {
			if (surface.isPresent()) {
				enterWallCling(hook, player, access, surface.get());
			} else {
				finishReeling(access, player, anchor, config);
			}
			return;
		}
		if (!result.velocity().equals(originalVelocity)) {
			player.setDeltaMovement(result.velocity());
			player.hasImpulse = true;
			player.hurtMarked = true;
			player.connection.send(new ClientboundSetEntityMotionPacket(player));
		}
	}

	private static void enterWallCling(
		FishingHook hook,
		ServerPlayer player,
		GrapplingBobberAccess access,
		SurfaceContact surface
	) {
		if (WallClingController.start(
			player,
			access.hookAndReel$getLaunchRod(),
			access.hookAndReel$getLaunchHand(),
			surface
		)) {
			detach(hook, SwingDetachReason.WALL_CLING_CAPTURE);
		}
	}

	private static void finishReeling(
		GrapplingBobberAccess access,
		ServerPlayer player,
		Vec3 anchor,
		HookReelConfig config
	) {
		double capturedLength = player.getBoundingBox().getCenter().distanceTo(anchor) + config.ropeTolerance;
		access.hookAndReel$setRopeLength(Math.min(access.hookAndReel$getMaximumRopeLength(), capturedLength));
		access.hookAndReel$finishReeling();
		access.hookAndReel$updateSwingInput(0.0F, 0.0F, Long.MIN_VALUE);
		access.hookAndReel$setHookState(HookState.ANCHORED_IDLE);
	}

	private static float currentReelingSidewaysInput(FishingHook hook, GrapplingBobberAccess access) {
		long lastInputTime = access.hookAndReel$getLastSwingInputGameTime();
		if (lastInputTime == Long.MIN_VALUE) {
			return 0.0F;
		}
		long elapsed = hook.level().getGameTime() - lastInputTime;
		if (elapsed < 0L || elapsed > INPUT_TIMEOUT_TICKS) {
			return 0.0F;
		}
		// Minecraft's leftImpulse is positive for A and negative for D. The reel math uses D=+1.
		return Math.clamp(-access.hookAndReel$getSwingLeftImpulse(), -1.0F, 1.0F);
	}

	private static void payOutRope(
		FishingHook hook,
		ServerPlayer player,
		GrapplingBobberAccess access,
		HookReelConfig config
	) {
		double currentLength = access.hookAndReel$getRopeLength();
		double maximumLength = access.hookAndReel$getMaximumRopeLength();
		double nextLength = Math.min(
			maximumLength,
			currentLength + RopeConstraintMath.rappelIncrementPerTick(config.rappelSpeed)
		);
		access.hookAndReel$setRopeLength(nextLength);
		if (nextLength >= maximumLength - 1.0E-4D) {
			long gameTime = hook.level().getGameTime();
			if (gameTime - access.hookAndReel$getLastMaximumRopeMessageTime() >= MAXIMUM_ROPE_MESSAGE_INTERVAL_TICKS) {
				player.displayClientMessage(Component.translatable("message.hook_and_reel.rope_maximum"), true);
				access.hookAndReel$setLastMaximumRopeMessageTime(gameTime);
			}
		}
	}

	private static void applyRopeConstraint(
		FishingHook hook,
		ServerPlayer player,
		GrapplingBobberAccess access,
		Vec3 anchor,
		HookReelConfig config
	) {
		Vec3 input = Vec3.ZERO;
		long lastInputTime = access.hookAndReel$getLastSwingInputGameTime();
		if (lastInputTime != Long.MIN_VALUE && hook.level().getGameTime() - lastInputTime <= INPUT_TIMEOUT_TICKS) {
			input = RopeConstraintMath.movementInput(
				access.hookAndReel$getSwingLeftImpulse(),
				access.hookAndReel$getSwingForwardImpulse(),
				player.getYRot()
			);
		}
		Vec3 originalVelocity = player.getDeltaMovement();
		RopeConstraintMath.ConstraintResult result = RopeConstraintMath.constrain(
			player.getBoundingBox().getCenter(),
			anchor,
			originalVelocity,
			access.hookAndReel$getRopeLength(),
			input,
			config.swingControlStrength,
			config.maximumSwingSpeed,
			config.ropeConstraintStrength,
			config.ropeDamping,
			config.ropeTolerance
		);
		boolean velocityChanged = !result.velocity().equals(originalVelocity);
		if (velocityChanged) {
			player.setDeltaMovement(result.velocity());
			player.hasImpulse = true;
			player.hurtMarked = true;
		}
		if (result.positionCorrection().lengthSqr() > 1.0E-8D) {
			player.move(MoverType.SELF, result.positionCorrection());
			player.hasImpulse = true;
			player.hurtMarked = true;
			velocityChanged = true;
		}
		if (velocityChanged) {
			player.connection.send(new ClientboundSetEntityMotionPacket(player));
		}
	}

	public static void stabilize(FishingHook hook) {
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		if (!access.hookAndReel$getHookState().isAnchored()) {
			return;
		}
		Vec3 anchor = access.hookAndReel$getAnchorPosition();
		if (anchor != null) {
			hook.setPos(anchor);
		}
		hook.setDeltaMovement(Vec3.ZERO);
	}

	public static void detach(FishingHook hook, SwingDetachReason reason) {
		if (hook.isRemoved()) {
			return;
		}
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		boolean wasAnchored = access.hookAndReel$getHookState().isAnchored();
		Player owner = hook.getPlayerOwner();
		ItemStack rod = access.hookAndReel$getLaunchRod();
		if (!rod.isEmpty()) {
			HookReelConfig config = HookReelConfigManager.get();
			int cooldownTicks = config.anchorHookCooldownSeconds <= 0.0D
				? 0
				: switch (reason.cooldownPolicy()) {
					case NONE -> 0;
					case FULL -> HookAbilityCooldownManager.secondsToTicks(config.anchorHookCooldownSeconds);
					case STATE_BASED -> HookAbilityCooldownManager.secondsToTicks(
						wasAnchored ? config.anchorHookCooldownSeconds : config.anchorHookFailedCastDelaySeconds
					);
				};
			if (cooldownTicks > 0) {
				HookAbilityCooldownManager.set(rod, hook.level(), HookAbilityCooldown.ANCHOR, cooldownTicks);
			}
		}
		if (reason.armsWallCapture() && wasAnchored && owner instanceof ServerPlayer player) {
			WallClingController.armReleaseCapture(player, rod, access.hookAndReel$getLaunchHand());
		}
		access.hookAndReel$clearAnchorState();
		hook.discard();
	}

	public static void lifecycleAbort(FishingHook hook) {
		detach(hook, SwingDetachReason.LIFECYCLE_ABORT);
	}

	public static void onHookRemoved(FishingHook hook, net.minecraft.world.entity.Entity.RemovalReason reason) {
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		if (access.hookAndReel$getLaunchMode() == GrappleMode.SWING) {
			ItemStack rod = access.hookAndReel$getLaunchRod();
			Player owner = hook.getPlayerOwner();
			if (
				access.hookAndReel$getHookState() == HookState.HOOK_FLYING
					&& !rod.isEmpty()
					&& owner instanceof ServerPlayer player
					&& player.isAlive()
					&& player.level() == hook.level()
			) {
				HookReelConfig config = HookReelConfigManager.get();
				int delayTicks = config.anchorHookCooldownSeconds > 0.0D
					? HookAbilityCooldownManager.secondsToTicks(config.anchorHookFailedCastDelaySeconds)
					: 0;
				HookAbilityCooldownManager.set(rod, hook.level(), HookAbilityCooldown.ANCHOR, delayTicks);
			}
			access.hookAndReel$clearAnchorState();
		}
	}

	public static boolean isValidAnchor(net.minecraft.world.level.Level level, BlockPos pos) {
		if (
			!(level instanceof ServerLevel serverLevel)
				|| serverLevel.getChunkSource().getChunkNow(
					SectionPos.blockToSectionCoord(pos.getX()),
					SectionPos.blockToSectionCoord(pos.getZ())
				) == null
		) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		return !state.isAir()
			&& state.getFluidState().isEmpty()
			&& !(state.getBlock() instanceof LiquidBlock)
			&& !state.is(ModBlockTags.SWING_UNHOOKABLE)
			&& !state.getCollisionShape(level, pos).isEmpty();
	}

	private static boolean isOwnerValid(FishingHook hook, ServerPlayer player, GrapplingBobberAccess access) {
		UUID ownerUuid = access.hookAndReel$getLaunchOwnerUuid();
		ItemStack currentRod = player.getItemInHand(access.hookAndReel$getLaunchHand());
		return !player.isRemoved()
			&& player.isAlive()
			&& !player.isSpectator()
			&& !(player.getAbilities().flying && player.getAbilities().mayfly)
			&& player.level() == hook.level()
			&& ownerUuid != null
			&& ownerUuid.equals(player.getUUID())
			&& currentRod == access.hookAndReel$getLaunchRod()
			&& !currentRod.isEmpty()
			&& AnchorHookLogic.getLevel(hook.level(), currentRod) > 0;
	}
}
