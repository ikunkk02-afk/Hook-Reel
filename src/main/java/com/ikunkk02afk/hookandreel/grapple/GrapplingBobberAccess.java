package com.ikunkk02afk.hookandreel.grapple;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface GrapplingBobberAccess {
	void hookAndReel$initializeGrapple(ItemStack rod, InteractionHand hand, double maximumRange);

	boolean hookAndReel$isGrapple();

	double hookAndReel$getMaximumRange();

	ItemStack hookAndReel$getLaunchRod();

	InteractionHand hookAndReel$getLaunchHand();

	long hookAndReel$getPullStartGameTime();

	void hookAndReel$startPull(long gameTime, Vec3 targetPosition);

	void hookAndReel$attachTarget(Entity target, GrappleTargetType targetType);

	GrappleTargetType hookAndReel$getTargetType();

	Vec3 hookAndReel$getLastTargetPosition();

	void hookAndReel$setLastTargetPosition(Vec3 position);

	boolean hookAndReel$isPulling(Entity target);
}
