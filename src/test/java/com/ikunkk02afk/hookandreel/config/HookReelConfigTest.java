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
		assertTrue(config.grapplingHookEnabled);
		assertEquals(8.0D, config.minimumGrappleRange);
		assertEquals(24.0D, config.grappleLevel1MaxRange);
		assertEquals(36.0D, config.grappleLevel2MaxRange);
		assertEquals(48.0D, config.grappleLevel3MaxRange);
		assertEquals(0.12D, config.pullStrength);
		assertEquals(1.5D, config.maximumPullSpeed);
		assertEquals(1.6D, config.itemPullSpeedMultiplier);
		assertEquals(2.0D, config.pullStopDistance);
		assertEquals(8.0D, config.maxPullDurationSeconds);
		assertTrue(config.allowPullPlayers);
		assertFalse(config.allowPullBosses);
		assertTrue(config.blockPullingEnabled);
		assertFalse(config.allowPullBlockEntities);
		assertEquals(50.0D, config.maximumBlockHardness);
		assertEquals(0.8D, config.blockPullSpeedMultiplier);
		assertEquals(10.0D, config.maxBlockPullDurationSeconds);
		assertEquals(2.5D, config.blockPullStopDistance);
		assertEquals(3, config.blockPullDurabilityCost);
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
		config.minimumGrappleRange = Double.NaN;
		config.grappleLevel1MaxRange = -1.0D;
		config.grappleLevel2MaxRange = 5.0D;
		config.grappleLevel3MaxRange = 2.0D;
		config.pullStrength = -1.0D;
		config.maximumPullSpeed = 99.0D;
		config.itemPullSpeedMultiplier = Double.POSITIVE_INFINITY;
		config.pullStopDistance = 0.0D;
		config.maxPullDurationSeconds = 999.0D;
		config.allowPullBlockEntities = true;
		config.maximumBlockHardness = Double.NaN;
		config.blockPullSpeedMultiplier = 99.0D;
		config.maxBlockPullDurationSeconds = 0.0D;
		config.blockPullStopDistance = Double.POSITIVE_INFINITY;
		config.blockPullDurabilityCost = 999;

		HookReelConfig validated = config.validatedCopy();

		assertEquals(1.5D, validated.luckyBonusPerLevel);
		assertEquals(300, validated.grappleCooldownSeconds);
		assertEquals(1.5D, validated.maxChargeTimeSeconds);
		assertFalse(validated.swingEnabled);
		assertEquals(8.0D, validated.minimumGrappleRange);
		assertEquals(8.0D, validated.grappleLevel1MaxRange);
		assertEquals(8.0D, validated.grappleLevel2MaxRange);
		assertEquals(8.0D, validated.grappleLevel3MaxRange);
		assertEquals(0.0D, validated.pullStrength);
		assertEquals(10.0D, validated.maximumPullSpeed);
		assertEquals(1.6D, validated.itemPullSpeedMultiplier);
		assertEquals(0.25D, validated.pullStopDistance);
		assertEquals(60.0D, validated.maxPullDurationSeconds);
		assertFalse(validated.allowPullBlockEntities);
		assertEquals(50.0D, validated.maximumBlockHardness);
		assertEquals(5.0D, validated.blockPullSpeedMultiplier);
		assertEquals(0.5D, validated.maxBlockPullDurationSeconds);
		assertEquals(2.5D, validated.blockPullStopDistance);
		assertEquals(64, validated.blockPullDurabilityCost);

		config.luckyBonusPerLevel = -5.0D;
		config.grappleCooldownSeconds = -1;
		config.maxChargeTimeSeconds = 99.0D;
		validated = config.validatedCopy();

		assertEquals(0.0D, validated.luckyBonusPerLevel);
		assertEquals(0, validated.grappleCooldownSeconds);
		assertEquals(10.0D, validated.maxChargeTimeSeconds);
	}
}
