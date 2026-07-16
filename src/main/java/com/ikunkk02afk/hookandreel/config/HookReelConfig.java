package com.ikunkk02afk.hookandreel.config;

import com.google.gson.annotations.SerializedName;

public final class HookReelConfig {
	public static final boolean DEFAULT_LUCKY_ENCHANTMENT_ENABLED = true;
	public static final double DEFAULT_LUCKY_BONUS_PER_LEVEL = 1.5D;
	public static final boolean DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA = true;
	public static final boolean DEFAULT_LUCKY_THREE_INSTANT_CATCH_ENABLED = true;
	public static final double DEFAULT_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS = 1.0D;
	public static final boolean DEFAULT_LUCKY_THREE_AUTO_RETRACT = true;
	public static final boolean DEFAULT_ALLOW_FISHING_ENTITIES = true;
	public static final double DEFAULT_FISHING_ENTITY_BASE_CHANCE = 0.05D;
	public static final double DEFAULT_FISHING_ENTITY_CHANCE_BONUS_PER_LUCKY_LEVEL = 0.03D;
	public static final double DEFAULT_MAXIMUM_FISHING_ENTITY_CHANCE = 0.25D;
	public static final boolean DEFAULT_ALLOW_AQUATIC_ENTITIES = true;
	public static final boolean DEFAULT_ALLOW_LAND_ANIMALS = false;
	public static final boolean DEFAULT_ALLOW_LAND_MONSTERS = false;
	public static final boolean DEFAULT_ALLOW_NETHER_ENTITIES = false;
	public static final boolean DEFAULT_NETHER_ENTITIES_ONLY_IN_NETHER = false;
	public static final boolean DEFAULT_ALLOW_BOSS_ENTITIES = false;
	public static final boolean DEFAULT_ALLOW_ENDER_DRAGON_FISHING = false;
	public static final double DEFAULT_AQUATIC_ENTITY_CATEGORY_WEIGHT = 70.0D;
	public static final double DEFAULT_LAND_ANIMAL_CATEGORY_WEIGHT = 20.0D;
	public static final double DEFAULT_LAND_MONSTER_CATEGORY_WEIGHT = 7.0D;
	public static final double DEFAULT_NETHER_ENTITY_CATEGORY_WEIGHT = 2.5D;
	public static final double DEFAULT_BOSS_ENTITY_CATEGORY_WEIGHT = 0.5D;
	public static final boolean DEFAULT_GRAPPLING_HOOK_ENABLED = true;
	public static final double DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS = 10.0D;
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
	public static final boolean DEFAULT_BLOCK_PULLING_ENABLED = true;
	public static final boolean DEFAULT_ALLOW_PULL_BLOCK_ENTITIES = false;
	public static final double DEFAULT_MAXIMUM_BLOCK_HARDNESS = 50.0D;
	public static final double DEFAULT_BLOCK_PULL_SPEED_MULTIPLIER = 0.8D;
	public static final double DEFAULT_MAX_BLOCK_PULL_DURATION_SECONDS = 10.0D;
	public static final double DEFAULT_BLOCK_PULL_STOP_DISTANCE = 2.5D;
	public static final int DEFAULT_BLOCK_PULL_DURABILITY_COST = 3;
	public static final boolean DEFAULT_ANCHOR_HOOK_ENABLED = true;
	public static final double DEFAULT_ANCHOR_LEVEL_1_MAX_RANGE = 24.0D;
	public static final double DEFAULT_ANCHOR_LEVEL_2_MAX_RANGE = 36.0D;
	public static final double DEFAULT_ANCHOR_LEVEL_3_MAX_RANGE = 48.0D;
	public static final double DEFAULT_REEL_ACCELERATION = 0.10D;
	public static final double DEFAULT_MAXIMUM_REEL_SPEED = 1.60D;
	public static final double DEFAULT_REEL_ARRIVAL_DISTANCE = 0.75D;
	public static final double DEFAULT_MAXIMUM_REEL_UP_DURATION_SECONDS = 5.0D;
	public static final double DEFAULT_REEL_TARGET_VERTICAL_OFFSET = 0.45D;
	public static final double DEFAULT_REEL_TARGET_SURFACE_OFFSET = 0.15D;
	public static final boolean DEFAULT_REELING_LATERAL_CONTROL_ENABLED = true;
	public static final double DEFAULT_REELING_LATERAL_CONTROL_STRENGTH = 0.10D;
	public static final double DEFAULT_MAXIMUM_REELING_LATERAL_SPEED = 0.45D;
	public static final double DEFAULT_REELING_OBSTACLE_BYPASS_MULTIPLIER = 1.75D;
	public static final double DEFAULT_REELING_COLLISION_FORWARD_RETENTION = 0.35D;
	public static final double DEFAULT_REELING_LATERAL_DETECTION_DISTANCE = 0.60D;
	public static final double DEFAULT_REELING_LATERAL_FADE_DISTANCE = 2.50D;
	public static final double DEFAULT_WALL_DETECTION_DISTANCE = 0.40D;
	public static final double DEFAULT_WALL_CLING_DURATION_SECONDS = 10.0D;
	public static final double DEFAULT_WALL_CLING_STRENGTH = 0.08D;
	public static final double DEFAULT_WALL_CLIMB_SPEED = 0.18D;
	public static final double DEFAULT_WALL_CLIMB_DOWN_SPEED = 0.15D;
	public static final double DEFAULT_WALL_HORIZONTAL_MOVE_SPEED = 0.12D;
	public static final double DEFAULT_WALL_JUMP_UP_VELOCITY = 0.42D;
	public static final double DEFAULT_WALL_JUMP_OUT_VELOCITY = 0.55D;
	public static final double DEFAULT_SWING_CONTROL_STRENGTH = 0.025D;
	public static final double DEFAULT_MAXIMUM_SWING_SPEED = 2.2D;
	public static final double DEFAULT_ROPE_CONSTRAINT_STRENGTH = 0.35D;
	public static final double DEFAULT_ROPE_DAMPING = 0.9D;
	public static final double DEFAULT_ROPE_TOLERANCE = 0.1D;
	public static final boolean DEFAULT_RAPPEL_ENABLED = true;
	public static final double DEFAULT_RAPPEL_SPEED = 2.5D;
	public static final double DEFAULT_RAPPEL_INITIAL_SLACK = 4.0D;
	public static final boolean DEFAULT_AUTO_DETACH_ON_GROUND = false;
	public static final boolean DEFAULT_SHOW_ROPE_LENGTH_HUD = true;
	public static final int DEFAULT_ANCHOR_DURABILITY_COST = 1;
	public static final double DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS = 1.5D;
	public static final double DEFAULT_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS = 0.25D;
	public static final boolean DEFAULT_SHOW_WALL_CLING_TIMER_HUD = true;

	public static final double MIN_LUCKY_BONUS_PER_LEVEL = 0.0D;
	public static final double MAX_LUCKY_BONUS_PER_LEVEL = 10.0D;
	public static final double MIN_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS = 0.25D;
	public static final double MAX_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS = 30.0D;
	public static final double MIN_FISHING_ENTITY_CHANCE = 0.0D;
	public static final double MAX_FISHING_ENTITY_CHANCE = 1.0D;
	public static final double MIN_FISHING_ENTITY_CATEGORY_WEIGHT = 0.0D;
	public static final double MAX_FISHING_ENTITY_CATEGORY_WEIGHT = 1000.0D;
	public static final double MIN_GRAPPLING_HOOK_COOLDOWN_SECONDS = 0.0D;
	public static final double MAX_GRAPPLING_HOOK_COOLDOWN_SECONDS = 120.0D;
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
	public static final double MIN_MAXIMUM_BLOCK_HARDNESS = 0.0D;
	public static final double MAX_MAXIMUM_BLOCK_HARDNESS = 1000.0D;
	public static final double MIN_BLOCK_PULL_SPEED_MULTIPLIER = 0.0D;
	public static final double MAX_BLOCK_PULL_SPEED_MULTIPLIER = 5.0D;
	public static final double MIN_MAX_BLOCK_PULL_DURATION_SECONDS = 0.5D;
	public static final double MAX_MAX_BLOCK_PULL_DURATION_SECONDS = 60.0D;
	public static final double MIN_BLOCK_PULL_STOP_DISTANCE = 0.5D;
	public static final double MAX_BLOCK_PULL_STOP_DISTANCE = 8.0D;
	public static final int MIN_BLOCK_PULL_DURABILITY_COST = 0;
	public static final int MAX_BLOCK_PULL_DURABILITY_COST = 64;
	public static final double MIN_REEL_ACCELERATION = 0.01D;
	public static final double MAX_REEL_ACCELERATION = 0.50D;
	public static final double MIN_MAXIMUM_REEL_SPEED = 0.10D;
	public static final double MAX_MAXIMUM_REEL_SPEED = 5.0D;
	public static final double MIN_REEL_ARRIVAL_DISTANCE = 0.25D;
	public static final double MAX_REEL_ARRIVAL_DISTANCE = 3.0D;
	public static final double MIN_MAXIMUM_REEL_UP_DURATION_SECONDS = 0.5D;
	public static final double MAX_MAXIMUM_REEL_UP_DURATION_SECONDS = 30.0D;
	public static final double MIN_REEL_TARGET_VERTICAL_OFFSET = 0.0D;
	public static final double MAX_REEL_TARGET_VERTICAL_OFFSET = 2.0D;
	public static final double MIN_REEL_TARGET_SURFACE_OFFSET = 0.02D;
	public static final double MAX_REEL_TARGET_SURFACE_OFFSET = 1.0D;
	public static final double MIN_REELING_LATERAL_CONTROL_STRENGTH = 0.0D;
	public static final double MAX_REELING_LATERAL_CONTROL_STRENGTH = 1.0D;
	public static final double MIN_MAXIMUM_REELING_LATERAL_SPEED = 0.0D;
	public static final double MAX_MAXIMUM_REELING_LATERAL_SPEED = 3.0D;
	public static final double MIN_REELING_OBSTACLE_BYPASS_MULTIPLIER = 1.0D;
	public static final double MAX_REELING_OBSTACLE_BYPASS_MULTIPLIER = 5.0D;
	public static final double MIN_REELING_COLLISION_FORWARD_RETENTION = 0.0D;
	public static final double MAX_REELING_COLLISION_FORWARD_RETENTION = 1.0D;
	public static final double MIN_REELING_LATERAL_DETECTION_DISTANCE = 0.1D;
	public static final double MAX_REELING_LATERAL_DETECTION_DISTANCE = 2.0D;
	public static final double MIN_REELING_LATERAL_FADE_DISTANCE = 0.0D;
	public static final double MAX_REELING_LATERAL_FADE_DISTANCE = 10.0D;
	public static final double MIN_WALL_DETECTION_DISTANCE = 0.10D;
	public static final double MAX_WALL_DETECTION_DISTANCE = 0.75D;
	public static final double MIN_WALL_CLING_DURATION_SECONDS = 1.0D;
	public static final double MAX_WALL_CLING_DURATION_SECONDS = 60.0D;
	public static final double MIN_WALL_CLING_STRENGTH = 0.01D;
	public static final double MAX_WALL_CLING_STRENGTH = 0.30D;
	public static final double MIN_WALL_CLIMB_SPEED = 0.0D;
	public static final double MAX_WALL_CLIMB_SPEED = 0.60D;
	public static final double MIN_WALL_CLIMB_DOWN_SPEED = 0.0D;
	public static final double MAX_WALL_CLIMB_DOWN_SPEED = 0.60D;
	public static final double MIN_WALL_HORIZONTAL_MOVE_SPEED = 0.0D;
	public static final double MAX_WALL_HORIZONTAL_MOVE_SPEED = 0.50D;
	public static final double MIN_WALL_JUMP_UP_VELOCITY = 0.0D;
	public static final double MAX_WALL_JUMP_UP_VELOCITY = 1.50D;
	public static final double MIN_WALL_JUMP_OUT_VELOCITY = 0.0D;
	public static final double MAX_WALL_JUMP_OUT_VELOCITY = 1.50D;
	public static final double MIN_SWING_CONTROL_STRENGTH = 0.0D;
	public static final double MAX_SWING_CONTROL_STRENGTH = 0.2D;
	public static final double MIN_MAXIMUM_SWING_SPEED = 0.1D;
	public static final double MAX_MAXIMUM_SWING_SPEED = 5.0D;
	public static final double MIN_ROPE_CONSTRAINT_STRENGTH = 0.05D;
	public static final double MAX_ROPE_CONSTRAINT_STRENGTH = 2.0D;
	public static final double MIN_ROPE_DAMPING = 0.0D;
	public static final double MAX_ROPE_DAMPING = 1.0D;
	public static final double MIN_ROPE_TOLERANCE = 0.0D;
	public static final double MAX_ROPE_TOLERANCE = 1.0D;
	public static final double MIN_RAPPEL_SPEED = 0.0D;
	public static final double MAX_RAPPEL_SPEED = 20.0D;
	public static final double MIN_RAPPEL_INITIAL_SLACK = 0.0D;
	public static final double MAX_RAPPEL_INITIAL_SLACK = 16.0D;
	public static final int MIN_ANCHOR_DURABILITY_COST = 0;
	public static final int MAX_ANCHOR_DURABILITY_COST = 64;
	public static final double MIN_ANCHOR_HOOK_COOLDOWN_SECONDS = 0.0D;
	public static final double MAX_ANCHOR_HOOK_COOLDOWN_SECONDS = 30.0D;
	public static final double MIN_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS = 0.0D;
	public static final double MAX_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS = 10.0D;

	public boolean luckyEnchantmentEnabled = DEFAULT_LUCKY_ENCHANTMENT_ENABLED;
	public double luckyBonusPerLevel = DEFAULT_LUCKY_BONUS_PER_LEVEL;
	public boolean allowStackWithLuckOfTheSea = DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA;
	public boolean luckyThreeInstantCatchEnabled = DEFAULT_LUCKY_THREE_INSTANT_CATCH_ENABLED;
	public double luckyThreeInstantCatchDelaySeconds = DEFAULT_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS;
	public boolean luckyThreeAutoRetract = DEFAULT_LUCKY_THREE_AUTO_RETRACT;
	public boolean allowFishingEntities = DEFAULT_ALLOW_FISHING_ENTITIES;
	public double fishingEntityBaseChance = DEFAULT_FISHING_ENTITY_BASE_CHANCE;
	public double fishingEntityChanceBonusPerLuckyLevel = DEFAULT_FISHING_ENTITY_CHANCE_BONUS_PER_LUCKY_LEVEL;
	public double maximumFishingEntityChance = DEFAULT_MAXIMUM_FISHING_ENTITY_CHANCE;
	public boolean allowAquaticEntities = DEFAULT_ALLOW_AQUATIC_ENTITIES;
	public boolean allowLandAnimals = DEFAULT_ALLOW_LAND_ANIMALS;
	public boolean allowLandMonsters = DEFAULT_ALLOW_LAND_MONSTERS;
	public boolean allowNetherEntities = DEFAULT_ALLOW_NETHER_ENTITIES;
	public boolean netherEntitiesOnlyInNether = DEFAULT_NETHER_ENTITIES_ONLY_IN_NETHER;
	public boolean allowBossEntities = DEFAULT_ALLOW_BOSS_ENTITIES;
	public boolean allowEnderDragonFishing = DEFAULT_ALLOW_ENDER_DRAGON_FISHING;
	public double aquaticEntityCategoryWeight = DEFAULT_AQUATIC_ENTITY_CATEGORY_WEIGHT;
	public double landAnimalCategoryWeight = DEFAULT_LAND_ANIMAL_CATEGORY_WEIGHT;
	public double landMonsterCategoryWeight = DEFAULT_LAND_MONSTER_CATEGORY_WEIGHT;
	public double netherEntityCategoryWeight = DEFAULT_NETHER_ENTITY_CATEGORY_WEIGHT;
	public double bossEntityCategoryWeight = DEFAULT_BOSS_ENTITY_CATEGORY_WEIGHT;
	public boolean grapplingHookEnabled = DEFAULT_GRAPPLING_HOOK_ENABLED;
	@SerializedName(value = "grapplingHookCooldownSeconds", alternate = "grappleCooldownSeconds")
	public double grapplingHookCooldownSeconds = DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS;
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
	public boolean blockPullingEnabled = DEFAULT_BLOCK_PULLING_ENABLED;
	public boolean allowPullBlockEntities = DEFAULT_ALLOW_PULL_BLOCK_ENTITIES;
	public double maximumBlockHardness = DEFAULT_MAXIMUM_BLOCK_HARDNESS;
	public double blockPullSpeedMultiplier = DEFAULT_BLOCK_PULL_SPEED_MULTIPLIER;
	public double maxBlockPullDurationSeconds = DEFAULT_MAX_BLOCK_PULL_DURATION_SECONDS;
	public double blockPullStopDistance = DEFAULT_BLOCK_PULL_STOP_DISTANCE;
	public int blockPullDurabilityCost = DEFAULT_BLOCK_PULL_DURABILITY_COST;
	public boolean anchorHookEnabled = DEFAULT_ANCHOR_HOOK_ENABLED;
	public double anchorLevel1MaxRange = DEFAULT_ANCHOR_LEVEL_1_MAX_RANGE;
	public double anchorLevel2MaxRange = DEFAULT_ANCHOR_LEVEL_2_MAX_RANGE;
	public double anchorLevel3MaxRange = DEFAULT_ANCHOR_LEVEL_3_MAX_RANGE;
	public double reelAcceleration = DEFAULT_REEL_ACCELERATION;
	public double maximumReelSpeed = DEFAULT_MAXIMUM_REEL_SPEED;
	public double reelArrivalDistance = DEFAULT_REEL_ARRIVAL_DISTANCE;
	public double maximumReelUpDurationSeconds = DEFAULT_MAXIMUM_REEL_UP_DURATION_SECONDS;
	public double reelTargetVerticalOffset = DEFAULT_REEL_TARGET_VERTICAL_OFFSET;
	public double reelTargetSurfaceOffset = DEFAULT_REEL_TARGET_SURFACE_OFFSET;
	public boolean reelingLateralControlEnabled = DEFAULT_REELING_LATERAL_CONTROL_ENABLED;
	public double reelingLateralControlStrength = DEFAULT_REELING_LATERAL_CONTROL_STRENGTH;
	public double maximumReelingLateralSpeed = DEFAULT_MAXIMUM_REELING_LATERAL_SPEED;
	public double reelingObstacleBypassMultiplier = DEFAULT_REELING_OBSTACLE_BYPASS_MULTIPLIER;
	public double reelingCollisionForwardRetention = DEFAULT_REELING_COLLISION_FORWARD_RETENTION;
	public double reelingLateralDetectionDistance = DEFAULT_REELING_LATERAL_DETECTION_DISTANCE;
	public double reelingLateralFadeDistance = DEFAULT_REELING_LATERAL_FADE_DISTANCE;
	public double wallDetectionDistance = DEFAULT_WALL_DETECTION_DISTANCE;
	public double wallClingDurationSeconds = DEFAULT_WALL_CLING_DURATION_SECONDS;
	public double wallClingStrength = DEFAULT_WALL_CLING_STRENGTH;
	public double wallClimbSpeed = DEFAULT_WALL_CLIMB_SPEED;
	public double wallClimbDownSpeed = DEFAULT_WALL_CLIMB_DOWN_SPEED;
	public double wallHorizontalMoveSpeed = DEFAULT_WALL_HORIZONTAL_MOVE_SPEED;
	public double wallJumpUpVelocity = DEFAULT_WALL_JUMP_UP_VELOCITY;
	public double wallJumpOutVelocity = DEFAULT_WALL_JUMP_OUT_VELOCITY;
	public double swingControlStrength = DEFAULT_SWING_CONTROL_STRENGTH;
	public double maximumSwingSpeed = DEFAULT_MAXIMUM_SWING_SPEED;
	public double ropeConstraintStrength = DEFAULT_ROPE_CONSTRAINT_STRENGTH;
	public double ropeDamping = DEFAULT_ROPE_DAMPING;
	public double ropeTolerance = DEFAULT_ROPE_TOLERANCE;
	public boolean rappelEnabled = DEFAULT_RAPPEL_ENABLED;
	public double rappelSpeed = DEFAULT_RAPPEL_SPEED;
	public double rappelInitialSlack = DEFAULT_RAPPEL_INITIAL_SLACK;
	public boolean autoDetachOnGround = DEFAULT_AUTO_DETACH_ON_GROUND;
	public boolean showRopeLengthHud = DEFAULT_SHOW_ROPE_LENGTH_HUD;
	public int anchorDurabilityCost = DEFAULT_ANCHOR_DURABILITY_COST;
	public double anchorHookCooldownSeconds = DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS;
	@SerializedName(value = "anchorHookFailedCastDelaySeconds", alternate = "swingRecastDelaySeconds")
	public double anchorHookFailedCastDelaySeconds = DEFAULT_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS;
	@SerializedName(value = "showWallClingTimerHud", alternate = "showClimbTimerHud")
	public boolean showWallClingTimerHud = DEFAULT_SHOW_WALL_CLING_TIMER_HUD;

	public HookReelConfig copy() {
		HookReelConfig copy = new HookReelConfig();
		copy.luckyEnchantmentEnabled = luckyEnchantmentEnabled;
		copy.luckyBonusPerLevel = luckyBonusPerLevel;
		copy.allowStackWithLuckOfTheSea = allowStackWithLuckOfTheSea;
		copy.luckyThreeInstantCatchEnabled = luckyThreeInstantCatchEnabled;
		copy.luckyThreeInstantCatchDelaySeconds = luckyThreeInstantCatchDelaySeconds;
		copy.luckyThreeAutoRetract = luckyThreeAutoRetract;
		copy.allowFishingEntities = allowFishingEntities;
		copy.fishingEntityBaseChance = fishingEntityBaseChance;
		copy.fishingEntityChanceBonusPerLuckyLevel = fishingEntityChanceBonusPerLuckyLevel;
		copy.maximumFishingEntityChance = maximumFishingEntityChance;
		copy.allowAquaticEntities = allowAquaticEntities;
		copy.allowLandAnimals = allowLandAnimals;
		copy.allowLandMonsters = allowLandMonsters;
		copy.allowNetherEntities = allowNetherEntities;
		copy.netherEntitiesOnlyInNether = netherEntitiesOnlyInNether;
		copy.allowBossEntities = allowBossEntities;
		copy.allowEnderDragonFishing = allowEnderDragonFishing;
		copy.aquaticEntityCategoryWeight = aquaticEntityCategoryWeight;
		copy.landAnimalCategoryWeight = landAnimalCategoryWeight;
		copy.landMonsterCategoryWeight = landMonsterCategoryWeight;
		copy.netherEntityCategoryWeight = netherEntityCategoryWeight;
		copy.bossEntityCategoryWeight = bossEntityCategoryWeight;
		copy.grapplingHookEnabled = grapplingHookEnabled;
		copy.grapplingHookCooldownSeconds = grapplingHookCooldownSeconds;
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
		copy.blockPullingEnabled = blockPullingEnabled;
		copy.allowPullBlockEntities = allowPullBlockEntities;
		copy.maximumBlockHardness = maximumBlockHardness;
		copy.blockPullSpeedMultiplier = blockPullSpeedMultiplier;
		copy.maxBlockPullDurationSeconds = maxBlockPullDurationSeconds;
		copy.blockPullStopDistance = blockPullStopDistance;
		copy.blockPullDurabilityCost = blockPullDurabilityCost;
		copy.anchorHookEnabled = anchorHookEnabled;
		copy.anchorLevel1MaxRange = anchorLevel1MaxRange;
		copy.anchorLevel2MaxRange = anchorLevel2MaxRange;
		copy.anchorLevel3MaxRange = anchorLevel3MaxRange;
		copy.reelAcceleration = reelAcceleration;
		copy.maximumReelSpeed = maximumReelSpeed;
		copy.reelArrivalDistance = reelArrivalDistance;
		copy.maximumReelUpDurationSeconds = maximumReelUpDurationSeconds;
		copy.reelTargetVerticalOffset = reelTargetVerticalOffset;
		copy.reelTargetSurfaceOffset = reelTargetSurfaceOffset;
		copy.reelingLateralControlEnabled = reelingLateralControlEnabled;
		copy.reelingLateralControlStrength = reelingLateralControlStrength;
		copy.maximumReelingLateralSpeed = maximumReelingLateralSpeed;
		copy.reelingObstacleBypassMultiplier = reelingObstacleBypassMultiplier;
		copy.reelingCollisionForwardRetention = reelingCollisionForwardRetention;
		copy.reelingLateralDetectionDistance = reelingLateralDetectionDistance;
		copy.reelingLateralFadeDistance = reelingLateralFadeDistance;
		copy.wallDetectionDistance = wallDetectionDistance;
		copy.wallClingDurationSeconds = wallClingDurationSeconds;
		copy.wallClingStrength = wallClingStrength;
		copy.wallClimbSpeed = wallClimbSpeed;
		copy.wallClimbDownSpeed = wallClimbDownSpeed;
		copy.wallHorizontalMoveSpeed = wallHorizontalMoveSpeed;
		copy.wallJumpUpVelocity = wallJumpUpVelocity;
		copy.wallJumpOutVelocity = wallJumpOutVelocity;
		copy.swingControlStrength = swingControlStrength;
		copy.maximumSwingSpeed = maximumSwingSpeed;
		copy.ropeConstraintStrength = ropeConstraintStrength;
		copy.ropeDamping = ropeDamping;
		copy.ropeTolerance = ropeTolerance;
		copy.rappelEnabled = rappelEnabled;
		copy.rappelSpeed = rappelSpeed;
		copy.rappelInitialSlack = rappelInitialSlack;
		copy.autoDetachOnGround = autoDetachOnGround;
		copy.showRopeLengthHud = showRopeLengthHud;
		copy.anchorDurabilityCost = anchorDurabilityCost;
		copy.anchorHookCooldownSeconds = anchorHookCooldownSeconds;
		copy.anchorHookFailedCastDelaySeconds = anchorHookFailedCastDelaySeconds;
		copy.showWallClingTimerHud = showWallClingTimerHud;
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
		validated.luckyThreeInstantCatchDelaySeconds = validatedFiniteClamped(
			validated.luckyThreeInstantCatchDelaySeconds,
			DEFAULT_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS,
			MIN_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS,
			MAX_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS
		);
		validated.fishingEntityBaseChance = validatedFiniteClamped(
			validated.fishingEntityBaseChance,
			DEFAULT_FISHING_ENTITY_BASE_CHANCE,
			MIN_FISHING_ENTITY_CHANCE,
			MAX_FISHING_ENTITY_CHANCE
		);
		validated.fishingEntityChanceBonusPerLuckyLevel = validatedFiniteClamped(
			validated.fishingEntityChanceBonusPerLuckyLevel,
			DEFAULT_FISHING_ENTITY_CHANCE_BONUS_PER_LUCKY_LEVEL,
			MIN_FISHING_ENTITY_CHANCE,
			MAX_FISHING_ENTITY_CHANCE
		);
		validated.maximumFishingEntityChance = validatedFiniteClamped(
			validated.maximumFishingEntityChance,
			DEFAULT_MAXIMUM_FISHING_ENTITY_CHANCE,
			MIN_FISHING_ENTITY_CHANCE,
			MAX_FISHING_ENTITY_CHANCE
		);
		validated.aquaticEntityCategoryWeight = validatedCategoryWeight(
			validated.aquaticEntityCategoryWeight,
			DEFAULT_AQUATIC_ENTITY_CATEGORY_WEIGHT
		);
		validated.landAnimalCategoryWeight = validatedCategoryWeight(
			validated.landAnimalCategoryWeight,
			DEFAULT_LAND_ANIMAL_CATEGORY_WEIGHT
		);
		validated.landMonsterCategoryWeight = validatedCategoryWeight(
			validated.landMonsterCategoryWeight,
			DEFAULT_LAND_MONSTER_CATEGORY_WEIGHT
		);
		validated.netherEntityCategoryWeight = validatedCategoryWeight(
			validated.netherEntityCategoryWeight,
			DEFAULT_NETHER_ENTITY_CATEGORY_WEIGHT
		);
		validated.bossEntityCategoryWeight = validatedCategoryWeight(
			validated.bossEntityCategoryWeight,
			DEFAULT_BOSS_ENTITY_CATEGORY_WEIGHT
		);
		validated.grapplingHookCooldownSeconds = validatedFiniteClamped(
			validated.grapplingHookCooldownSeconds,
			DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS,
			MIN_GRAPPLING_HOOK_COOLDOWN_SECONDS,
			MAX_GRAPPLING_HOOK_COOLDOWN_SECONDS
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
		validated.allowPullBlockEntities = false;
		validated.maximumBlockHardness = validatedFiniteClamped(validated.maximumBlockHardness, DEFAULT_MAXIMUM_BLOCK_HARDNESS, MIN_MAXIMUM_BLOCK_HARDNESS, MAX_MAXIMUM_BLOCK_HARDNESS);
		validated.blockPullSpeedMultiplier = validatedFiniteClamped(validated.blockPullSpeedMultiplier, DEFAULT_BLOCK_PULL_SPEED_MULTIPLIER, MIN_BLOCK_PULL_SPEED_MULTIPLIER, MAX_BLOCK_PULL_SPEED_MULTIPLIER);
		validated.maxBlockPullDurationSeconds = validatedFiniteClamped(validated.maxBlockPullDurationSeconds, DEFAULT_MAX_BLOCK_PULL_DURATION_SECONDS, MIN_MAX_BLOCK_PULL_DURATION_SECONDS, MAX_MAX_BLOCK_PULL_DURATION_SECONDS);
		validated.blockPullStopDistance = validatedFiniteClamped(validated.blockPullStopDistance, DEFAULT_BLOCK_PULL_STOP_DISTANCE, MIN_BLOCK_PULL_STOP_DISTANCE, MAX_BLOCK_PULL_STOP_DISTANCE);
		validated.blockPullDurabilityCost = Math.clamp(validated.blockPullDurabilityCost, MIN_BLOCK_PULL_DURABILITY_COST, MAX_BLOCK_PULL_DURABILITY_COST);
		validated.anchorLevel1MaxRange = validatedFiniteClamped(
			validated.anchorLevel1MaxRange,
			DEFAULT_ANCHOR_LEVEL_1_MAX_RANGE,
			MIN_GRAPPLE_RANGE,
			MAX_GRAPPLE_RANGE
		);
		validated.anchorLevel2MaxRange = Math.max(
			validated.anchorLevel1MaxRange,
			validatedFiniteClamped(validated.anchorLevel2MaxRange, DEFAULT_ANCHOR_LEVEL_2_MAX_RANGE, MIN_GRAPPLE_RANGE, MAX_GRAPPLE_RANGE)
		);
		validated.anchorLevel3MaxRange = Math.max(
			validated.anchorLevel2MaxRange,
			validatedFiniteClamped(validated.anchorLevel3MaxRange, DEFAULT_ANCHOR_LEVEL_3_MAX_RANGE, MIN_GRAPPLE_RANGE, MAX_GRAPPLE_RANGE)
		);
		validated.reelAcceleration = validatedFiniteClamped(validated.reelAcceleration, DEFAULT_REEL_ACCELERATION, MIN_REEL_ACCELERATION, MAX_REEL_ACCELERATION);
		validated.maximumReelSpeed = validatedFiniteClamped(validated.maximumReelSpeed, DEFAULT_MAXIMUM_REEL_SPEED, MIN_MAXIMUM_REEL_SPEED, MAX_MAXIMUM_REEL_SPEED);
		validated.reelArrivalDistance = validatedFiniteClamped(validated.reelArrivalDistance, DEFAULT_REEL_ARRIVAL_DISTANCE, MIN_REEL_ARRIVAL_DISTANCE, MAX_REEL_ARRIVAL_DISTANCE);
		validated.maximumReelUpDurationSeconds = validatedFiniteClamped(validated.maximumReelUpDurationSeconds, DEFAULT_MAXIMUM_REEL_UP_DURATION_SECONDS, MIN_MAXIMUM_REEL_UP_DURATION_SECONDS, MAX_MAXIMUM_REEL_UP_DURATION_SECONDS);
		validated.reelTargetVerticalOffset = validatedFiniteClamped(validated.reelTargetVerticalOffset, DEFAULT_REEL_TARGET_VERTICAL_OFFSET, MIN_REEL_TARGET_VERTICAL_OFFSET, MAX_REEL_TARGET_VERTICAL_OFFSET);
		validated.reelTargetSurfaceOffset = validatedFiniteClamped(validated.reelTargetSurfaceOffset, DEFAULT_REEL_TARGET_SURFACE_OFFSET, MIN_REEL_TARGET_SURFACE_OFFSET, MAX_REEL_TARGET_SURFACE_OFFSET);
		validated.reelingLateralControlStrength = validatedFiniteClamped(validated.reelingLateralControlStrength, DEFAULT_REELING_LATERAL_CONTROL_STRENGTH, MIN_REELING_LATERAL_CONTROL_STRENGTH, MAX_REELING_LATERAL_CONTROL_STRENGTH);
		validated.maximumReelingLateralSpeed = validatedFiniteClamped(validated.maximumReelingLateralSpeed, DEFAULT_MAXIMUM_REELING_LATERAL_SPEED, MIN_MAXIMUM_REELING_LATERAL_SPEED, MAX_MAXIMUM_REELING_LATERAL_SPEED);
		validated.reelingObstacleBypassMultiplier = validatedFiniteClamped(validated.reelingObstacleBypassMultiplier, DEFAULT_REELING_OBSTACLE_BYPASS_MULTIPLIER, MIN_REELING_OBSTACLE_BYPASS_MULTIPLIER, MAX_REELING_OBSTACLE_BYPASS_MULTIPLIER);
		validated.reelingCollisionForwardRetention = validatedFiniteClamped(validated.reelingCollisionForwardRetention, DEFAULT_REELING_COLLISION_FORWARD_RETENTION, MIN_REELING_COLLISION_FORWARD_RETENTION, MAX_REELING_COLLISION_FORWARD_RETENTION);
		validated.reelingLateralDetectionDistance = validatedFiniteClamped(validated.reelingLateralDetectionDistance, DEFAULT_REELING_LATERAL_DETECTION_DISTANCE, MIN_REELING_LATERAL_DETECTION_DISTANCE, MAX_REELING_LATERAL_DETECTION_DISTANCE);
		validated.reelingLateralFadeDistance = validatedFiniteClamped(validated.reelingLateralFadeDistance, DEFAULT_REELING_LATERAL_FADE_DISTANCE, MIN_REELING_LATERAL_FADE_DISTANCE, MAX_REELING_LATERAL_FADE_DISTANCE);
		validated.wallDetectionDistance = validatedFiniteClamped(validated.wallDetectionDistance, DEFAULT_WALL_DETECTION_DISTANCE, MIN_WALL_DETECTION_DISTANCE, MAX_WALL_DETECTION_DISTANCE);
		validated.wallClingDurationSeconds = validatedFiniteClamped(validated.wallClingDurationSeconds, DEFAULT_WALL_CLING_DURATION_SECONDS, MIN_WALL_CLING_DURATION_SECONDS, MAX_WALL_CLING_DURATION_SECONDS);
		validated.wallClingStrength = validatedFiniteClamped(validated.wallClingStrength, DEFAULT_WALL_CLING_STRENGTH, MIN_WALL_CLING_STRENGTH, MAX_WALL_CLING_STRENGTH);
		validated.wallClimbSpeed = validatedFiniteClamped(validated.wallClimbSpeed, DEFAULT_WALL_CLIMB_SPEED, MIN_WALL_CLIMB_SPEED, MAX_WALL_CLIMB_SPEED);
		validated.wallClimbDownSpeed = validatedFiniteClamped(validated.wallClimbDownSpeed, DEFAULT_WALL_CLIMB_DOWN_SPEED, MIN_WALL_CLIMB_DOWN_SPEED, MAX_WALL_CLIMB_DOWN_SPEED);
		validated.wallHorizontalMoveSpeed = validatedFiniteClamped(validated.wallHorizontalMoveSpeed, DEFAULT_WALL_HORIZONTAL_MOVE_SPEED, MIN_WALL_HORIZONTAL_MOVE_SPEED, MAX_WALL_HORIZONTAL_MOVE_SPEED);
		validated.wallJumpUpVelocity = validatedFiniteClamped(validated.wallJumpUpVelocity, DEFAULT_WALL_JUMP_UP_VELOCITY, MIN_WALL_JUMP_UP_VELOCITY, MAX_WALL_JUMP_UP_VELOCITY);
		validated.wallJumpOutVelocity = validatedFiniteClamped(validated.wallJumpOutVelocity, DEFAULT_WALL_JUMP_OUT_VELOCITY, MIN_WALL_JUMP_OUT_VELOCITY, MAX_WALL_JUMP_OUT_VELOCITY);
		validated.swingControlStrength = validatedFiniteClamped(validated.swingControlStrength, DEFAULT_SWING_CONTROL_STRENGTH, MIN_SWING_CONTROL_STRENGTH, MAX_SWING_CONTROL_STRENGTH);
		validated.maximumSwingSpeed = validatedFiniteClamped(validated.maximumSwingSpeed, DEFAULT_MAXIMUM_SWING_SPEED, MIN_MAXIMUM_SWING_SPEED, MAX_MAXIMUM_SWING_SPEED);
		validated.ropeConstraintStrength = validatedFiniteClamped(validated.ropeConstraintStrength, DEFAULT_ROPE_CONSTRAINT_STRENGTH, MIN_ROPE_CONSTRAINT_STRENGTH, MAX_ROPE_CONSTRAINT_STRENGTH);
		validated.ropeDamping = validatedFiniteClamped(validated.ropeDamping, DEFAULT_ROPE_DAMPING, MIN_ROPE_DAMPING, MAX_ROPE_DAMPING);
		validated.ropeTolerance = validatedFiniteClamped(validated.ropeTolerance, DEFAULT_ROPE_TOLERANCE, MIN_ROPE_TOLERANCE, MAX_ROPE_TOLERANCE);
		validated.rappelSpeed = validatedFiniteClamped(validated.rappelSpeed, DEFAULT_RAPPEL_SPEED, MIN_RAPPEL_SPEED, MAX_RAPPEL_SPEED);
		validated.rappelInitialSlack = validatedFiniteClamped(validated.rappelInitialSlack, DEFAULT_RAPPEL_INITIAL_SLACK, MIN_RAPPEL_INITIAL_SLACK, MAX_RAPPEL_INITIAL_SLACK);
		validated.anchorDurabilityCost = Math.clamp(validated.anchorDurabilityCost, MIN_ANCHOR_DURABILITY_COST, MAX_ANCHOR_DURABILITY_COST);
		validated.anchorHookCooldownSeconds = validatedFiniteClamped(validated.anchorHookCooldownSeconds, DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS, MIN_ANCHOR_HOOK_COOLDOWN_SECONDS, MAX_ANCHOR_HOOK_COOLDOWN_SECONDS);
		validated.anchorHookFailedCastDelaySeconds = validatedFiniteClamped(validated.anchorHookFailedCastDelaySeconds, DEFAULT_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS, MIN_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS, MAX_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS);
		return validated;
	}

	private static double validatedFiniteClamped(double value, double defaultValue, double min, double max) {
		return clamp(finiteOrDefault(value, defaultValue), min, max);
	}

	private static double validatedCategoryWeight(double value, double defaultValue) {
		return validatedFiniteClamped(
			value,
			defaultValue,
			MIN_FISHING_ENTITY_CATEGORY_WEIGHT,
			MAX_FISHING_ENTITY_CATEGORY_WEIGHT
		);
	}

	private static double finiteOrDefault(double value, double defaultValue) {
		return Double.isFinite(value) ? value : defaultValue;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
