package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;

public final class FishingEntityChance {
	private FishingEntityChance() {
	}

	public static double calculate(HookReelConfig config, int luckyLevel) {
		if (!config.allowFishingEntities) {
			return 0.0D;
		}
		int effectiveLuckyLevel = config.luckyEnchantmentEnabled ? Math.max(0, luckyLevel) : 0;
		double base = finiteOrZero(config.fishingEntityBaseChance);
		double bonus = finiteOrZero(config.fishingEntityChanceBonusPerLuckyLevel);
		double maximum = Math.clamp(finiteOrZero(config.maximumFishingEntityChance), 0.0D, 1.0D);
		double calculated = Math.clamp(base + effectiveLuckyLevel * bonus, 0.0D, 1.0D);
		return Math.min(maximum, calculated);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
