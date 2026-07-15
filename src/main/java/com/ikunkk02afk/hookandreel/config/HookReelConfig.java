package com.ikunkk02afk.hookandreel.config;

public final class HookReelConfig {
	public static final boolean DEFAULT_LUCKY_ENCHANTMENT_ENABLED = true;
	public static final double DEFAULT_LUCKY_BONUS_PER_LEVEL = 1.5D;
	public static final boolean DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA = true;
	public static final boolean DEFAULT_GRAPPLING_HOOK_ENABLED = true;
	public static final int DEFAULT_GRAPPLE_COOLDOWN_SECONDS = 10;
	public static final double DEFAULT_MAX_CHARGE_TIME_SECONDS = 1.5D;
	public static final double DEFAULT_MINIMUM_GRAPPLE_RANGE = 8.0D;
	public static final double DEFAULT_GRAPPLE_LEVEL_1_MAX_RANGE = 24.0D;
	public static final double DEFAULT_GRAPPLE_LEVEL_2_MAX_RANGE = 36.0D;
	public static final double DEFAULT_GRAPPLE_LEVEL_3_MAX_RANGE = 48.0D;
	public static final double DEFAULT_PULL_STRENGTH = 0.12D;
	public static final double DEFAULT_MAXIMUM_PULL_SPEED = 1.5D;
	public static final double DEFAULT_ITEM_PULL_SPEED_MULTIPLIER = 1.6D;
	public static final double DEFAULT_PULL_STOP_DISTANCE = 2.0D;
	public static final double DEFAULT_MAX_PULL_DURATION_SECONDS = 8.0D;
	public static final boolean DEFAULT_ALLOW_PULL_PLAYERS = true;
	public static final boolean DEFAULT_ALLOW_PULL_BOSSES = false;
	public static final boolean DEFAULT_SWING_ENABLED = true;
	public static final boolean DEFAULT_RAPPEL_ENABLED = true;

	public static final double MIN_LUCKY_BONUS_PER_LEVEL = 0.0D;
	public static final double MAX_LUCKY_BONUS_PER_LEVEL = 10.0D;
	public static final int MIN_GRAPPLE_COOLDOWN_SECONDS = 0;
	public static final int MAX_GRAPPLE_COOLDOWN_SECONDS = 300;
	public static final double MIN_MAX_CHARGE_TIME_SECONDS = 0.1D;
	public static final double MAX_MAX_CHARGE_TIME_SECONDS = 10.0D;
	public static final double MIN_GRAPPLE_RANGE = 1.0D;
	public static final double MAX_GRAPPLE_RANGE = 128.0D;
	public static final double MIN_PULL_STRENGTH = 0.0D;
	public static final double MAX_PULL_STRENGTH = 2.0D;
	public static final double MIN_MAXIMUM_PULL_SPEED = 0.0D;
	public static final double MAX_MAXIMUM_PULL_SPEED = 10.0D;
	public static final double MIN_ITEM_PULL_SPEED_MULTIPLIER = 0.0D;
	public static final double MAX_ITEM_PULL_SPEED_MULTIPLIER = 5.0D;
	public static final double MIN_PULL_STOP_DISTANCE = 0.25D;
	public static final double MAX_PULL_STOP_DISTANCE = 8.0D;
	public static final double MIN_MAX_PULL_DURATION_SECONDS = 0.5D;
	public static final double MAX_MAX_PULL_DURATION_SECONDS = 60.0D;

	public boolean luckyEnchantmentEnabled = DEFAULT_LUCKY_ENCHANTMENT_ENABLED;
	public double luckyBonusPerLevel = DEFAULT_LUCKY_BONUS_PER_LEVEL;
	public boolean allowStackWithLuckOfTheSea = DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA;
	public boolean grapplingHookEnabled = DEFAULT_GRAPPLING_HOOK_ENABLED;
	public int grappleCooldownSeconds = DEFAULT_GRAPPLE_COOLDOWN_SECONDS;
	public double maxChargeTimeSeconds = DEFAULT_MAX_CHARGE_TIME_SECONDS;
	public double minimumGrappleRange = DEFAULT_MINIMUM_GRAPPLE_RANGE;
	public double grappleLevel1MaxRange = DEFAULT_GRAPPLE_LEVEL_1_MAX_RANGE;
	public double grappleLevel2MaxRange = DEFAULT_GRAPPLE_LEVEL_2_MAX_RANGE;
	public double grappleLevel3MaxRange = DEFAULT_GRAPPLE_LEVEL_3_MAX_RANGE;
	public double pullStrength = DEFAULT_PULL_STRENGTH;
	public double maximumPullSpeed = DEFAULT_MAXIMUM_PULL_SPEED;
	public double itemPullSpeedMultiplier = DEFAULT_ITEM_PULL_SPEED_MULTIPLIER;
	public double pullStopDistance = DEFAULT_PULL_STOP_DISTANCE;
	public double maxPullDurationSeconds = DEFAULT_MAX_PULL_DURATION_SECONDS;
	public boolean allowPullPlayers = DEFAULT_ALLOW_PULL_PLAYERS;
	public boolean allowPullBosses = DEFAULT_ALLOW_PULL_BOSSES;
	public boolean swingEnabled = DEFAULT_SWING_ENABLED;
	public boolean rappelEnabled = DEFAULT_RAPPEL_ENABLED;

	public HookReelConfig copy() {
		HookReelConfig copy = new HookReelConfig();
		copy.luckyEnchantmentEnabled = luckyEnchantmentEnabled;
		copy.luckyBonusPerLevel = luckyBonusPerLevel;
		copy.allowStackWithLuckOfTheSea = allowStackWithLuckOfTheSea;
		copy.grapplingHookEnabled = grapplingHookEnabled;
		copy.grappleCooldownSeconds = grappleCooldownSeconds;
		copy.maxChargeTimeSeconds = maxChargeTimeSeconds;
		copy.minimumGrappleRange = minimumGrappleRange;
		copy.grappleLevel1MaxRange = grappleLevel1MaxRange;
		copy.grappleLevel2MaxRange = grappleLevel2MaxRange;
		copy.grappleLevel3MaxRange = grappleLevel3MaxRange;
		copy.pullStrength = pullStrength;
		copy.maximumPullSpeed = maximumPullSpeed;
		copy.itemPullSpeedMultiplier = itemPullSpeedMultiplier;
		copy.pullStopDistance = pullStopDistance;
		copy.maxPullDurationSeconds = maxPullDurationSeconds;
		copy.allowPullPlayers = allowPullPlayers;
		copy.allowPullBosses = allowPullBosses;
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
		validated.minimumGrappleRange = validatedFiniteClamped(
			validated.minimumGrappleRange,
			DEFAULT_MINIMUM_GRAPPLE_RANGE,
			MIN_GRAPPLE_RANGE,
			MAX_GRAPPLE_RANGE
		);
		validated.grappleLevel1MaxRange = Math.max(
			validated.minimumGrappleRange,
			validatedFiniteClamped(validated.grappleLevel1MaxRange, DEFAULT_GRAPPLE_LEVEL_1_MAX_RANGE, MIN_GRAPPLE_RANGE, MAX_GRAPPLE_RANGE)
		);
		validated.grappleLevel2MaxRange = Math.max(
			validated.grappleLevel1MaxRange,
			validatedFiniteClamped(validated.grappleLevel2MaxRange, DEFAULT_GRAPPLE_LEVEL_2_MAX_RANGE, MIN_GRAPPLE_RANGE, MAX_GRAPPLE_RANGE)
		);
		validated.grappleLevel3MaxRange = Math.max(
			validated.grappleLevel2MaxRange,
			validatedFiniteClamped(validated.grappleLevel3MaxRange, DEFAULT_GRAPPLE_LEVEL_3_MAX_RANGE, MIN_GRAPPLE_RANGE, MAX_GRAPPLE_RANGE)
		);
		validated.pullStrength = validatedFiniteClamped(validated.pullStrength, DEFAULT_PULL_STRENGTH, MIN_PULL_STRENGTH, MAX_PULL_STRENGTH);
		validated.maximumPullSpeed = validatedFiniteClamped(validated.maximumPullSpeed, DEFAULT_MAXIMUM_PULL_SPEED, MIN_MAXIMUM_PULL_SPEED, MAX_MAXIMUM_PULL_SPEED);
		validated.itemPullSpeedMultiplier = validatedFiniteClamped(validated.itemPullSpeedMultiplier, DEFAULT_ITEM_PULL_SPEED_MULTIPLIER, MIN_ITEM_PULL_SPEED_MULTIPLIER, MAX_ITEM_PULL_SPEED_MULTIPLIER);
		validated.pullStopDistance = validatedFiniteClamped(validated.pullStopDistance, DEFAULT_PULL_STOP_DISTANCE, MIN_PULL_STOP_DISTANCE, MAX_PULL_STOP_DISTANCE);
		validated.maxPullDurationSeconds = validatedFiniteClamped(validated.maxPullDurationSeconds, DEFAULT_MAX_PULL_DURATION_SECONDS, MIN_MAX_PULL_DURATION_SECONDS, MAX_MAX_PULL_DURATION_SECONDS);
		return validated;
	}

	private static double validatedFiniteClamped(double value, double defaultValue, double min, double max) {
		return clamp(finiteOrDefault(value, defaultValue), min, max);
	}

	private static double finiteOrDefault(double value, double defaultValue) {
		return Double.isFinite(value) ? value : defaultValue;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
