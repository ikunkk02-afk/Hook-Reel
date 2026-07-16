package com.ikunkk02afk.hookandreel.fishing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import org.junit.jupiter.api.Test;

class FishingEntityChanceTest {
	@Test
	void luckyLevelsIncreaseChanceAndRespectSafetyCap() {
		HookReelConfig config = new HookReelConfig();

		assertEquals(0.05D, FishingEntityChance.calculate(config, 0), 0.000001D);
		assertEquals(0.08D, FishingEntityChance.calculate(config, 1), 0.000001D);
		assertEquals(0.11D, FishingEntityChance.calculate(config, 2), 0.000001D);
		assertEquals(0.14D, FishingEntityChance.calculate(config, 3), 0.000001D);

		config.fishingEntityBaseChance = 0.20D;
		assertEquals(0.25D, FishingEntityChance.calculate(config, 3), 0.000001D);
	}

	@Test
	void switchesAndInvalidNumbersFailSafe() {
		HookReelConfig config = new HookReelConfig();
		config.luckyEnchantmentEnabled = false;
		assertEquals(0.05D, FishingEntityChance.calculate(config, 3), 0.000001D);

		config.allowFishingEntities = false;
		assertEquals(0.0D, FishingEntityChance.calculate(config, 3), 0.000001D);

		config.allowFishingEntities = true;
		config.fishingEntityBaseChance = Double.NaN;
		config.maximumFishingEntityChance = Double.POSITIVE_INFINITY;
		assertEquals(0.0D, FishingEntityChance.calculate(config, 0), 0.000001D);
	}
}
