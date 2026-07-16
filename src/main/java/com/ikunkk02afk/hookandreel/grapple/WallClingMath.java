package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import net.minecraft.world.phys.Vec3;

public final class WallClingMath {
	private static final double INPUT_DEAD_ZONE = 0.1D;

	private WallClingMath() {
	}

	public static Vec3 clingVelocity(
		Vec3 velocity,
		Vec3 outwardNormal,
		double wallDistance,
		float leftImpulse,
		float forwardImpulse,
		float yawDegrees,
		boolean shiftHeld,
		HookReelConfig config
	) {
		Vec3 normal = outwardNormal.normalize();
		Vec3 tangent = new Vec3(-normal.z, 0.0D, normal.x).normalize();
		double yaw = Math.toRadians(yawDegrees);
		Vec3 cameraRight = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
		double tangentSign = Math.signum(cameraRight.dot(tangent));
		if (tangentSign == 0.0D) {
			tangentSign = 1.0D;
		}

		double targetTangent = shiftHeld
			? 0.0D
			: leftImpulse * tangentSign * config.wallHorizontalMoveSpeed;
		double targetVertical = 0.0D;
		if (shiftHeld) {
			targetVertical = 0.0D;
		} else if (forwardImpulse > INPUT_DEAD_ZONE) {
			targetVertical = config.wallClimbSpeed * forwardImpulse;
		} else if (forwardImpulse < -INPUT_DEAD_ZONE) {
			targetVertical = config.wallClimbDownSpeed * forwardImpulse;
		}

		double desiredGap = Math.min(0.08D, config.wallDetectionDistance * 0.25D);
		double targetNormal = wallDistance > desiredGap
			? -config.wallClingStrength
			: -config.wallClingStrength * 0.15D;
		double tangentSpeed = approach(
			velocity.dot(tangent),
			targetTangent,
			config.wallClingStrength
		);
		double normalSpeed = approach(
			Math.min(0.0D, velocity.dot(normal)),
			targetNormal,
			config.wallClingStrength
		);
		return tangent.scale(tangentSpeed)
			.add(normal.scale(normalSpeed))
			.add(0.0D, targetVertical, 0.0D);
	}

	public static Vec3 wallJumpVelocity(
		Vec3 velocity,
		Vec3 outwardNormal,
		HookReelConfig config
	) {
		Vec3 normal = outwardNormal.normalize();
		Vec3 tangent = new Vec3(-normal.z, 0.0D, normal.x).normalize();
		return tangent.scale(velocity.dot(tangent))
			.add(normal.scale(config.wallJumpOutVelocity))
			.add(0.0D, Math.max(velocity.y, config.wallJumpUpVelocity), 0.0D);
	}

	private static double approach(double current, double target, double maximumChange) {
		double change = Math.clamp(target - current, -maximumChange, maximumChange);
		return current + change;
	}
}
