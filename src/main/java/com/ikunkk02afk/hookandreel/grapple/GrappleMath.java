package com.ikunkk02afk.hookandreel.grapple;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

public final class GrappleMath {
	private static final double VELOCITY_RETENTION = 0.55D;
	private static final double DESIRED_VELOCITY_WEIGHT = 0.45D;

	private GrappleMath() {
	}

	public static double chargeProgress(int chargeTicks, int maxChargeTicks) {
		if (maxChargeTicks <= 0) {
			return 1.0D;
		}
		return Math.clamp((double) chargeTicks / maxChargeTicks, 0.0D, 1.0D);
	}

	public static double chargePower(double progress) {
		double clamped = Math.clamp(progress, 0.0D, 1.0D);
		return (clamped * clamped + 2.0D * clamped) / 3.0D;
	}

	public static double actualRange(int chargeTicks, int maxChargeTicks, double minimumRange, double maximumRange) {
		double power = chargePower(chargeProgress(chargeTicks, maxChargeTicks));
		return minimumRange + (Math.max(minimumRange, maximumRange) - minimumRange) * power;
	}

	public static double launchSpeed(double actualRange) {
		return Math.max(0.0D, actualRange) * 0.08D;
	}

	public static Vec3 pulledVelocity(
		Vec3 currentVelocity,
		Vec3 direction,
		double distance,
		double stopDistance,
		double pullStrength,
		double maximumSpeed
	) {
		if (maximumSpeed <= 0.0D || direction.lengthSqr() < 1.0E-8D) {
			return Vec3.ZERO;
		}
		double desiredSpeed = Math.min(
			maximumSpeed,
			Math.max(0.0D, pullStrength) * Math.max(0.0D, distance - stopDistance)
		);
		Vec3 next = currentVelocity.scale(VELOCITY_RETENTION)
			.add(direction.normalize().scale(desiredSpeed * DESIRED_VELOCITY_WEIGHT));
		double speedSqr = next.lengthSqr();
		if (speedSqr > maximumSpeed * maximumSpeed) {
			return next.normalize().scale(maximumSpeed);
		}
		return next;
	}

	public static double axisGap(double firstMin, double firstMax, double secondMin, double secondMax) {
		if (firstMax < secondMin) {
			return secondMin - firstMax;
		}
		if (secondMax < firstMin) {
			return firstMin - secondMax;
		}
		return 0.0D;
	}

	public static double boundingBoxDistance(AABB first, AABB second) {
		double x = axisGap(first.minX, first.maxX, second.minX, second.maxX);
		double y = axisGap(first.minY, first.maxY, second.minY, second.maxY);
		double z = axisGap(first.minZ, first.maxZ, second.minZ, second.maxZ);
		return Math.sqrt(x * x + y * y + z * z);
	}
}
