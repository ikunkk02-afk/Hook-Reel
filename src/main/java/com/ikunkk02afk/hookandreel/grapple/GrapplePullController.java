package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import com.ikunkk02afk.hookandreel.tag.ModEntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class GrapplePullController {
	private static final double MAX_TARGET_TELEPORT_PER_TICK_SQR = 16.0D * 16.0D;

	private GrapplePullController() {
	}

	public static boolean isPullable(Entity target, HookReelConfig config) {
		if (target.isRemoved() || target instanceof Player player && player.isSpectator()) {
			return false;
		}
		if (target instanceof LivingEntity living && !living.isAlive()) {
			return false;
		}
		if (!(target instanceof LivingEntity) && !(target instanceof ItemEntity)) {
			return false;
		}
		if (target instanceof Player && !config.allowPullPlayers) {
			return false;
		}
		return config.allowPullBosses || !target.getType().is(ModEntityTypeTags.GRAPPLE_PULL_BLACKLIST);
	}

	public static void beginPull(FishingHook hook, Entity target) {
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		HookReelConfig config = HookReelConfigManager.get();
		if (access.hookAndReel$getLaunchMode() != GrappleMode.PULL) {
			return;
		}
		if (!access.hookAndReel$isGrapple() || !isPullable(target, config)) {
			finish(hook, EndReason.DENIED, target);
			return;
		}
		access.hookAndReel$attachTarget(target, GrappleTargetType.classify(target));
		access.hookAndReel$startPull(hook.level().getGameTime(), target.position());
	}

	public static void tick(FishingHook hook) {
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		if (
			!access.hookAndReel$isGrapple()
				|| access.hookAndReel$getLaunchMode() != GrappleMode.PULL
				|| hook.level().isClientSide
		) {
			return;
		}
		Player owner = hook.getPlayerOwner();
		if (
			owner == null
				|| owner.isRemoved()
				|| !owner.isAlive()
				|| owner.level() != hook.level()
				|| access.hookAndReel$getLaunchRod().isEmpty()
		) {
			finish(hook, EndReason.LIFECYCLE, hook.getHookedIn());
			return;
		}
		limitRange(hook, owner, access.hookAndReel$getMaximumRange());

		Entity target = hook.getHookedIn();
		if (target == null || access.hookAndReel$getPullStartGameTime() < 0L) {
			return;
		}
		if (target instanceof PulledBlockEntity) {
			return;
		}
		HookReelConfig config = HookReelConfigManager.get();
		if (target.level() != hook.level() || !isPullable(target, config)) {
			finish(hook, EndReason.LIFECYCLE, target);
			return;
		}

		Vec3 previousPosition = access.hookAndReel$getLastTargetPosition();
		Vec3 currentPosition = target.position();
		if (previousPosition != null && previousPosition.distanceToSqr(currentPosition) > MAX_TARGET_TELEPORT_PER_TICK_SQR) {
			finish(hook, EndReason.LIFECYCLE, target);
			return;
		}
		access.hookAndReel$setLastTargetPosition(currentPosition);

		long elapsed = hook.level().getGameTime() - access.hookAndReel$getPullStartGameTime();
		int maximumDuration = GrappleEnchantmentLogic.secondsToTicks(config.maxPullDurationSeconds);
		if (elapsed >= maximumDuration) {
			finish(hook, EndReason.TIMEOUT, target);
			return;
		}

		double boxDistance = GrappleMath.boundingBoxDistance(owner.getBoundingBox(), target.getBoundingBox());
		if (boxDistance <= config.pullStopDistance) {
			finish(hook, EndReason.SUCCESS, target);
			return;
		}

		Vec3 direction = owner.getBoundingBox().getCenter().subtract(target.getBoundingBox().getCenter());
		double centerDistance = direction.length();
		double multiplier = target instanceof ItemEntity ? config.itemPullSpeedMultiplier : 1.0D;
		Vec3 velocity = GrappleMath.pulledVelocity(
			target.getDeltaMovement(),
			direction,
			centerDistance,
			config.pullStopDistance,
			config.pullStrength * multiplier,
			config.maximumPullSpeed * multiplier
		);
		target.setDeltaMovement(velocity);
		target.hasImpulse = true;
		target.hurtMarked = true;
	}

	public static void manualCancel(FishingHook hook) {
		if (hook.getHookedIn() instanceof PulledBlockEntity pulled) {
			GrappleBlockController.manualCancel(hook, pulled);
			return;
		}
		finish(hook, EndReason.MANUAL_CANCEL, hook.getHookedIn());
	}

	public static void lifecycleAbort(FishingHook hook) {
		if (hook.getHookedIn() instanceof PulledBlockEntity pulled) {
			GrappleBlockController.lifecycleAbort(hook, pulled);
			return;
		}
		finish(hook, EndReason.LIFECYCLE, hook.getHookedIn());
	}

	private static void limitRange(FishingHook hook, Player owner, double maximumRange) {
		Vec3 fromOwner = hook.position().subtract(owner.getEyePosition());
		if (fromOwner.lengthSqr() < maximumRange * maximumRange) {
			return;
		}
		Vec3 velocity = hook.getDeltaMovement();
		if (velocity.dot(fromOwner) > 0.0D) {
			hook.setDeltaMovement(0.0D, Math.min(velocity.y, 0.0D), 0.0D);
		}
	}

	private static void finish(FishingHook hook, EndReason reason, Entity target) {
		if (hook.isRemoved()) {
			return;
		}
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		ItemStack rod = access.hookAndReel$getLaunchRod();
		HookReelConfig config = HookReelConfigManager.get();
		if (!rod.isEmpty()) {
			int cooldownTicks = switch (reason) {
				case SUCCESS, TIMEOUT -> HookAbilityCooldownManager.secondsToTicks(config.grapplingHookCooldownSeconds);
				case MANUAL_CANCEL, DENIED -> config.grapplingHookCooldownSeconds > 0.0D
					? HookAbilityCooldownManager.PULL_CANCEL_COOLDOWN_TICKS
					: 0;
				case LIFECYCLE -> 0;
			};
			HookAbilityCooldownManager.set(rod, hook.level(), HookAbilityCooldown.PULL, cooldownTicks);
		}
		if (reason == EndReason.SUCCESS && hook.level() instanceof ServerLevel serverLevel && hook.getPlayerOwner() instanceof ServerPlayer player) {
			int durability = target instanceof ItemEntity ? 3 : 5;
			rod.hurtAndBreak(
				durability,
				serverLevel,
				player,
				item -> player.onEquippedItemBroken(item, LivingEntity.getSlotForHand(access.hookAndReel$getLaunchHand()))
			);
		}
		hook.discard();
	}

	private enum EndReason {
		SUCCESS,
		TIMEOUT,
		MANUAL_CANCEL,
		DENIED,
		LIFECYCLE
	}
}
