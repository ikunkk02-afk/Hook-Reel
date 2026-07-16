package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class RopeConstraintMathTest {
	private static final double EPSILON = 1.0E-9D;

	@Test
	void slackRopeDoesNotOverrideVanillaMovement() {
		Vec3 velocity = new Vec3(0.2D, -0.4D, 0.1D);
		RopeConstraintMath.ConstraintResult result = constrain(
			new Vec3(5.0D, 0.0D, 0.0D),
			velocity,
			10.0D,
			new Vec3(1.0D, 0.0D, 0.0D)
		);

		assertEquals(velocity, result.velocity());
		assertEquals(Vec3.ZERO, result.positionCorrection());
		assertFalse(result.taut());
	}

	@Test
	void tautRopeRemovesOnlyOutwardRadialVelocity() {
		RopeConstraintMath.ConstraintResult result = constrain(
			new Vec3(10.0D, 0.0D, 0.0D),
			new Vec3(0.7D, -0.4D, 0.3D),
			10.0D,
			Vec3.ZERO
		);

		assertEquals(0.0D, result.velocity().x, EPSILON);
		assertEquals(-0.4D, result.velocity().y, EPSILON);
		assertEquals(0.3D, result.velocity().z, EPSILON);
		assertTrue(result.taut());
	}

	@Test
	void excessLengthUsesCappedVelocityAndPositionCorrections() {
		RopeConstraintMath.ConstraintResult result = constrain(
			new Vec3(20.0D, 0.0D, 0.0D),
			Vec3.ZERO,
			10.0D,
			Vec3.ZERO
		);

		assertEquals(-0.6D, result.velocity().x, EPSILON);
		assertEquals(-0.25D, result.positionCorrection().x, EPSILON);
	}

	@Test
	void controlIsProjectedOntoRopeTangentAndOverallSpeedIsCapped() {
		assertEquals(
			Vec3.ZERO,
			RopeConstraintMath.projectOntoTangentPlane(new Vec3(1.0D, 0.0D, 0.0D), new Vec3(1.0D, 0.0D, 0.0D))
		);
		Vec3 horizontal = RopeConstraintMath.projectOntoTangentPlane(
			new Vec3(1.0D, 0.0D, 0.0D),
			new Vec3(0.0D, 1.0D, 0.0D)
		);
		assertEquals(new Vec3(1.0D, 0.0D, 0.0D), horizontal);

		RopeConstraintMath.ConstraintResult result = RopeConstraintMath.constrain(
			new Vec3(10.0D, 0.0D, 0.0D),
			Vec3.ZERO,
			new Vec3(0.0D, 0.0D, 8.0D),
			10.0D,
			Vec3.ZERO,
			0.0D,
			2.2D,
			0.35D,
			0.9D,
			0.1D
		);
		assertEquals(2.2D, result.velocity().length(), EPSILON);
	}

	@Test
	void rappelSpeedConvertsBlocksPerSecondToPerTick() {
		assertEquals(0.125D, RopeConstraintMath.rappelIncrementPerTick(2.5D), EPSILON);
		assertEquals(0.0D, RopeConstraintMath.rappelIncrementPerTick(-2.5D), EPSILON);
	}

	private static RopeConstraintMath.ConstraintResult constrain(
		Vec3 attachment,
		Vec3 velocity,
		double ropeLength,
		Vec3 input
	) {
		return RopeConstraintMath.constrain(
			attachment,
			Vec3.ZERO,
			velocity,
			ropeLength,
			input,
			0.025D,
			5.0D,
			0.35D,
			1.0D,
			0.1D
		);
	}
}
