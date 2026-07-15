package com.ikunkk02afk.hookandreel.config;

public final class HookReelConfig {
	public static final boolean DEFAULT_LUCKY_ENCHANTMENT_ENABLED = true;
	public static final double DEFAULT_LUCKY_BONUS_PER_LEVEL = 1.5D;
	public static final boolean DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA = true;
	public static final int DEFAULT_GRAPPLE_COOLDOWN_SECONDS = 10;
	public static final double DEFAULT_MAX_CHARGE_TIME_SECONDS = 1.5D;
	public static final boolean DEFAULT_SWING_ENABLED = true;
	public static final boolean DEFAULT_RAPPEL_ENABLED = true;

	public static final double MIN_LUCKY_BONUS_PER_LEVEL = 0.0D;
	public static final double MAX_LUCKY_BONUS_PER_LEVEL = 10.0D;
	public static final int MIN_GRAPPLE_COOLDOWN_SECONDS = 0;
	public static final int MAX_GRAPPLE_COOLDOWN_SECONDS = 300;
	public static final double MIN_MAX_CHARGE_TIME_SECONDS = 0.1D;
	public static final double MAX_MAX_CHARGE_TIME_SECONDS = 10.0D;

	public boolean luckyEnchantmentEnabled = DEFAULT_LUCKY_ENCHANTMENT_ENABLED;
	public double luckyBonusPerLevel = DEFAULT_LUCKY_BONUS_PER_LEVEL;
	public boolean allowStackWithLuckOfTheSea = DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA;
	public int grappleCooldownSeconds = DEFAULT_GRAPPLE_COOLDOWN_SECONDS;
	public double maxChargeTimeSeconds = DEFAULT_MAX_CHARGE_TIME_SECONDS;
	public boolean swingEnabled = DEFAULT_SWING_ENABLED;
	public boolean rappelEnabled = DEFAULT_RAPPEL_ENABLED;

	public HookReelConfig copy() {
		HookReelConfig copy = new HookReelConfig();
		copy.luckyEnchantmentEnabled = luckyEnchantmentEnabled;
		copy.luckyBonusPerLevel = luckyBonusPerLevel;
		copy.allowStackWithLuckOfTheSea = allowStackWithLuckOfTheSea;
		copy.grappleCooldownSeconds = grappleCooldownSeconds;
		copy.maxChargeTimeSeconds = maxChargeTimeSeconds;
		copy.swingEnabled = swingEnabled;
		copy.rappelEnabled = rappelEnabled;
		return copy;
	}

	public HookReelConfig validatedCopy() {
		HookReelConfig validated = copy();
		validated.luckyBonusPerLevel = finiteOrDefault(
			validated.luckyBonusPerLevel,
			DEFAULT_LUCKY_BONUS_PER_LEVEL
		);
		validated.luckyBonusPerLevel = clamp(
			validated.luckyBonusPerLevel,
			MIN_LUCKY_BONUS_PER_LEVEL,
			MAX_LUCKY_BONUS_PER_LEVEL
		);
		validated.grappleCooldownSeconds = Math.clamp(
			validated.grappleCooldownSeconds,
			MIN_GRAPPLE_COOLDOWN_SECONDS,
			MAX_GRAPPLE_COOLDOWN_SECONDS
		);
		validated.maxChargeTimeSeconds = finiteOrDefault(
			validated.maxChargeTimeSeconds,
			DEFAULT_MAX_CHARGE_TIME_SECONDS
		);
		validated.maxChargeTimeSeconds = clamp(
			validated.maxChargeTimeSeconds,
			MIN_MAX_CHARGE_TIME_SECONDS,
			MAX_MAX_CHARGE_TIME_SECONDS
		);
		return validated;
	}

	private static double finiteOrDefault(double value, double defaultValue) {
		return Double.isFinite(value) ? value : defaultValue;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
