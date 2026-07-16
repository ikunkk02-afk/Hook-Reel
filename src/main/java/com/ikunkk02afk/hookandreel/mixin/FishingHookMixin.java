package com.ikunkk02afk.hookandreel.mixin;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.enchantment.LuckyCatchLogic;
import com.ikunkk02afk.hookandreel.fishing.FishingEntityController;
import com.ikunkk02afk.hookandreel.fishing.LuckyInstantCatchController;
import com.ikunkk02afk.hookandreel.grapple.GrapplePullController;
import com.ikunkk02afk.hookandreel.grapple.GrappleBlockController;
import com.ikunkk02afk.hookandreel.grapple.GrappleTargetType;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookState;
import com.ikunkk02afk.hookandreel.grapple.SwingController;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements GrapplingBobberAccess {
	@Unique
	private static final String GRAPPLE_NBT_KEY = "HookAndReelGrapple";
	@Unique
	private static final double FISHING_HOOK_GRAVITY = -0.03D;
	@Unique
	private static final int MAX_COLLISION_PATH_CHUNKS = 16;
	@Unique
	private static final EntityDataAccessor<Integer> HOOK_AND_REEL_STATE = SynchedEntityData.defineId(
		FishingHook.class,
		EntityDataSerializers.INT
	);
	@Unique
	private static final EntityDataAccessor<Float> HOOK_AND_REEL_ROPE_LENGTH = SynchedEntityData.defineId(
		FishingHook.class,
		EntityDataSerializers.FLOAT
	);
	@Unique
	private static final EntityDataAccessor<Float> HOOK_AND_REEL_MAXIMUM_ROPE_LENGTH = SynchedEntityData.defineId(
		FishingHook.class,
		EntityDataSerializers.FLOAT
	);

	@Unique
	private ItemStack hookAndReel$retrievalRod = ItemStack.EMPTY;
	@Unique
	private ItemStack hookAndReel$fishingRod = ItemStack.EMPTY;
	@Unique
	private InteractionHand hookAndReel$fishingHand = InteractionHand.MAIN_HAND;
	@Unique
	private int hookAndReel$luckyWaterTicks;
	@Unique
	private boolean hookAndReel$luckyCatchArmed;
	@Unique
	private boolean hookAndReel$instantCatchTriggered;
	@Unique
	private boolean hookAndReel$forcedBite;
	@Unique
	private double hookAndReel$maximumRange;
	@Unique
	private ItemStack hookAndReel$launchRod = ItemStack.EMPTY;
	@Unique
	private InteractionHand hookAndReel$launchHand = InteractionHand.MAIN_HAND;
	@Unique
	private GrappleMode hookAndReel$launchMode = GrappleMode.PULL;
	@Unique
	private UUID hookAndReel$launchOwnerUuid;
	@Unique
	private int hookAndReel$anchorLevel;
	@Unique
	private BlockPos hookAndReel$anchorBlockPos;
	@Unique
	private Vec3 hookAndReel$anchorPosition;
	@Unique
	private Direction hookAndReel$anchorFace;
	@Unique
	private Vec3 hookAndReel$reelTargetPosition;
	@Unique
	private long hookAndReel$reelStartGameTime = -1L;
	@Unique
	private float hookAndReel$swingLeftImpulse;
	@Unique
	private float hookAndReel$swingForwardImpulse;
	@Unique
	private long hookAndReel$lastSwingInputGameTime = Long.MIN_VALUE;
	@Unique
	private int hookAndReel$groundedTicks;
	@Unique
	private long hookAndReel$lastMaximumRopeMessageTime = -40L;
	@Unique
	private long hookAndReel$pullStartGameTime = -1L;
	@Unique
	private Vec3 hookAndReel$lastTargetPosition;
	@Unique
	private GrappleTargetType hookAndReel$targetType = GrappleTargetType.NONE;
	@Unique
	private boolean hookAndReel$preCollisionGravityApplied;

	@Shadow
	@Final
	private static EntityDataAccessor<Boolean> DATA_BITING;

	@Shadow
	private boolean biting;

	@Shadow
	private int nibble;

	@Shadow
	private int timeUntilLured;

	@Shadow
	private int timeUntilHooked;

	@Shadow
	private void setHookedEntity(@Nullable Entity entity) {
		throw new AssertionError();
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void hookAndReel$defineHookState(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(HOOK_AND_REEL_STATE, HookState.VANILLA.ordinal());
		builder.define(HOOK_AND_REEL_ROPE_LENGTH, 0.0F);
		builder.define(HOOK_AND_REEL_MAXIMUM_ROPE_LENGTH, 0.0F);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void hookAndReel$tickGrapple(CallbackInfo ci) {
		hookAndReel$preCollisionGravityApplied = false;
		FishingHook hook = (FishingHook) (Object) this;
		LuckyInstantCatchController.preTick(hook);
		GrapplePullController.tick(hook);
		SwingController.tickFlying(hook);
		if (hook.isRemoved()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/FishingHook;catchingFish(Lnet/minecraft/core/BlockPos;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void hookAndReel$tickLuckyInstantCatch(CallbackInfo ci) {
		if (LuckyInstantCatchController.tickValidWater((FishingHook) (Object) this)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/FishingHook;checkCollision()V"
		),
		cancellable = true
	)
	private void hookAndReel$prepareSweptCollision(CallbackInfo ci) {
		if (hookAndReel$getHookState() != HookState.HOOK_FLYING) {
			return;
		}

		FishingHook hook = (FishingHook) (Object) this;
		boolean inWater = hook.level().getFluidState(hook.blockPosition()).is(FluidTags.WATER);
		Vec3 movement = inWater
			? hook.getDeltaMovement()
			: hook.getDeltaMovement().add(0.0D, FISHING_HOOK_GRAVITY, 0.0D);
		Vec3 start = hook.position();
		Vec3 end = start.add(movement);

		if (hook.level() instanceof ServerLevel level && !hookAndReel$pathChunksLoaded(level, start, end)) {
			hook.discard();
			ci.cancel();
			return;
		}

		if (!inWater) {
			hook.setDeltaMovement(movement);
			hookAndReel$preCollisionGravityApplied = true;
		}
	}

	@ModifyConstant(
		method = "tick",
		constant = @Constant(doubleValue = FISHING_HOOK_GRAVITY),
		require = 1
	)
	private double hookAndReel$skipLateGravityAfterSweptCollision(double gravity) {
		return hookAndReel$preCollisionGravityApplied ? 0.0D : gravity;
	}

	@Unique
	private static boolean hookAndReel$pathChunksLoaded(ServerLevel level, Vec3 start, Vec3 end) {
		int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(Math.min(start.x, end.x)));
		int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(Math.max(start.x, end.x)));
		int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(Math.min(start.z, end.z)));
		int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(Math.max(start.z, end.z)));
		long chunkCount = (long) (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
		if (chunkCount > MAX_COLLISION_PATH_CHUNKS) {
			return false;
		}

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
					return false;
				}
			}
		}
		return true;
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/Projectile;tick()V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void hookAndReel$tickAnchored(CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (hookAndReel$getHookState().isAnchored()) {
			SwingController.tickAnchored(hook);
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void hookAndReel$stabilizeAnchorAfterHit(CallbackInfo ci) {
		SwingController.stabilize((FishingHook) (Object) this);
	}

	@Inject(method = "onHitEntity", at = @At("RETURN"))
	private void hookAndReel$beginEntityPull(EntityHitResult hitResult, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (hookAndReel$isGrapple() && hookAndReel$launchMode == GrappleMode.PULL && !hook.level().isClientSide) {
			GrapplePullController.beginPull(hook, hitResult.getEntity());
		}
	}

	@Inject(method = "onHitBlock", at = @At("RETURN"))
	private void hookAndReel$beginBlockPull(BlockHitResult hitResult, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (!hookAndReel$isGrapple() || hook.level().isClientSide) {
			return;
		}
		if (hookAndReel$launchMode == GrappleMode.PULL) {
			GrappleBlockController.tryBegin(hook, hitResult);
		} else {
			SwingController.tryAnchor(hook, hitResult);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void hookAndReel$recoverBlockOnRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (!hook.level().isClientSide) {
			GrappleBlockController.onHookRemoved(hook, reason);
			SwingController.onHookRemoved(hook, reason);
		}
	}

	@Inject(method = "shouldStopFishing", at = @At("HEAD"), cancellable = true)
	private void hookAndReel$useGrappleRange(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (!hookAndReel$isGrapple()) {
			return;
		}
		FishingHook hook = (FishingHook) (Object) this;
		boolean holdsFishingRod = player.getMainHandItem().is(Items.FISHING_ROD)
			|| player.getOffhandItem().is(Items.FISHING_ROD);
		if (hook.level().isClientSide) {
			cir.setReturnValue(!holdsFishingRod);
			return;
		}
		boolean holdsLaunchRod = player.getItemInHand(hookAndReel$launchHand) == hookAndReel$launchRod;
		double activeRange = hookAndReel$getHookState().isAnchored()
			? hookAndReel$getMaximumRopeLength()
			: hookAndReel$maximumRange;
		double allowedDistance = Math.max(1.0D, activeRange + 4.0D);
		boolean invalid = player.isRemoved()
			|| !player.isAlive()
			|| player.isSpectator()
			|| !holdsLaunchRod
			|| hook.distanceToSqr(player) > allowedDistance * allowedDistance;
		if (invalid) {
			if (hookAndReel$launchMode == GrappleMode.SWING) {
				SwingController.lifecycleAbort(hook);
			} else {
				GrapplePullController.lifecycleAbort(hook);
			}
		}
		cir.setReturnValue(invalid);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void hookAndReel$markSavedGrapple(CompoundTag tag, CallbackInfo ci) {
		if (hookAndReel$isGrapple()) {
			tag.putBoolean(GRAPPLE_NBT_KEY, true);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void hookAndReel$markOrphanedGrapple(CompoundTag tag, CallbackInfo ci) {
		if (tag.getBoolean(GRAPPLE_NBT_KEY)) {
			hookAndReel$setHookState(HookState.HOOK_FLYING);
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

	@Inject(
		method = "retrieve",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
		),
		cancellable = true
	)
	private void hookAndReel$replaceFishingLootWithEntity(
		ItemStack rod,
		CallbackInfoReturnable<Integer> cir
	) {
		if (FishingEntityController.tryCompleteEntityCatch((FishingHook) (Object) this, rod)) {
			cir.setReturnValue(1);
		}
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
		hookAndReel$initializeHook(rod, hand, GrappleMode.PULL, maximumRange, maximumRange, 0);
	}

	@Override
	public void hookAndReel$initializeHook(
		ItemStack rod,
		InteractionHand hand,
		GrappleMode mode,
		double maximumRange,
		double maximumRopeLength,
		int anchorLevel
	) {
		hookAndReel$launchRod = rod;
		hookAndReel$launchHand = hand;
		hookAndReel$launchMode = mode;
		FishingHook hook = (FishingHook) (Object) this;
		hookAndReel$launchOwnerUuid = hook.getPlayerOwner() == null ? null : hook.getPlayerOwner().getUUID();
		hookAndReel$maximumRange = maximumRange;
		hookAndReel$anchorLevel = Math.max(0, anchorLevel);
		hook.getEntityData().set(HOOK_AND_REEL_MAXIMUM_ROPE_LENGTH, (float) Math.max(0.0D, maximumRopeLength));
		hookAndReel$setHookState(HookState.HOOK_FLYING);
	}

	@Override
	public boolean hookAndReel$isGrapple() {
		return hookAndReel$getHookState() != HookState.VANILLA;
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
	public GrappleMode hookAndReel$getLaunchMode() {
		return hookAndReel$launchMode;
	}

	@Override
	@Nullable
	public UUID hookAndReel$getLaunchOwnerUuid() {
		return hookAndReel$launchOwnerUuid;
	}

	@Override
	public HookState hookAndReel$getHookState() {
		FishingHook hook = (FishingHook) (Object) this;
		return HookState.byId(hook.getEntityData().get(HOOK_AND_REEL_STATE));
	}

	@Override
	public void hookAndReel$setHookState(HookState state) {
		FishingHook hook = (FishingHook) (Object) this;
		hook.getEntityData().set(HOOK_AND_REEL_STATE, state.ordinal());
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
		hookAndReel$setHookState(switch (targetType) {
			case ENTITY -> HookState.PULLING_ENTITY;
			case ITEM -> HookState.PULLING_ITEM;
			case BLOCK -> HookState.PULLING_BLOCK;
			case NONE -> HookState.HOOK_FLYING;
		});
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
		return hookAndReel$getHookState().isPulling()
			&& hookAndReel$pullStartGameTime >= 0L
			&& target != null;
	}

	@Override
	public void hookAndReel$anchor(
		BlockPos blockPos,
		Vec3 anchorPosition,
		Direction anchorFace,
		double ropeLength
	) {
		hookAndReel$anchorBlockPos = blockPos.immutable();
		hookAndReel$anchorPosition = anchorPosition;
		hookAndReel$anchorFace = anchorFace;
		hookAndReel$setRopeLength(ropeLength);
		hookAndReel$reelTargetPosition = null;
		hookAndReel$reelStartGameTime = -1L;
		hookAndReel$setHookState(HookState.ANCHORED_IDLE);
	}

	@Override
	@Nullable
	public BlockPos hookAndReel$getAnchorBlockPos() {
		return hookAndReel$anchorBlockPos;
	}

	@Override
	@Nullable
	public Vec3 hookAndReel$getAnchorPosition() {
		return hookAndReel$anchorPosition;
	}

	@Override
	@Nullable
	public Direction hookAndReel$getAnchorFace() {
		return hookAndReel$anchorFace;
	}

	@Override
	public void hookAndReel$startReeling(Vec3 reelTargetPosition, long gameTime) {
		hookAndReel$reelTargetPosition = reelTargetPosition;
		hookAndReel$reelStartGameTime = gameTime;
		hookAndReel$setHookState(HookState.REELING_UP);
	}

	@Override
	@Nullable
	public Vec3 hookAndReel$getReelTargetPosition() {
		return hookAndReel$reelTargetPosition;
	}

	@Override
	public long hookAndReel$getReelStartGameTime() {
		return hookAndReel$reelStartGameTime;
	}

	@Override
	public void hookAndReel$finishReeling() {
		hookAndReel$reelTargetPosition = null;
		hookAndReel$reelStartGameTime = -1L;
	}

	@Override
	public double hookAndReel$getRopeLength() {
		FishingHook hook = (FishingHook) (Object) this;
		return hook.getEntityData().get(HOOK_AND_REEL_ROPE_LENGTH);
	}

	@Override
	public void hookAndReel$setRopeLength(double ropeLength) {
		FishingHook hook = (FishingHook) (Object) this;
		hook.getEntityData().set(HOOK_AND_REEL_ROPE_LENGTH, (float) Math.max(0.0D, ropeLength));
	}

	@Override
	public double hookAndReel$getMaximumRopeLength() {
		FishingHook hook = (FishingHook) (Object) this;
		return hook.getEntityData().get(HOOK_AND_REEL_MAXIMUM_ROPE_LENGTH);
	}

	@Override
	public int hookAndReel$getAnchorLevel() {
		return hookAndReel$anchorLevel;
	}

	@Override
	public void hookAndReel$clearAnchorState() {
		hookAndReel$anchorBlockPos = null;
		hookAndReel$anchorPosition = null;
		hookAndReel$anchorFace = null;
		hookAndReel$reelTargetPosition = null;
		hookAndReel$reelStartGameTime = -1L;
		hookAndReel$setRopeLength(0.0D);
		hookAndReel$setHookState(HookState.VANILLA);
		hookAndReel$swingLeftImpulse = 0.0F;
		hookAndReel$swingForwardImpulse = 0.0F;
		hookAndReel$groundedTicks = 0;
	}

	@Override
	public void hookAndReel$updateSwingInput(float leftImpulse, float forwardImpulse, long gameTime) {
		hookAndReel$swingLeftImpulse = Math.clamp(leftImpulse, -1.0F, 1.0F);
		hookAndReel$swingForwardImpulse = Math.clamp(forwardImpulse, -1.0F, 1.0F);
		hookAndReel$lastSwingInputGameTime = gameTime;
	}

	@Override
	public float hookAndReel$getSwingLeftImpulse() {
		return hookAndReel$swingLeftImpulse;
	}

	@Override
	public float hookAndReel$getSwingForwardImpulse() {
		return hookAndReel$swingForwardImpulse;
	}

	@Override
	public long hookAndReel$getLastSwingInputGameTime() {
		return hookAndReel$lastSwingInputGameTime;
	}

	@Override
	public int hookAndReel$incrementGroundedTicks() {
		return ++hookAndReel$groundedTicks;
	}

	@Override
	public void hookAndReel$resetGroundedTicks() {
		hookAndReel$groundedTicks = 0;
	}

	@Override
	public long hookAndReel$getLastMaximumRopeMessageTime() {
		return hookAndReel$lastMaximumRopeMessageTime;
	}

	@Override
	public void hookAndReel$setLastMaximumRopeMessageTime(long gameTime) {
		hookAndReel$lastMaximumRopeMessageTime = gameTime;
	}

	@Override
	public void hookAndReel$initializeFishingCast(ItemStack rod, InteractionHand hand) {
		hookAndReel$fishingRod = rod;
		hookAndReel$fishingHand = hand;
		hookAndReel$luckyWaterTicks = 0;
		hookAndReel$luckyCatchArmed = false;
		hookAndReel$instantCatchTriggered = false;
		hookAndReel$forcedBite = false;
	}

	@Override
	public ItemStack hookAndReel$getFishingRod() {
		return hookAndReel$fishingRod;
	}

	@Override
	public InteractionHand hookAndReel$getFishingHand() {
		return hookAndReel$fishingHand;
	}

	@Override
	public int hookAndReel$getLuckyWaterTicks() {
		return hookAndReel$luckyWaterTicks;
	}

	@Override
	public void hookAndReel$setLuckyWaterTicks(int ticks) {
		hookAndReel$luckyWaterTicks = Math.max(0, ticks);
	}

	@Override
	public boolean hookAndReel$isLuckyCatchArmed() {
		return hookAndReel$luckyCatchArmed;
	}

	@Override
	public void hookAndReel$setLuckyCatchArmed(boolean armed) {
		hookAndReel$luckyCatchArmed = armed;
	}

	@Override
	public boolean hookAndReel$isInstantCatchTriggered() {
		return hookAndReel$instantCatchTriggered;
	}

	@Override
	public void hookAndReel$setInstantCatchTriggered(boolean triggered) {
		hookAndReel$instantCatchTriggered = triggered;
	}

	@Override
	public int hookAndReel$getNibbleTicks() {
		return nibble;
	}

	@Override
	public void hookAndReel$armForcedBite(int nibbleTicks) {
		nibble = Math.max(nibble, Math.max(2, nibbleTicks));
		timeUntilLured = 0;
		timeUntilHooked = 0;
		biting = true;
		hookAndReel$forcedBite = true;
		((FishingHook) (Object) this).getEntityData().set(DATA_BITING, true);
	}

	@Override
	public void hookAndReel$resetLuckyCatchState() {
		if (hookAndReel$forcedBite) {
			nibble = 0;
			timeUntilLured = 0;
			timeUntilHooked = 0;
			biting = false;
			((FishingHook) (Object) this).getEntityData().set(DATA_BITING, false);
		}
		hookAndReel$luckyWaterTicks = 0;
		hookAndReel$luckyCatchArmed = false;
		hookAndReel$instantCatchTriggered = false;
		hookAndReel$forcedBite = false;
	}

	@Override
	public void hookAndReel$setVanillaHookedEntity(Entity entity) {
		setHookedEntity(entity);
	}
}
