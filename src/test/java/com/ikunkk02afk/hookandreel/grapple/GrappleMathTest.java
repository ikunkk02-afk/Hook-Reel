package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GrappleMathTest {
	@Test
	void chargeCurveClampsAndReachesConfiguredRanges() {
		assertEquals(0.0D, GrappleMath.chargeProgress(-10, 30));
		assertEquals(0.5D, GrappleMath.chargeProgress(15, 30));
		assertEquals(1.0D, GrappleMath.chargeProgress(90, 30));
		assertEquals(8.0D, GrappleMath.actualRange(0, 30, 8.0D, 48.0D));
		assertEquals(48.0D, GrappleMath.actualRange(30, 30, 8.0D, 48.0D));
		assertTrue(GrappleMath.actualRange(15, 30, 8.0D, 48.0D) > 20.0D);
		assertEquals(24.0D, GrappleMath.actualRange(30, 30, 8.0D, 24.0D));
		assertEquals(36.0D, GrappleMath.actualRange(30, 30, 8.0D, 36.0D));
	}

	@Test
	void pullVelocityUsesDampingAndNeverExceedsMaximum() {
		Vec3 far = GrappleMath.pulledVelocity(Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D), 30.0D, 2.0D, 0.12D, 1.5D);
		Vec3 near = GrappleMath.pulledVelocity(Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D), 4.0D, 2.0D, 0.12D, 1.5D);
		Vec3 fast = GrappleMath.pulledVelocity(new Vec3(10.0D, 0.0D, 0.0D), new Vec3(1.0D, 0.0D, 0.0D), 30.0D, 2.0D, 0.12D, 1.5D);

		assertTrue(far.length() > near.length());
		assertTrue(far.length() <= 1.5D);
		assertEquals(1.5D, fast.length(), 1.0E-9D);
	}

	@Test
	void axisGapSupportsCollisionBoxStopChecks() {
		assertEquals(0.0D, GrappleMath.axisGap(0.0D, 1.0D, 0.5D, 2.0D));
		assertEquals(2.0D, GrappleMath.axisGap(0.0D, 1.0D, 3.0D, 4.0D));
	}
}
