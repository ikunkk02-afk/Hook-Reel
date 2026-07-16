package com.ikunkk02afk.hookandreel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HookReelConfigTest {
	@Test
	void validationPreservesChildSelectionsWhenParentSwitchesAreDisabled() {
		HookReelConfig config = new HookReelConfig();
		config.allowFishingEntities = false;
		config.allowAquaticEntities = false;
		config.allowNetherEntities = false;
		config.netherEntitiesOnlyInNether = true;
		config.allowBossEntities = false;
		config.allowEnderDragonFishing = true;

		HookReelConfig validated = config.validatedCopy();

		assertFalse(validated.allowFishingEntities);
		assertFalse(validated.allowAquaticEntities);
		assertFalse(validated.allowNetherEntities);
		assertTrue(validated.netherEntitiesOnlyInNether);
		assertFalse(validated.allowBossEntities);
		assertTrue(validated.allowEnderDragonFishing);
	}

	@Test
	void defaultsMatchThePublicConfigurationContract() {
		HookReelConfig config = new HookReelConfig();

		assertTrue(config.luckyEnchantmentEnabled);
		assertEquals(1.5D, config.luckyBonusPerLevel);
		assertTrue(config.allowStackWithLuckOfTheSea);
		assertTrue(config.luckyThreeInstantCatchEnabled);
		assertEquals(1.0D, config.luckyThreeInstantCatchDelaySeconds);
		assertTrue(config.luckyThreeAutoRetract);
		assertTrue(config.allowFishingEntities);
		assertEquals(0.05D, config.fishingEntityBaseChance);
		assertEquals(0.03D, config.fishingEntityChanceBonusPerLuckyLevel);
		assertEquals(0.25D, config.maximumFishingEntityChance);
		assertTrue(config.allowAquaticEntities);
		assertFalse(config.allowLandAnimals);
		assertFalse(config.allowLandMonsters);
		assertFalse(config.allowNetherEntities);
		assertFalse(config.netherEntitiesOnlyInNether);
		assertFalse(config.allowBossEntities);
		assertFalse(config.allowEnderDragonFishing);
		assertEquals(70.0D, config.aquaticEntityCategoryWeight);
		assertEquals(20.0D, config.landAnimalCategoryWeight);
		assertEquals(7.0D, config.landMonsterCategoryWeight);
		assertEquals(2.5D, config.netherEntityCategoryWeight);
		assertEquals(0.5D, config.bossEntityCategoryWeight);
		assertEquals(10.0D, config.grapplingHookCooldownSeconds);
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
		assertTrue(config.anchorHookEnabled);
		assertEquals(24.0D, config.anchorLevel1MaxRange);
		assertEquals(36.0D, config.anchorLevel2MaxRange);
		assertEquals(48.0D, config.anchorLevel3MaxRange);
		assertEquals(0.10D, config.reelAcceleration);
		assertEquals(1.60D, config.maximumReelSpeed);
		assertEquals(0.75D, config.reelArrivalDistance);
		assertEquals(5.0D, config.maximumReelUpDurationSeconds);
		assertEquals(0.45D, config.reelTargetVerticalOffset);
		assertEquals(0.15D, config.reelTargetSurfaceOffset);
		assertTrue(config.reelingLateralControlEnabled);
		assertEquals(0.10D, config.reelingLateralControlStrength);
		assertEquals(0.45D, config.maximumReelingLateralSpeed);
		assertEquals(1.75D, config.reelingObstacleBypassMultiplier);
		assertEquals(0.35D, config.reelingCollisionForwardRetention);
		assertEquals(0.60D, config.reelingLateralDetectionDistance);
		assertEquals(2.50D, config.reelingLateralFadeDistance);
		assertEquals(0.40D, config.wallDetectionDistance);
		assertEquals(10.0D, config.wallClingDurationSeconds);
		assertEquals(0.08D, config.wallClingStrength);
		assertEquals(0.18D, config.wallClimbSpeed);
		assertEquals(0.15D, config.wallClimbDownSpeed);
		assertEquals(0.12D, config.wallHorizontalMoveSpeed);
		assertEquals(0.42D, config.wallJumpUpVelocity);
		assertEquals(0.55D, config.wallJumpOutVelocity);
		assertEquals(0.025D, config.swingControlStrength);
		assertEquals(2.2D, config.maximumSwingSpeed);
		assertEquals(0.35D, config.ropeConstraintStrength);
		assertEquals(0.9D, config.ropeDamping);
		assertEquals(0.1D, config.ropeTolerance);
		assertTrue(config.rappelEnabled);
		assertEquals(2.5D, config.rappelSpeed);
		assertEquals(4.0D, config.rappelInitialSlack);
		assertFalse(config.autoDetachOnGround);
		assertTrue(config.showRopeLengthHud);
		assertEquals(1, config.anchorDurabilityCost);
		assertEquals(1.5D, config.anchorHookCooldownSeconds);
		assertEquals(0.25D, config.anchorHookFailedCastDelaySeconds);
		assertTrue(config.showWallClingTimerHud);
	}

	@Test
	void validationClampsRangesAndRestoresNonFiniteNumbers() {
		HookReelConfig config = new HookReelConfig();
		config.luckyBonusPerLevel = Double.NaN;
		config.luckyThreeInstantCatchDelaySeconds = Double.NaN;
		config.fishingEntityBaseChance = -1.0D;
		config.fishingEntityChanceBonusPerLuckyLevel = Double.POSITIVE_INFINITY;
		config.maximumFishingEntityChance = 2.0D;
		config.aquaticEntityCategoryWeight = -1.0D;
		config.landAnimalCategoryWeight = Double.POSITIVE_INFINITY;
		config.landMonsterCategoryWeight = 2000.0D;
		config.netherEntityCategoryWeight = Double.NaN;
		config.bossEntityCategoryWeight = -5.0D;
		config.grapplingHookCooldownSeconds = 999.0D;
		config.maxChargeTimeSeconds = Double.POSITIVE_INFINITY;
		config.anchorHookEnabled = false;
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
		config.anchorLevel1MaxRange = Double.NaN;
		config.anchorLevel2MaxRange = -2.0D;
		config.anchorLevel3MaxRange = 5.0D;
		config.reelAcceleration = Double.NaN;
		config.maximumReelSpeed = -1.0D;
		config.reelArrivalDistance = Double.POSITIVE_INFINITY;
		config.maximumReelUpDurationSeconds = 99.0D;
		config.reelTargetVerticalOffset = -1.0D;
		config.reelTargetSurfaceOffset = 99.0D;
		config.reelingLateralControlStrength = Double.NaN;
		config.maximumReelingLateralSpeed = 99.0D;
		config.reelingObstacleBypassMultiplier = 0.0D;
		config.reelingCollisionForwardRetention = Double.POSITIVE_INFINITY;
		config.reelingLateralDetectionDistance = 0.0D;
		config.reelingLateralFadeDistance = 99.0D;
		config.wallDetectionDistance = Double.NaN;
		config.wallClingDurationSeconds = 0.0D;
		config.wallClingStrength = 99.0D;
		config.wallClimbSpeed = 99.0D;
		config.wallClimbDownSpeed = 99.0D;
		config.wallHorizontalMoveSpeed = -1.0D;
		config.wallJumpUpVelocity = 99.0D;
		config.wallJumpOutVelocity = Double.POSITIVE_INFINITY;
		config.swingControlStrength = Double.NaN;
		config.maximumSwingSpeed = 99.0D;
		config.ropeConstraintStrength = -1.0D;
		config.ropeDamping = 5.0D;
		config.ropeTolerance = Double.POSITIVE_INFINITY;
		config.rappelSpeed = -1.0D;
		config.rappelInitialSlack = 99.0D;
		config.anchorDurabilityCost = 999;
		config.anchorHookCooldownSeconds = 99.0D;
		config.anchorHookFailedCastDelaySeconds = Double.NaN;

		HookReelConfig validated = config.validatedCopy();

		assertEquals(1.5D, validated.luckyBonusPerLevel);
		assertEquals(1.0D, validated.luckyThreeInstantCatchDelaySeconds);
		assertEquals(0.0D, validated.fishingEntityBaseChance);
		assertEquals(0.03D, validated.fishingEntityChanceBonusPerLuckyLevel);
		assertEquals(1.0D, validated.maximumFishingEntityChance);
		assertEquals(0.0D, validated.aquaticEntityCategoryWeight);
		assertEquals(20.0D, validated.landAnimalCategoryWeight);
		assertEquals(1000.0D, validated.landMonsterCategoryWeight);
		assertEquals(2.5D, validated.netherEntityCategoryWeight);
		assertEquals(0.0D, validated.bossEntityCategoryWeight);
		assertEquals(120.0D, validated.grapplingHookCooldownSeconds);
		assertEquals(1.5D, validated.maxChargeTimeSeconds);
		assertFalse(validated.anchorHookEnabled);
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
		assertEquals(24.0D, validated.anchorLevel1MaxRange);
		assertEquals(24.0D, validated.anchorLevel2MaxRange);
		assertEquals(24.0D, validated.anchorLevel3MaxRange);
		assertEquals(0.10D, validated.reelAcceleration);
		assertEquals(0.10D, validated.maximumReelSpeed);
		assertEquals(0.75D, validated.reelArrivalDistance);
		assertEquals(30.0D, validated.maximumReelUpDurationSeconds);
		assertEquals(0.0D, validated.reelTargetVerticalOffset);
		assertEquals(1.0D, validated.reelTargetSurfaceOffset);
		assertEquals(0.10D, validated.reelingLateralControlStrength);
		assertEquals(3.0D, validated.maximumReelingLateralSpeed);
		assertEquals(1.0D, validated.reelingObstacleBypassMultiplier);
		assertEquals(0.35D, validated.reelingCollisionForwardRetention);
		assertEquals(0.1D, validated.reelingLateralDetectionDistance);
		assertEquals(10.0D, validated.reelingLateralFadeDistance);
		assertEquals(0.40D, validated.wallDetectionDistance);
		assertEquals(1.0D, validated.wallClingDurationSeconds);
		assertEquals(0.30D, validated.wallClingStrength);
		assertEquals(0.60D, validated.wallClimbSpeed);
		assertEquals(0.60D, validated.wallClimbDownSpeed);
		assertEquals(0.0D, validated.wallHorizontalMoveSpeed);
		assertEquals(1.50D, validated.wallJumpUpVelocity);
		assertEquals(0.55D, validated.wallJumpOutVelocity);
		assertEquals(0.025D, validated.swingControlStrength);
		assertEquals(5.0D, validated.maximumSwingSpeed);
		assertEquals(0.05D, validated.ropeConstraintStrength);
		assertEquals(1.0D, validated.ropeDamping);
		assertEquals(0.1D, validated.ropeTolerance);
		assertEquals(0.0D, validated.rappelSpeed);
		assertEquals(16.0D, validated.rappelInitialSlack);
		assertEquals(64, validated.anchorDurabilityCost);
		assertEquals(30.0D, validated.anchorHookCooldownSeconds);
		assertEquals(0.25D, validated.anchorHookFailedCastDelaySeconds);

		config.luckyBonusPerLevel = -5.0D;
		config.luckyThreeInstantCatchDelaySeconds = 0.0D;
		config.grapplingHookCooldownSeconds = -1.0D;
		config.anchorHookCooldownSeconds = -1.0D;
		config.anchorHookFailedCastDelaySeconds = 11.0D;
		config.maxChargeTimeSeconds = 99.0D;
		validated = config.validatedCopy();

		assertEquals(0.0D, validated.luckyBonusPerLevel);
		assertEquals(0.25D, validated.luckyThreeInstantCatchDelaySeconds);
		assertEquals(0.0D, validated.grapplingHookCooldownSeconds);
		assertEquals(0.0D, validated.anchorHookCooldownSeconds);
		assertEquals(10.0D, validated.anchorHookFailedCastDelaySeconds);
		assertEquals(10.0D, validated.maxChargeTimeSeconds);

		config.minimumGrappleRange = 64.0D;
		config.anchorLevel1MaxRange = 2.0D;
		config.anchorLevel2MaxRange = 3.0D;
		config.anchorLevel3MaxRange = 4.0D;
		validated = config.validatedCopy();
		assertEquals(2.0D, validated.anchorLevel1MaxRange);
		assertEquals(3.0D, validated.anchorLevel2MaxRange);
		assertEquals(4.0D, validated.anchorLevel3MaxRange);
	}

	@Test
	void nonFiniteCooldownsRestoreDefaultsWhileFiniteValuesClampToPublicRanges() {
		HookReelConfig config = new HookReelConfig();
		config.grapplingHookCooldownSeconds = Double.NaN;
		config.anchorHookCooldownSeconds = Double.POSITIVE_INFINITY;

		HookReelConfig validated = config.validatedCopy();

		assertEquals(10.0D, validated.grapplingHookCooldownSeconds);
		assertEquals(1.5D, validated.anchorHookCooldownSeconds);

		config.grapplingHookCooldownSeconds = 121.0D;
		config.anchorHookCooldownSeconds = 31.0D;
		validated = config.validatedCopy();

		assertEquals(120.0D, validated.grapplingHookCooldownSeconds);
		assertEquals(30.0D, validated.anchorHookCooldownSeconds);
	}
}
