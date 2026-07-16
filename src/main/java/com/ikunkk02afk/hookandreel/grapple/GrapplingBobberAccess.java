package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface GrapplingBobberAccess {
	void hookAndReel$initializeGrapple(ItemStack rod, InteractionHand hand, double maximumRange);

	void hookAndReel$initializeHook(
		ItemStack rod,
		InteractionHand hand,
		GrappleMode mode,
		double maximumRange,
		double maximumRopeLength,
		int anchorLevel
	);

	boolean hookAndReel$isGrapple();

	double hookAndReel$getMaximumRange();

	ItemStack hookAndReel$getLaunchRod();

	InteractionHand hookAndReel$getLaunchHand();

	GrappleMode hookAndReel$getLaunchMode();

	UUID hookAndReel$getLaunchOwnerUuid();

	HookState hookAndReel$getHookState();

	void hookAndReel$setHookState(HookState state);

	long hookAndReel$getPullStartGameTime();

	void hookAndReel$startPull(long gameTime, Vec3 targetPosition);

	void hookAndReel$attachTarget(Entity target, GrappleTargetType targetType);

	GrappleTargetType hookAndReel$getTargetType();

	Vec3 hookAndReel$getLastTargetPosition();

	void hookAndReel$setLastTargetPosition(Vec3 position);

	boolean hookAndReel$isPulling(Entity target);

	void hookAndReel$anchor(
		BlockPos blockPos,
		Vec3 anchorPosition,
		Direction anchorFace,
		double ropeLength
	);

	BlockPos hookAndReel$getAnchorBlockPos();

	Vec3 hookAndReel$getAnchorPosition();

	Direction hookAndReel$getAnchorFace();

	void hookAndReel$startReeling(Vec3 reelTargetPosition, long gameTime);

	Vec3 hookAndReel$getReelTargetPosition();

	long hookAndReel$getReelStartGameTime();

	void hookAndReel$finishReeling();

	double hookAndReel$getRopeLength();

	void hookAndReel$setRopeLength(double ropeLength);

	double hookAndReel$getMaximumRopeLength();

	int hookAndReel$getAnchorLevel();

	void hookAndReel$clearAnchorState();

	void hookAndReel$updateSwingInput(float leftImpulse, float forwardImpulse, long gameTime);

	float hookAndReel$getSwingLeftImpulse();

	float hookAndReel$getSwingForwardImpulse();

	long hookAndReel$getLastSwingInputGameTime();

	int hookAndReel$incrementGroundedTicks();

	void hookAndReel$resetGroundedTicks();

	long hookAndReel$getLastMaximumRopeMessageTime();

	void hookAndReel$setLastMaximumRopeMessageTime(long gameTime);

	void hookAndReel$initializeFishingCast(ItemStack rod, InteractionHand hand);

	ItemStack hookAndReel$getFishingRod();

	InteractionHand hookAndReel$getFishingHand();

	int hookAndReel$getLuckyWaterTicks();

	void hookAndReel$setLuckyWaterTicks(int ticks);

	boolean hookAndReel$isLuckyCatchArmed();

	void hookAndReel$setLuckyCatchArmed(boolean armed);

	boolean hookAndReel$isInstantCatchTriggered();

	void hookAndReel$setInstantCatchTriggered(boolean triggered);

	int hookAndReel$getNibbleTicks();

	void hookAndReel$armForcedBite(int nibbleTicks);

	void hookAndReel$resetLuckyCatchState();

	void hookAndReel$setVanillaHookedEntity(Entity entity);
}
