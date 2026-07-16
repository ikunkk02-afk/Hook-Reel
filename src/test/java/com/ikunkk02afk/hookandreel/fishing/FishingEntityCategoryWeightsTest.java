package com.ikunkk02afk.hookandreel.fishing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import org.junit.jupiter.api.Test;

class FishingEntityCategoryWeightsTest {
	@Test
	void weightedChoiceUsesCategoryWeightsInsteadOfEntityCounts() {
		double[] aquaticAndBoss = {70.0D, 0.5D};

		assertEquals(0, FishingEntityCategoryWeights.selectIndex(aquaticAndBoss, 0.0D));
		assertEquals(0, FishingEntityCategoryWeights.selectIndex(aquaticAndBoss, 69.999D / 70.5D));
		assertEquals(1, FishingEntityCategoryWeights.selectIndex(aquaticAndBoss, 70.0D / 70.5D));
		assertEquals(1, FishingEntityCategoryWeights.selectIndex(aquaticAndBoss, 0.999999D));
	}

	@Test
	void zeroAndNonFiniteWeightsNeverDivideByZeroOrParticipate() {
		assertEquals(-1, FishingEntityCategoryWeights.selectIndex(
			new double[] {0.0D, -1.0D, Double.NaN, Double.POSITIVE_INFINITY},
			0.5D
		));
		assertEquals(2, FishingEntityCategoryWeights.selectIndex(
			new double[] {0.0D, Double.NaN, 7.0D, -2.0D},
			0.5D
		));
	}

	@Test
	void categoryWeightsDoNotChangeOverallEntityChance() {
		HookReelConfig config = new HookReelConfig();
		config.fishingEntityBaseChance = 0.20D;
		config.fishingEntityChanceBonusPerLuckyLevel = 0.10D;
		config.maximumFishingEntityChance = 0.60D;
		double before = FishingEntityChance.calculate(config, 3);

		config.aquaticEntityCategoryWeight = 0.0D;
		config.landAnimalCategoryWeight = 1000.0D;
		config.landMonsterCategoryWeight = 999.0D;
		config.netherEntityCategoryWeight = 500.0D;
		config.bossEntityCategoryWeight = 1000.0D;

		assertEquals(before, FishingEntityChance.calculate(config, 3));
	}
}
