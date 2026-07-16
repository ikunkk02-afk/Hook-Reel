package com.ikunkk02afk.hookandreel.grapple;

import net.minecraft.world.phys.Vec3;

public final class RopeConstraintMath {
	public static final double MAX_INWARD_CORRECTION_SPEED = 0.6D;
	public static final double MAX_POSITION_CORRECTION_PER_TICK = 0.25D;
	private static final double EPSILON = 1.0E-8D;

	private RopeConstraintMath() {
	}

	public static ConstraintResult constrain(
		Vec3 attachment,
		Vec3 anchor,
		Vec3 velocity,
		double ropeLength,
		Vec3 controlInput,
		double controlStrength,
		double maximumSpeed,
		double constraintStrength,
		double damping,
		double tolerance
	) {
		Vec3 ropeVector = attachment.subtract(anchor);
		double distance = ropeVector.length();
		if (distance < EPSILON) {
			return new ConstraintResult(capSpeed(velocity, maximumSpeed), Vec3.ZERO, false, distance);
		}

		double safeLength = Math.max(0.0D, ropeLength);
		double safeTolerance = Math.max(0.0D, tolerance);
		Vec3 direction = ropeVector.scale(1.0D / distance);
		boolean taut = distance >= Math.max(0.0D, safeLength - safeTolerance);
		Vec3 next = velocity;
		if (taut) {
			Vec3 tangentialInput = projectOntoTangentPlane(controlInput, direction);
			if (tangentialInput.lengthSqr() > EPSILON && controlStrength > 0.0D) {
				next = next.add(tangentialInput.normalize().scale(controlStrength));
			}

			double outwardRadialVelocity = Math.max(0.0D, next.dot(direction));
			if (outwardRadialVelocity > 0.0D) {
				next = next.subtract(direction.scale(outwardRadialVelocity * Math.clamp(damping, 0.0D, 1.0D)));
			}
		}

		double excess = Math.max(0.0D, distance - safeLength);
		Vec3 positionCorrection = Vec3.ZERO;
		if (excess > safeTolerance) {
			double inwardSpeed = Math.min(
				MAX_INWARD_CORRECTION_SPEED,
				excess * Math.max(0.0D, constraintStrength)
			);
			next = next.subtract(direction.scale(inwardSpeed));
			double correctionDistance = Math.min(
				MAX_POSITION_CORRECTION_PER_TICK,
				excess - safeTolerance
			);
			positionCorrection = direction.scale(-correctionDistance);
		}

		return new ConstraintResult(capSpeed(next, maximumSpeed), positionCorrection, taut, distance);
	}

	public static Vec3 projectOntoTangentPlane(Vec3 vector, Vec3 ropeDirection) {
		if (ropeDirection.lengthSqr() < EPSILON) {
			return vector;
		}
		Vec3 normalized = ropeDirection.normalize();
		return vector.subtract(normalized.scale(vector.dot(normalized)));
	}

	public static Vec3 movementInput(float leftImpulse, float forwardImpulse, float yawDegrees) {
		if (Math.abs(leftImpulse) < 1.0E-4F && Math.abs(forwardImpulse) < 1.0E-4F) {
			return Vec3.ZERO;
		}
		double yaw = Math.toRadians(yawDegrees);
		Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
		Vec3 right = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
		Vec3 movement = forward.scale(forwardImpulse).add(right.scale(leftImpulse));
		return movement.lengthSqr() > 1.0D ? movement.normalize() : movement;
	}

	public static double rappelIncrementPerTick(double blocksPerSecond) {
		return Math.max(0.0D, blocksPerSecond) / 20.0D;
	}

	private static Vec3 capSpeed(Vec3 velocity, double maximumSpeed) {
		double maximum = Math.max(0.0D, maximumSpeed);
		double speedSqr = velocity.lengthSqr();
		if (speedSqr <= maximum * maximum || speedSqr < EPSILON) {
			return velocity;
		}
		return velocity.scale(maximum / Math.sqrt(speedSqr));
	}

	public record ConstraintResult(
		Vec3 velocity,
		Vec3 positionCorrection,
		boolean taut,
		double distance
	) {
	}
}
