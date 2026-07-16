package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import net.minecraft.world.phys.Vec3;

public final class ReelMovementMath {
	private static final double EPSILON = 1.0E-8D;
	private static final double INPUT_DEAD_ZONE = 1.0E-4D;
	private static final double MINIMUM_APPROACH_SPEED = 0.15D;
	private static final double DEFAULT_COLLISION_FORWARD_RETENTION = 0.15D;

	private ReelMovementMath() {
	}

	public static ReelResult apply(
		Vec3 attachment,
		Vec3 target,
		Vec3 velocity,
		boolean horizontalCollision,
		HookReelConfig config
	) {
		return apply(
			attachment,
			target,
			velocity,
			horizontalCollision,
			0.0F,
			0.0F,
			true,
			false,
			config
		);
	}

	public static ReelResult apply(
		Vec3 attachment,
		Vec3 target,
		Vec3 velocity,
		boolean horizontalCollision,
		float sidewaysInput,
		float yawDegrees,
		boolean lateralPathClear,
		boolean forwardPathBlocked,
		HookReelConfig config
	) {
		Vec3 error = target.subtract(attachment);
		double distance = error.length();
		double verticalError = error.y;
		if (distance <= config.reelArrivalDistance || distance < EPSILON) {
			return new ReelResult(capSpeed(velocity, maximumSpeed(config)), true, distance, verticalError);
		}

		double maximumSpeed = maximumSpeed(config);
		double desiredSpeed = Math.min(maximumSpeed, Math.max(MINIMUM_APPROACH_SPEED, distance * 0.35D));
		Vec3 desiredVelocity = error.scale(desiredSpeed / distance);
		if (verticalError > 0.0D) {
			double minimumUpward = Math.min(
				maximumSpeed,
				0.12D + Math.min(0.35D, verticalError * 0.08D)
			);
			desiredVelocity = new Vec3(
				desiredVelocity.x,
				Math.max(desiredVelocity.y, minimumUpward),
				desiredVelocity.z
			);
		}

		Vec3 next = moveToward(velocity, desiredVelocity, config.reelAcceleration);
		float clampedSidewaysInput = Math.clamp(sidewaysInput, -1.0F, 1.0F);
		boolean lateralRequested = config.reelingLateralControlEnabled
			&& Math.abs(clampedSidewaysInput) > INPUT_DEAD_ZONE;
		if (horizontalCollision) {
			if (lateralRequested || verticalError > config.reelArrivalDistance) {
				double forwardRetention = lateralRequested
					? config.reelingCollisionForwardRetention
					: DEFAULT_COLLISION_FORWARD_RETENTION;
				next = retainHorizontalForwardVelocity(next, error, forwardRetention);
			}
			if (verticalError > config.reelArrivalDistance) {
				double collisionLift = Math.min(
					config.reelAcceleration,
					Math.max(0.02D, verticalError * 0.03D)
				);
				next = next.add(0.0D, collisionLift, 0.0D);
			}
		}

		next = capSpeed(next, maximumSpeed);
		if (lateralRequested && lateralPathClear) {
			Vec3 strafeDirection = strafeDirection(attachment, target, yawDegrees);
			double obstacleMultiplier = horizontalCollision || forwardPathBlocked
				? config.reelingObstacleBypassMultiplier
				: 1.0D;
			double lateralAcceleration = config.reelingLateralControlStrength
				* clampedSidewaysInput
				* obstacleMultiplier
				* lateralFade(distance, config.reelingLateralFadeDistance);
			next = addLimitedLateralAcceleration(
				next,
				strafeDirection,
				lateralAcceleration,
				config.maximumReelingLateralSpeed
			);
		}
		return new ReelResult(next, false, distance, verticalError);
	}

	public static Vec3 strafeDirection(Vec3 attachment, Vec3 target, float yawDegrees) {
		Vec3 rawRight = viewRight(yawDegrees);
		Vec3 toTarget = target.subtract(attachment);
		if (toTarget.lengthSqr() < EPSILON) {
			return rawRight;
		}
		Vec3 reelDirection = toTarget.normalize();
		Vec3 projected = rawRight.subtract(reelDirection.scale(rawRight.dot(reelDirection)));
		return projected.lengthSqr() < EPSILON ? rawRight : projected.normalize();
	}

	public static Vec3 viewRight(float yawDegrees) {
		double yaw = Math.toRadians(yawDegrees);
		Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
		return forward.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
	}

	public static Vec3 addLimitedLateralAcceleration(
		Vec3 velocity,
		Vec3 strafeDirection,
		double acceleration,
		double maximumLateralSpeed
	) {
		double maximum = Math.max(0.0D, maximumLateralSpeed);
		if (
			maximum <= 0.0D
				|| Math.abs(acceleration) < EPSILON
				|| strafeDirection.lengthSqr() < EPSILON
		) {
			return velocity;
		}
		Vec3 direction = strafeDirection.normalize();
		double currentLateralSpeed = velocity.dot(direction);
		double proposedLateralSpeed = currentLateralSpeed + acceleration;
		double appliedAcceleration = acceleration;
		if (Math.abs(currentLateralSpeed) <= maximum) {
			double cappedLateralSpeed = Math.clamp(proposedLateralSpeed, -maximum, maximum);
			appliedAcceleration = cappedLateralSpeed - currentLateralSpeed;
		} else if (Math.abs(proposedLateralSpeed) > Math.abs(currentLateralSpeed)) {
			appliedAcceleration = 0.0D;
		}
		return velocity.add(direction.scale(appliedAcceleration));
	}

	public static Vec3 retainHorizontalForwardVelocity(
		Vec3 velocity,
		Vec3 error,
		double retention
	) {
		Vec3 horizontalDirection = new Vec3(error.x, 0.0D, error.z);
		if (horizontalDirection.lengthSqr() < EPSILON) {
			return velocity;
		}
		horizontalDirection = horizontalDirection.normalize();
		double forwardSpeed = Math.max(0.0D, velocity.dot(horizontalDirection));
		double retainedSpeed = forwardSpeed * Math.clamp(retention, 0.0D, 1.0D);
		return velocity.subtract(horizontalDirection.scale(forwardSpeed - retainedSpeed));
	}

	public static double lateralFade(double distanceToTarget, double fadeDistance) {
		if (fadeDistance <= EPSILON) {
			return 1.0D;
		}
		return Math.clamp(Math.max(0.0D, distanceToTarget) / fadeDistance, 0.0D, 1.0D);
	}

	private static Vec3 moveToward(Vec3 current, Vec3 target, double maximumChange) {
		Vec3 change = target.subtract(current);
		double length = change.length();
		double safeChange = Math.max(0.0D, maximumChange);
		return length <= safeChange || length < EPSILON
			? target
			: current.add(change.scale(safeChange / length));
	}

	private static double maximumSpeed(HookReelConfig config) {
		return Math.max(0.0D, Math.min(config.maximumReelSpeed, config.maximumSwingSpeed));
	}

	private static Vec3 capSpeed(Vec3 velocity, double maximumSpeed) {
		double lengthSqr = velocity.lengthSqr();
		return lengthSqr > maximumSpeed * maximumSpeed && lengthSqr > EPSILON
			? velocity.scale(maximumSpeed / Math.sqrt(lengthSqr))
			: velocity;
	}

	public record ReelResult(Vec3 velocity, boolean arrived, double distance, double verticalError) {
	}
}
