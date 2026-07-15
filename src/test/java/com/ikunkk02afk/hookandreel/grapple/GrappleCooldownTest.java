package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GrappleCooldownTest {
	@Test
	void remainingTimeExpiresAndClampsCorruptFutureValues() {
		assertEquals(0L, GrappleCooldown.calculateRemaining(90L, 100L, 200));
		assertEquals(40L, GrappleCooldown.calculateRemaining(140L, 100L, 200));
		assertEquals(200L, GrappleCooldown.calculateRemaining(1000L, 100L, 200));
		assertEquals(0L, GrappleCooldown.calculateRemaining(1000L, 100L, 0));
	}
}
