package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HookAbilityCooldownManagerTest {
	@Test
	void remainingTimeExpiresAndClampsCorruptFutureValues() {
		assertEquals(0L, HookAbilityCooldownManager.calculateRemaining(90L, 100L, 200));
		assertEquals(40L, HookAbilityCooldownManager.calculateRemaining(140L, 100L, 200));
		assertEquals(200L, HookAbilityCooldownManager.calculateRemaining(1000L, 100L, 200));
		assertEquals(0L, HookAbilityCooldownManager.calculateRemaining(1000L, 100L, 0));
	}

	@Test
	void secondsSupportIndependentFractionalCooldowns() {
		assertEquals(200, HookAbilityCooldownManager.secondsToTicks(10.0D));
		assertEquals(30, HookAbilityCooldownManager.secondsToTicks(1.5D));
		assertEquals(40, HookAbilityCooldownManager.secondsToTicks(2.0D));
		assertEquals(18, HookAbilityCooldownManager.secondsToTicks(0.9D));
		assertEquals(10, HookAbilityCooldownManager.secondsToTicks(0.5D));
		assertEquals(5, HookAbilityCooldownManager.secondsToTicks(0.25D));
		assertEquals(0, HookAbilityCooldownManager.secondsToTicks(0.0D));
		assertEquals(0, HookAbilityCooldownManager.secondsToTicks(-1.0D));
		assertEquals(0, HookAbilityCooldownManager.secondsToTicks(Double.NaN));
		assertEquals(0, HookAbilityCooldownManager.secondsToTicks(Double.POSITIVE_INFINITY));
	}

	@Test
	void actionBarTimeAlwaysUsesOneDecimalPlace() {
		assertEquals("10.0", HookAbilityCooldownManager.formatRemainingSeconds(200L));
		assertEquals("1.5", HookAbilityCooldownManager.formatRemainingSeconds(30L));
		assertEquals("0.5", HookAbilityCooldownManager.formatRemainingSeconds(10L));
		assertEquals("0.0", HookAbilityCooldownManager.formatRemainingSeconds(0L));
	}
}
