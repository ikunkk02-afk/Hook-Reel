package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class WallClingMathTest {
	@Test
	void idleClingHoldsHeightWithoutClearingHorizontalControl() {
		HookReelConfig config = new HookReelConfig();
		Vec3 normal = new Vec3(-1.0D, 0.0D, 0.0D);
		Vec3 result = WallClingMath.clingVelocity(
			new Vec3(-0.5D, -0.8D, 0.35D),
			normal,
			0.2D,
			0.0F,
			0.0F,
			0.0F,
			false,
			config
		);
		assertTrue(result.dot(normal) <= 0.0D, "cling must not keep outward velocity");
		assertEquals(0.0D, result.y, "idle cling must override gravity with a stable hold");
		assertTrue(result.multiply(1.0D, 0.0D, 1.0D).lengthSqr() > 0.0D, "vertical hold must not clear horizontal wall control");
	}

	@Test
	void verticalInputDirectlySelectsClimbDescendOrHold() {
		HookReelConfig config = new HookReelConfig();
		Vec3 normal = new Vec3(0.0D, 0.0D, 1.0D);
		Vec3 climbing = WallClingMath.clingVelocity(
			Vec3.ZERO,
			normal,
			0.1D,
			0.0F,
			1.0F,
			0.0F,
			false,
			config
		);
		assertEquals(config.wallClimbSpeed, climbing.y, "full W input must use the configured upward speed");

		Vec3 descending = WallClingMath.clingVelocity(
			climbing,
			normal,
			0.1D,
			0.0F,
			-1.0F,
			0.0F,
			false,
			config
		);
		assertEquals(-config.wallClimbDownSpeed, descending.y, "full S input must use the configured downward speed");

		Vec3 released = WallClingMath.clingVelocity(
			descending,
			normal,
			0.1D,
			0.0F,
			0.0F,
			0.0F,
			false,
			config
		);
		assertEquals(0.0D, released.y, "releasing W or S must stop vertical motion immediately");

		Vec3 shifted = WallClingMath.clingVelocity(
			climbing,
			normal,
			0.1D,
			0.0F,
			1.0F,
			0.0F,
			true,
			config
		);
		assertEquals(0.0D, shifted.y, "Shift must hold the current wall height even with movement input");
	}

	@Test
	void wallJumpMovesUpAndOut() {
		HookReelConfig config = new HookReelConfig();
		Vec3 normal = new Vec3(0.0D, 0.0D, 1.0D);
		Vec3 climbing = new Vec3(0.0D, config.wallClimbSpeed, 0.0D);
		Vec3 jump = WallClingMath.wallJumpVelocity(climbing, normal, config);
		assertTrue(jump.y >= config.wallJumpUpVelocity);
		assertTrue(jump.dot(normal) >= config.wallJumpOutVelocity);
	}
}
