package com.ikunkk02afk.hookandreel.mixin;

import com.ikunkk02afk.hookandreel.enchantment.LuckyCatchLogic;
import com.ikunkk02afk.hookandreel.grapple.GrapplePullController;
import com.ikunkk02afk.hookandreel.grapple.GrappleBlockController;
import com.ikunkk02afk.hookandreel.grapple.GrappleTargetType;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements GrapplingBobberAccess {
	@Unique
	private static final String GRAPPLE_NBT_KEY = "HookAndReelGrapple";

	@Unique
	private ItemStack hookAndReel$retrievalRod = ItemStack.EMPTY;
	@Unique
	private boolean hookAndReel$grapple;
	@Unique
	private double hookAndReel$maximumRange;
	@Unique
	private ItemStack hookAndReel$launchRod = ItemStack.EMPTY;
	@Unique
	private InteractionHand hookAndReel$launchHand = InteractionHand.MAIN_HAND;
	@Unique
	private long hookAndReel$pullStartGameTime = -1L;
	@Unique
	private Vec3 hookAndReel$lastTargetPosition;
	@Unique
	private GrappleTargetType hookAndReel$targetType = GrappleTargetType.NONE;

	@Shadow
	private void setHookedEntity(@Nullable Entity entity) {
		throw new AssertionError();
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void hookAndReel$tickGrapple(CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		GrapplePullController.tick(hook);
		if (hook.isRemoved()) {
			ci.cancel();
		}
	}

	@Inject(method = "onHitEntity", at = @At("RETURN"))
	private void hookAndReel$beginEntityPull(EntityHitResult hitResult, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (hookAndReel$grapple && !hook.level().isClientSide) {
			GrapplePullController.beginPull(hook, hitResult.getEntity());
		}
	}

	@Inject(method = "onHitBlock", at = @At("RETURN"))
	private void hookAndReel$beginBlockPull(BlockHitResult hitResult, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (hookAndReel$grapple && !hook.level().isClientSide) {
			GrappleBlockController.tryBegin(hook, hitResult);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void hookAndReel$recoverBlockOnRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (!hook.level().isClientSide) {
			GrappleBlockController.onHookRemoved(hook, reason);
		}
	}

	@Inject(method = "shouldStopFishing", at = @At("HEAD"), cancellable = true)
	private void hookAndReel$useGrappleRange(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (!hookAndReel$grapple) {
			return;
		}
		FishingHook hook = (FishingHook) (Object) this;
		boolean holdsFishingRod = player.getMainHandItem().is(Items.FISHING_ROD)
			|| player.getOffhandItem().is(Items.FISHING_ROD);
		double allowedDistance = Math.max(1.0D, hookAndReel$maximumRange + 4.0D);
		boolean invalid = player.isRemoved()
			|| !player.isAlive()
			|| !holdsFishingRod
			|| hook.distanceToSqr(player) > allowedDistance * allowedDistance;
		if (invalid) {
			GrapplePullController.lifecycleAbort(hook);
		}
		cir.setReturnValue(invalid);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void hookAndReel$markSavedGrapple(CompoundTag tag, CallbackInfo ci) {
		if (hookAndReel$grapple) {
			tag.putBoolean(GRAPPLE_NBT_KEY, true);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void hookAndReel$markOrphanedGrapple(CompoundTag tag, CallbackInfo ci) {
		if (tag.getBoolean(GRAPPLE_NBT_KEY)) {
			hookAndReel$grapple = true;
		}
	}

	@Inject(method = "retrieve", at = @At("HEAD"))
	private void hookAndReel$captureRetrievalRod(
		ItemStack rod,
		CallbackInfoReturnable<Integer> cir
	) {
		hookAndReel$retrievalRod = rod;
	}

	@ModifyArg(
		method = "retrieve",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withLuck(F)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;"
		),
		index = 0
	)
	private float hookAndReel$addLuckyCatchLuck(float vanillaLuck) {
		FishingHook hook = (FishingHook) (Object) this;
		return LuckyCatchLogic.addFishingLuck(
			vanillaLuck,
			hookAndReel$retrievalRod,
			(ServerLevel) hook.level()
		);
	}

	@Inject(method = "retrieve", at = @At("RETURN"))
	private void hookAndReel$clearRetrievalRod(
		ItemStack rod,
		CallbackInfoReturnable<Integer> cir
	) {
		hookAndReel$retrievalRod = ItemStack.EMPTY;
	}

	@Override
	public void hookAndReel$initializeGrapple(ItemStack rod, InteractionHand hand, double maximumRange) {
		hookAndReel$grapple = true;
		hookAndReel$launchRod = rod;
		hookAndReel$launchHand = hand;
		hookAndReel$maximumRange = maximumRange;
	}

	@Override
	public boolean hookAndReel$isGrapple() {
		return hookAndReel$grapple;
	}

	@Override
	public double hookAndReel$getMaximumRange() {
		return hookAndReel$maximumRange;
	}

	@Override
	public ItemStack hookAndReel$getLaunchRod() {
		return hookAndReel$launchRod;
	}

	@Override
	public InteractionHand hookAndReel$getLaunchHand() {
		return hookAndReel$launchHand;
	}

	@Override
	public long hookAndReel$getPullStartGameTime() {
		return hookAndReel$pullStartGameTime;
	}

	@Override
	public void hookAndReel$startPull(long gameTime, Vec3 targetPosition) {
		hookAndReel$pullStartGameTime = gameTime;
		hookAndReel$lastTargetPosition = targetPosition;
	}

	@Override
	public void hookAndReel$attachTarget(Entity target, GrappleTargetType targetType) {
		setHookedEntity(target);
		hookAndReel$targetType = targetType;
	}

	@Override
	public GrappleTargetType hookAndReel$getTargetType() {
		return hookAndReel$targetType;
	}

	@Override
	@Nullable
	public Vec3 hookAndReel$getLastTargetPosition() {
		return hookAndReel$lastTargetPosition;
	}

	@Override
	public void hookAndReel$setLastTargetPosition(Vec3 position) {
		hookAndReel$lastTargetPosition = position;
	}

	@Override
	public boolean hookAndReel$isPulling(Entity target) {
		return hookAndReel$grapple
			&& hookAndReel$targetType != GrappleTargetType.NONE
			&& hookAndReel$pullStartGameTime >= 0L
			&& target != null;
	}
}
