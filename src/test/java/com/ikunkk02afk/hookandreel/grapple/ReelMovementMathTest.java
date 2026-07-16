package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ReelMovementMathTest {
	private static final double EPSILON = 1.0E-9D;

	@Test
	void targetAboveAddsErrorScaledUpwardMotionAndPreservesSideInertia() {
		HookReelConfig config = new HookReelConfig();
		Vec3 initial = new Vec3(0.0D, -0.2D, 0.25D);

		ReelMovementMath.ReelResult result = ReelMovementMath.apply(
			Vec3.ZERO,
			new Vec3(8.0D, 12.0D, 0.0D),
			initial,
			false,
			config
		);

		assertFalse(result.arrived());
		assertTrue(result.velocity().y > initial.y);
		assertTrue(result.velocity().z > 0.0D);
		assertTrue(result.velocity().length() <= config.maximumReelSpeed + EPSILON);
	}

	@Test
	void threeBlocksFromTargetDoesNotCountAsArrival() {
		HookReelConfig config = new HookReelConfig();
		ReelMovementMath.ReelResult result = ReelMovementMath.apply(
			Vec3.ZERO,
			new Vec3(0.0D, 3.0D, 0.0D),
			Vec3.ZERO,
			false,
			config
		);
		assertFalse(result.arrived());
		assertTrue(result.velocity().y > 0.0D);
	}

	@Test
	void collisionReducesMotionIntoWallAndAddsLimitedLift() {
		HookReelConfig config = new HookReelConfig();
		Vec3 initial = new Vec3(0.8D, 0.0D, 0.0D);
		ReelMovementMath.ReelResult result = ReelMovementMath.apply(
			Vec3.ZERO,
			new Vec3(8.0D, 8.0D, 0.0D),
			initial,
			true,
			config
		);
		assertTrue(result.velocity().x < initial.x);
		assertTrue(result.velocity().y > 0.0D);
		assertTrue(result.velocity().length() <= config.maximumReelSpeed + EPSILON);
	}

	@Test
	void targetArrivalPreservesExistingVelocity() {
		HookReelConfig config = new HookReelConfig();
		Vec3 velocity = new Vec3(0.2D, 0.3D, -0.1D);
		ReelMovementMath.ReelResult result = ReelMovementMath.apply(
			Vec3.ZERO,
			new Vec3(0.5D, 0.0D, 0.0D),
			velocity,
			false,
			config
		);
		assertTrue(result.arrived());
		assertEquals(velocity, result.velocity());
	}

	@Test
	void aAndDUseYawOnlyScreenDirections() {
		HookReelConfig config = new HookReelConfig();
		Vec3 target = new Vec3(0.0D, 10.0D, 0.0D);

		ReelMovementMath.ReelResult dFacingSouth = ReelMovementMath.apply(
			Vec3.ZERO,
			target,
			Vec3.ZERO,
			false,
			1.0F,
			0.0F,
			true,
			false,
			config
		);
		ReelMovementMath.ReelResult aFacingSouth = ReelMovementMath.apply(
			Vec3.ZERO,
			target,
			Vec3.ZERO,
			false,
			-1.0F,
			0.0F,
			true,
			false,
			config
		);
		ReelMovementMath.ReelResult dFacingWest = ReelMovementMath.apply(
			Vec3.ZERO,
			target,
			Vec3.ZERO,
			false,
			1.0F,
			90.0F,
			true,
			false,
			config
		);

		assertTrue(dFacingSouth.velocity().x < 0.0D, "D must move toward screen-right when yaw is zero");
		assertTrue(aFacingSouth.velocity().x > 0.0D, "A must move toward screen-left when yaw is zero");
		assertTrue(dFacingWest.velocity().z < 0.0D, "turning yaw must rotate screen-right with the view");
		assertEquals(0.0D, dFacingSouth.velocity().z, EPSILON);
	}

	@Test
	void strafeDirectionIsProjectedOntoReelTangentPlane() {
		Vec3 reelDirection = new Vec3(10.0D, 10.0D, 0.0D).normalize();
		Vec3 strafeDirection = ReelMovementMath.strafeDirection(
			Vec3.ZERO,
			new Vec3(10.0D, 10.0D, 0.0D),
			0.0F
		);

		assertEquals(1.0D, strafeDirection.length(), EPSILON);
		assertEquals(0.0D, strafeDirection.dot(reelDirection), EPSILON);
	}

	@Test
	void lateralCapOnlyLimitsNewControlAcceleration() {
		Vec3 strafeDirection = new Vec3(-1.0D, 0.0D, 0.0D);
		Vec3 belowCap = strafeDirection.scale(0.44D).add(0.0D, 0.30D, 0.20D);
		Vec3 capped = ReelMovementMath.addLimitedLateralAcceleration(
			belowCap,
			strafeDirection,
			0.10D,
			0.45D
		);
		Vec3 inheritedFast = strafeDirection.scale(0.80D).add(0.0D, 0.30D, 0.20D);
		Vec3 preserved = ReelMovementMath.addLimitedLateralAcceleration(
			inheritedFast,
			strafeDirection,
			0.10D,
			0.45D
		);

		assertEquals(0.45D, capped.dot(strafeDirection), EPSILON);
		assertEquals(0.30D, capped.y, EPSILON, "the lateral cap must preserve upward reel velocity");
		assertEquals(0.20D, capped.z, EPSILON, "the lateral cap must preserve other inertia");
		assertEquals(inheritedFast, preserved, "pre-existing speed above the lateral cap must not be bluntly clamped");
	}

	@Test
	void collisionRetentionPreservesVerticalVelocityAndFadeIsGradual() {
		Vec3 retained = ReelMovementMath.retainHorizontalForwardVelocity(
			new Vec3(1.0D, 0.40D, 0.20D),
			new Vec3(8.0D, 5.0D, 0.0D),
			0.35D
		);

		assertEquals(0.35D, retained.x, EPSILON);
		assertEquals(0.40D, retained.y, EPSILON);
		assertEquals(0.20D, retained.z, EPSILON);
		assertEquals(0.50D, ReelMovementMath.lateralFade(1.25D, 2.50D), EPSILON);
		assertEquals(1.0D, ReelMovementMath.lateralFade(4.0D, 2.50D), EPSILON);
		assertEquals(1.0D, ReelMovementMath.lateralFade(0.5D, 0.0D), EPSILON);
	}

	@Test
	void blockedSideStopsAccelerationAndObstacleBoostStaysCapped() {
		HookReelConfig config = new HookReelConfig();
		Vec3 target = new Vec3(0.0D, 10.0D, 0.0D);
		ReelMovementMath.ReelResult blocked = ReelMovementMath.apply(
			Vec3.ZERO,
			target,
			Vec3.ZERO,
			false,
			1.0F,
			0.0F,
			false,
			true,
			config
		);
		ReelMovementMath.ReelResult normal = ReelMovementMath.apply(
			Vec3.ZERO,
			target,
			Vec3.ZERO,
			false,
			1.0F,
			0.0F,
			true,
			false,
			config
		);
		ReelMovementMath.ReelResult bypass = ReelMovementMath.apply(
			Vec3.ZERO,
			target,
			Vec3.ZERO,
			true,
			1.0F,
			0.0F,
			true,
			true,
			config
		);
		Vec3 right = ReelMovementMath.strafeDirection(Vec3.ZERO, target, 0.0F);

		assertEquals(0.0D, blocked.velocity().dot(right), EPSILON);
		assertTrue(bypass.velocity().dot(right) > normal.velocity().dot(right));
		assertTrue(bypass.velocity().dot(right) <= config.maximumReelingLateralSpeed + EPSILON);
	}

	@Test
	void noLateralInputKeepsTheExistingReelResult() {
		HookReelConfig config = new HookReelConfig();
		Vec3 attachment = new Vec3(1.0D, 2.0D, 3.0D);
		Vec3 target = new Vec3(7.0D, 12.0D, -4.0D);
		Vec3 velocity = new Vec3(0.2D, -0.1D, 0.3D);

		ReelMovementMath.ReelResult existing = ReelMovementMath.apply(
			attachment,
			target,
			velocity,
			true,
			config
		);
		ReelMovementMath.ReelResult extended = ReelMovementMath.apply(
			attachment,
			target,
			velocity,
			true,
			0.0F,
			135.0F,
			false,
			true,
			config
		);

		assertEquals(existing, extended);
	}
}
