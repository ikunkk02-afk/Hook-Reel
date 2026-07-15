package com.ikunkk02afk.hookandreel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HookReelConfigTest {
	@Test
	void defaultsMatchThePublicConfigurationContract() {
		HookReelConfig config = new HookReelConfig();

		assertTrue(config.luckyEnchantmentEnabled);
		assertEquals(1.5D, config.luckyBonusPerLevel);
		assertTrue(config.allowStackWithLuckOfTheSea);
		assertEquals(10, config.grappleCooldownSeconds);
		assertEquals(1.5D, config.maxChargeTimeSeconds);
		assertTrue(config.swingEnabled);
		assertTrue(config.rappelEnabled);
	}

	@Test
	void validationClampsRangesAndRestoresNonFiniteNumbers() {
		HookReelConfig config = new HookReelConfig();
		config.luckyBonusPerLevel = Double.NaN;
		config.grappleCooldownSeconds = 999;
		config.maxChargeTimeSeconds = Double.POSITIVE_INFINITY;
		config.swingEnabled = false;

		HookReelConfig validated = config.validatedCopy();

		assertEquals(1.5D, validated.luckyBonusPerLevel);
		assertEquals(300, validated.grappleCooldownSeconds);
		assertEquals(1.5D, validated.maxChargeTimeSeconds);
		assertFalse(validated.swingEnabled);

		config.luckyBonusPerLevel = -5.0D;
		config.grappleCooldownSeconds = -1;
		config.maxChargeTimeSeconds = 99.0D;
		validated = config.validatedCopy();

		assertEquals(0.0D, validated.luckyBonusPerLevel);
		assertEquals(0, validated.grappleCooldownSeconds);
		assertEquals(10.0D, validated.maxChargeTimeSeconds);
	}
}
