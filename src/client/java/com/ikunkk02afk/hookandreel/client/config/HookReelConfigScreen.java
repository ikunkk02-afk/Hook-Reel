package com.ikunkk02afk.hookandreel.client.config;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import java.util.function.DoubleConsumer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HookReelConfigScreen {
	private HookReelConfigScreen() {
	}

	public static Screen create(Screen parent) {
		HookReelConfig editing = HookReelConfigManager.get().copy();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.translatable("title.hook_and_reel.config"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory lucky = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.lucky")
		);
		lucky.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.lucky_enchantment_enabled"),
				editing.luckyEnchantmentEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_LUCKY_ENCHANTMENT_ENABLED)
			.setSaveConsumer(value -> editing.luckyEnchantmentEnabled = value)
			.build());
		lucky.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.lucky_bonus_per_level"),
				editing.luckyBonusPerLevel
			)
			.setDefaultValue(HookReelConfig.DEFAULT_LUCKY_BONUS_PER_LEVEL)
			.setMin(HookReelConfig.MIN_LUCKY_BONUS_PER_LEVEL)
			.setMax(HookReelConfig.MAX_LUCKY_BONUS_PER_LEVEL)
			.setTooltip(Component.translatable("option.hook_and_reel.lucky_bonus_per_level.tooltip"))
			.setSaveConsumer(value -> editing.luckyBonusPerLevel = value)
			.build());
		lucky.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_stack_with_luck_of_the_sea"),
				editing.allowStackWithLuckOfTheSea
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_STACK_WITH_LUCK_OF_THE_SEA)
			.setSaveConsumer(value -> editing.allowStackWithLuckOfTheSea = value)
			.build());

		ConfigCategory luckyThree = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.lucky_three")
		);
		luckyThree.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.lucky_three_instant_catch_enabled"),
				editing.luckyThreeInstantCatchEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_LUCKY_THREE_INSTANT_CATCH_ENABLED)
			.setTooltip(Component.translatable("option.hook_and_reel.lucky_three_instant_catch_enabled.tooltip"))
			.setSaveConsumer(value -> editing.luckyThreeInstantCatchEnabled = value)
			.build());
		luckyThree.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.lucky_three_instant_catch_delay_seconds"),
				editing.luckyThreeInstantCatchDelaySeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS)
			.setMin(HookReelConfig.MIN_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS)
			.setMax(HookReelConfig.MAX_LUCKY_THREE_INSTANT_CATCH_DELAY_SECONDS)
			.setTooltip(Component.translatable("option.hook_and_reel.lucky_three_instant_catch_delay_seconds.tooltip"))
			.setSaveConsumer(value -> editing.luckyThreeInstantCatchDelaySeconds = value)
			.build());
		luckyThree.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.lucky_three_auto_retract"),
				editing.luckyThreeAutoRetract
			)
			.setDefaultValue(HookReelConfig.DEFAULT_LUCKY_THREE_AUTO_RETRACT)
			.setTooltip(Component.translatable("option.hook_and_reel.lucky_three_auto_retract.tooltip"))
			.setSaveConsumer(value -> editing.luckyThreeAutoRetract = value)
			.build());

		ConfigCategory cooldowns = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.ability_cooldowns")
		);
		cooldowns.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.grappling_hook_cooldown_seconds"),
				editing.grapplingHookCooldownSeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS)
			.setMin(HookReelConfig.MIN_GRAPPLING_HOOK_COOLDOWN_SECONDS)
			.setMax(HookReelConfig.MAX_GRAPPLING_HOOK_COOLDOWN_SECONDS)
			.setTooltip(Component.translatable("option.hook_and_reel.grappling_hook_cooldown_seconds.tooltip"))
			.setSaveConsumer(value -> editing.grapplingHookCooldownSeconds = value)
			.build());
		cooldowns.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.anchor_hook_cooldown_seconds"),
				editing.anchorHookCooldownSeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS)
			.setMin(HookReelConfig.MIN_ANCHOR_HOOK_COOLDOWN_SECONDS)
			.setMax(HookReelConfig.MAX_ANCHOR_HOOK_COOLDOWN_SECONDS)
			.setTooltip(Component.translatable("option.hook_and_reel.anchor_hook_cooldown_seconds.tooltip"))
			.setSaveConsumer(value -> editing.anchorHookCooldownSeconds = value)
			.build());
		ConfigCategory fishingEntities = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.fishing_entities")
		);
		SubCategoryBuilder fishingEntityGeneral = entries.startSubCategory(
			Component.translatable("subcategory.hook_and_reel.fishing_entity_general")
		).setExpanded(true);
		fishingEntityGeneral.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_fishing_entities"),
				editing.allowFishingEntities
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_FISHING_ENTITIES)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_fishing_entities.tooltip"))
			.setSaveConsumer(value -> editing.allowFishingEntities = value)
			.build());
		fishingEntityGeneral.add(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.fishing_entity_base_chance_percent"),
				editing.fishingEntityBaseChance * 100.0D
			)
			.setDefaultValue(HookReelConfig.DEFAULT_FISHING_ENTITY_BASE_CHANCE * 100.0D)
			.setMin(0.0D)
			.setMax(100.0D)
			.setTooltip(Component.translatable("option.hook_and_reel.fishing_entity_base_chance_percent.tooltip"))
			.setSaveConsumer(value -> editing.fishingEntityBaseChance = value / 100.0D)
			.build());
		fishingEntityGeneral.add(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.fishing_entity_bonus_per_level_percent"),
				editing.fishingEntityChanceBonusPerLuckyLevel * 100.0D
			)
			.setDefaultValue(HookReelConfig.DEFAULT_FISHING_ENTITY_CHANCE_BONUS_PER_LUCKY_LEVEL * 100.0D)
			.setMin(0.0D)
			.setMax(100.0D)
			.setTooltip(Component.translatable("option.hook_and_reel.fishing_entity_bonus_per_level_percent.tooltip"))
			.setSaveConsumer(value -> editing.fishingEntityChanceBonusPerLuckyLevel = value / 100.0D)
			.build());
		fishingEntityGeneral.add(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.maximum_fishing_entity_chance_percent"),
				editing.maximumFishingEntityChance * 100.0D
			)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_FISHING_ENTITY_CHANCE * 100.0D)
			.setMin(0.0D)
			.setMax(100.0D)
			.setTooltip(Component.translatable("option.hook_and_reel.maximum_fishing_entity_chance_percent.tooltip"))
			.setSaveConsumer(value -> editing.maximumFishingEntityChance = value / 100.0D)
			.build());
		fishingEntities.addEntry(fishingEntityGeneral.build());

		SubCategoryBuilder allowedFishingCategories = entries.startSubCategory(
			Component.translatable("subcategory.hook_and_reel.allowed_fishing_categories")
		).setExpanded(true);
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_aquatic_entities"),
				editing.allowAquaticEntities
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_AQUATIC_ENTITIES)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_aquatic_entities.tooltip"))
			.setSaveConsumer(value -> editing.allowAquaticEntities = value)
			.build());
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_land_animals"),
				editing.allowLandAnimals
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_LAND_ANIMALS)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_land_animals.tooltip"))
			.setSaveConsumer(value -> editing.allowLandAnimals = value)
			.build());
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_land_monsters"),
				editing.allowLandMonsters
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_LAND_MONSTERS)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_land_monsters.tooltip"))
			.setSaveConsumer(value -> editing.allowLandMonsters = value)
			.build());
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_nether_entities"),
				editing.allowNetherEntities
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_NETHER_ENTITIES)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_nether_entities.tooltip"))
			.setSaveConsumer(value -> editing.allowNetherEntities = value)
			.build());
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.nether_entities_only_in_nether"),
				editing.netherEntitiesOnlyInNether
			)
			.setDefaultValue(HookReelConfig.DEFAULT_NETHER_ENTITIES_ONLY_IN_NETHER)
			.setTooltip(Component.translatable("option.hook_and_reel.nether_entities_only_in_nether.tooltip"))
			.setSaveConsumer(value -> editing.netherEntitiesOnlyInNether = value)
			.build());
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_boss_entities"),
				editing.allowBossEntities
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_BOSS_ENTITIES)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_boss_entities.tooltip"))
			.setSaveConsumer(value -> editing.allowBossEntities = value)
			.build());
		allowedFishingCategories.add(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_ender_dragon_fishing"),
				editing.allowEnderDragonFishing
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_ENDER_DRAGON_FISHING)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_ender_dragon_fishing.tooltip"))
			.setSaveConsumer(value -> editing.allowEnderDragonFishing = value)
			.build());
		fishingEntities.addEntry(allowedFishingCategories.build());

		SubCategoryBuilder fishingCategoryWeights = entries.startSubCategory(
			Component.translatable("subcategory.hook_and_reel.fishing_category_weights")
		).setExpanded(true);
		fishingCategoryWeights.add(categoryWeightEntry(
			entries,
			"aquatic_entity_category_weight",
			editing.aquaticEntityCategoryWeight,
			HookReelConfig.DEFAULT_AQUATIC_ENTITY_CATEGORY_WEIGHT,
			value -> editing.aquaticEntityCategoryWeight = value
		));
		fishingCategoryWeights.add(categoryWeightEntry(
			entries,
			"land_animal_category_weight",
			editing.landAnimalCategoryWeight,
			HookReelConfig.DEFAULT_LAND_ANIMAL_CATEGORY_WEIGHT,
			value -> editing.landAnimalCategoryWeight = value
		));
		fishingCategoryWeights.add(categoryWeightEntry(
			entries,
			"land_monster_category_weight",
			editing.landMonsterCategoryWeight,
			HookReelConfig.DEFAULT_LAND_MONSTER_CATEGORY_WEIGHT,
			value -> editing.landMonsterCategoryWeight = value
		));
		fishingCategoryWeights.add(categoryWeightEntry(
			entries,
			"nether_entity_category_weight",
			editing.netherEntityCategoryWeight,
			HookReelConfig.DEFAULT_NETHER_ENTITY_CATEGORY_WEIGHT,
			value -> editing.netherEntityCategoryWeight = value
		));
		fishingCategoryWeights.add(categoryWeightEntry(
			entries,
			"boss_entity_category_weight",
			editing.bossEntityCategoryWeight,
			HookReelConfig.DEFAULT_BOSS_ENTITY_CATEGORY_WEIGHT,
			value -> editing.bossEntityCategoryWeight = value
		));
		fishingEntities.addEntry(fishingCategoryWeights.build());

		ConfigCategory grapple = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.grapple")
		);
		grapple.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.grappling_hook_enabled"),
				editing.grapplingHookEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_GRAPPLING_HOOK_ENABLED)
			.setSaveConsumer(value -> editing.grapplingHookEnabled = value)
			.build());
		grapple.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.max_charge_time_seconds"),
				editing.maxChargeTimeSeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_MAX_CHARGE_TIME_SECONDS)
			.setMin(HookReelConfig.MIN_MAX_CHARGE_TIME_SECONDS)
			.setMax(HookReelConfig.MAX_MAX_CHARGE_TIME_SECONDS)
			.setSaveConsumer(value -> editing.maxChargeTimeSeconds = value)
			.build());
		grapple.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.minimum_grapple_range"),
				editing.minimumGrappleRange
			)
			.setDefaultValue(HookReelConfig.DEFAULT_MINIMUM_GRAPPLE_RANGE)
			.setMin(HookReelConfig.MIN_GRAPPLE_RANGE)
			.setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.minimumGrappleRange = value)
			.build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.grapple_level_1_max_range"), editing.grappleLevel1MaxRange)
			.setDefaultValue(HookReelConfig.DEFAULT_GRAPPLE_LEVEL_1_MAX_RANGE).setMin(HookReelConfig.MIN_GRAPPLE_RANGE).setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.grappleLevel1MaxRange = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.grapple_level_2_max_range"), editing.grappleLevel2MaxRange)
			.setDefaultValue(HookReelConfig.DEFAULT_GRAPPLE_LEVEL_2_MAX_RANGE).setMin(HookReelConfig.MIN_GRAPPLE_RANGE).setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.grappleLevel2MaxRange = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.grapple_level_3_max_range"), editing.grappleLevel3MaxRange)
			.setDefaultValue(HookReelConfig.DEFAULT_GRAPPLE_LEVEL_3_MAX_RANGE).setMin(HookReelConfig.MIN_GRAPPLE_RANGE).setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.grappleLevel3MaxRange = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.pull_strength"), editing.pullStrength)
			.setDefaultValue(HookReelConfig.DEFAULT_PULL_STRENGTH).setMin(HookReelConfig.MIN_PULL_STRENGTH).setMax(HookReelConfig.MAX_PULL_STRENGTH)
			.setSaveConsumer(value -> editing.pullStrength = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.maximum_pull_speed"), editing.maximumPullSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_PULL_SPEED).setMin(HookReelConfig.MIN_MAXIMUM_PULL_SPEED).setMax(HookReelConfig.MAX_MAXIMUM_PULL_SPEED)
			.setSaveConsumer(value -> editing.maximumPullSpeed = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.item_pull_speed_multiplier"), editing.itemPullSpeedMultiplier)
			.setDefaultValue(HookReelConfig.DEFAULT_ITEM_PULL_SPEED_MULTIPLIER).setMin(HookReelConfig.MIN_ITEM_PULL_SPEED_MULTIPLIER).setMax(HookReelConfig.MAX_ITEM_PULL_SPEED_MULTIPLIER)
			.setSaveConsumer(value -> editing.itemPullSpeedMultiplier = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.pull_stop_distance"), editing.pullStopDistance)
			.setDefaultValue(HookReelConfig.DEFAULT_PULL_STOP_DISTANCE).setMin(HookReelConfig.MIN_PULL_STOP_DISTANCE).setMax(HookReelConfig.MAX_PULL_STOP_DISTANCE)
			.setSaveConsumer(value -> editing.pullStopDistance = value).build());
		grapple.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.max_pull_duration_seconds"), editing.maxPullDurationSeconds)
			.setDefaultValue(HookReelConfig.DEFAULT_MAX_PULL_DURATION_SECONDS).setMin(HookReelConfig.MIN_MAX_PULL_DURATION_SECONDS).setMax(HookReelConfig.MAX_MAX_PULL_DURATION_SECONDS)
			.setSaveConsumer(value -> editing.maxPullDurationSeconds = value).build());
		grapple.addEntry(entries.startBooleanToggle(Component.translatable("option.hook_and_reel.allow_pull_players"), editing.allowPullPlayers)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_PULL_PLAYERS).setSaveConsumer(value -> editing.allowPullPlayers = value).build());
		grapple.addEntry(entries.startBooleanToggle(Component.translatable("option.hook_and_reel.allow_pull_bosses"), editing.allowPullBosses)
			.setDefaultValue(HookReelConfig.DEFAULT_ALLOW_PULL_BOSSES).setSaveConsumer(value -> editing.allowPullBosses = value).build());

		ConfigCategory blockPulling = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.block_pulling")
		);
		blockPulling.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.block_pulling_enabled"),
				editing.blockPullingEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_BLOCK_PULLING_ENABLED)
			.setSaveConsumer(value -> editing.blockPullingEnabled = value)
			.build());
		var blockEntities = entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.allow_pull_block_entities"),
				false
			)
			.setDefaultValue(false)
			.setTooltip(Component.translatable("option.hook_and_reel.allow_pull_block_entities.tooltip"))
			.build();
		blockEntities.setEditable(false);
		blockPulling.addEntry(blockEntities);
		blockPulling.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.maximum_block_hardness"),
				editing.maximumBlockHardness
			)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_BLOCK_HARDNESS)
			.setMin(HookReelConfig.MIN_MAXIMUM_BLOCK_HARDNESS)
			.setMax(HookReelConfig.MAX_MAXIMUM_BLOCK_HARDNESS)
			.setSaveConsumer(value -> editing.maximumBlockHardness = value)
			.build());
		blockPulling.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.block_pull_speed_multiplier"),
				editing.blockPullSpeedMultiplier
			)
			.setDefaultValue(HookReelConfig.DEFAULT_BLOCK_PULL_SPEED_MULTIPLIER)
			.setMin(HookReelConfig.MIN_BLOCK_PULL_SPEED_MULTIPLIER)
			.setMax(HookReelConfig.MAX_BLOCK_PULL_SPEED_MULTIPLIER)
			.setSaveConsumer(value -> editing.blockPullSpeedMultiplier = value)
			.build());
		blockPulling.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.max_block_pull_duration_seconds"),
				editing.maxBlockPullDurationSeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_MAX_BLOCK_PULL_DURATION_SECONDS)
			.setMin(HookReelConfig.MIN_MAX_BLOCK_PULL_DURATION_SECONDS)
			.setMax(HookReelConfig.MAX_MAX_BLOCK_PULL_DURATION_SECONDS)
			.setSaveConsumer(value -> editing.maxBlockPullDurationSeconds = value)
			.build());
		blockPulling.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.block_pull_stop_distance"),
				editing.blockPullStopDistance
			)
			.setDefaultValue(HookReelConfig.DEFAULT_BLOCK_PULL_STOP_DISTANCE)
			.setMin(HookReelConfig.MIN_BLOCK_PULL_STOP_DISTANCE)
			.setMax(HookReelConfig.MAX_BLOCK_PULL_STOP_DISTANCE)
			.setSaveConsumer(value -> editing.blockPullStopDistance = value)
			.build());
		blockPulling.addEntry(entries.startIntField(
				Component.translatable("option.hook_and_reel.block_pull_durability_cost"),
				editing.blockPullDurabilityCost
			)
			.setDefaultValue(HookReelConfig.DEFAULT_BLOCK_PULL_DURABILITY_COST)
			.setMin(HookReelConfig.MIN_BLOCK_PULL_DURABILITY_COST)
			.setMax(HookReelConfig.MAX_BLOCK_PULL_DURABILITY_COST)
			.setSaveConsumer(value -> editing.blockPullDurabilityCost = value)
			.build());

		ConfigCategory swing = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.swing")
		);
		ConfigCategory anchorMovement = builder.getOrCreateCategory(
			Component.translatable("category.hook_and_reel.anchor_hook_movement")
		);
		anchorMovement.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.reeling_lateral_control_enabled"),
				editing.reelingLateralControlEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_REELING_LATERAL_CONTROL_ENABLED)
			.setSaveConsumer(value -> editing.reelingLateralControlEnabled = value)
			.build());
		anchorMovement.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reeling_lateral_control_strength"), editing.reelingLateralControlStrength)
			.setDefaultValue(HookReelConfig.DEFAULT_REELING_LATERAL_CONTROL_STRENGTH).setMin(HookReelConfig.MIN_REELING_LATERAL_CONTROL_STRENGTH).setMax(HookReelConfig.MAX_REELING_LATERAL_CONTROL_STRENGTH)
			.setSaveConsumer(value -> editing.reelingLateralControlStrength = value).build());
		anchorMovement.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.maximum_reeling_lateral_speed"), editing.maximumReelingLateralSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_REELING_LATERAL_SPEED).setMin(HookReelConfig.MIN_MAXIMUM_REELING_LATERAL_SPEED).setMax(HookReelConfig.MAX_MAXIMUM_REELING_LATERAL_SPEED)
			.setSaveConsumer(value -> editing.maximumReelingLateralSpeed = value).build());
		anchorMovement.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reeling_obstacle_bypass_multiplier"), editing.reelingObstacleBypassMultiplier)
			.setDefaultValue(HookReelConfig.DEFAULT_REELING_OBSTACLE_BYPASS_MULTIPLIER).setMin(HookReelConfig.MIN_REELING_OBSTACLE_BYPASS_MULTIPLIER).setMax(HookReelConfig.MAX_REELING_OBSTACLE_BYPASS_MULTIPLIER)
			.setSaveConsumer(value -> editing.reelingObstacleBypassMultiplier = value).build());
		anchorMovement.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reeling_collision_forward_retention"), editing.reelingCollisionForwardRetention)
			.setDefaultValue(HookReelConfig.DEFAULT_REELING_COLLISION_FORWARD_RETENTION).setMin(HookReelConfig.MIN_REELING_COLLISION_FORWARD_RETENTION).setMax(HookReelConfig.MAX_REELING_COLLISION_FORWARD_RETENTION)
			.setSaveConsumer(value -> editing.reelingCollisionForwardRetention = value).build());
		anchorMovement.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reeling_lateral_detection_distance"), editing.reelingLateralDetectionDistance)
			.setDefaultValue(HookReelConfig.DEFAULT_REELING_LATERAL_DETECTION_DISTANCE).setMin(HookReelConfig.MIN_REELING_LATERAL_DETECTION_DISTANCE).setMax(HookReelConfig.MAX_REELING_LATERAL_DETECTION_DISTANCE)
			.setSaveConsumer(value -> editing.reelingLateralDetectionDistance = value).build());
		anchorMovement.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reeling_lateral_fade_distance"), editing.reelingLateralFadeDistance)
			.setDefaultValue(HookReelConfig.DEFAULT_REELING_LATERAL_FADE_DISTANCE).setMin(HookReelConfig.MIN_REELING_LATERAL_FADE_DISTANCE).setMax(HookReelConfig.MAX_REELING_LATERAL_FADE_DISTANCE)
			.setSaveConsumer(value -> editing.reelingLateralFadeDistance = value).build());
		swing.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.anchor_hook_enabled"),
				editing.anchorHookEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_HOOK_ENABLED)
			.setSaveConsumer(value -> editing.anchorHookEnabled = value)
			.build());
		swing.addEntry(entries.startDoubleField(
				Component.translatable("option.hook_and_reel.anchor_hook_failed_cast_delay_seconds"),
				editing.anchorHookFailedCastDelaySeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS)
			.setMin(HookReelConfig.MIN_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS)
			.setMax(HookReelConfig.MAX_ANCHOR_HOOK_FAILED_CAST_DELAY_SECONDS)
			.setTooltip(Component.translatable("option.hook_and_reel.anchor_hook_failed_cast_delay_seconds.tooltip"))
			.setSaveConsumer(value -> editing.anchorHookFailedCastDelaySeconds = value)
			.build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.anchor_level_1_max_range"), editing.anchorLevel1MaxRange)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_LEVEL_1_MAX_RANGE).setMin(HookReelConfig.MIN_GRAPPLE_RANGE).setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.anchorLevel1MaxRange = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.anchor_level_2_max_range"), editing.anchorLevel2MaxRange)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_LEVEL_2_MAX_RANGE).setMin(HookReelConfig.MIN_GRAPPLE_RANGE).setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.anchorLevel2MaxRange = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.anchor_level_3_max_range"), editing.anchorLevel3MaxRange)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_LEVEL_3_MAX_RANGE).setMin(HookReelConfig.MIN_GRAPPLE_RANGE).setMax(HookReelConfig.MAX_GRAPPLE_RANGE)
			.setSaveConsumer(value -> editing.anchorLevel3MaxRange = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reel_acceleration"), editing.reelAcceleration)
			.setDefaultValue(HookReelConfig.DEFAULT_REEL_ACCELERATION).setMin(HookReelConfig.MIN_REEL_ACCELERATION).setMax(HookReelConfig.MAX_REEL_ACCELERATION)
			.setSaveConsumer(value -> editing.reelAcceleration = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.maximum_reel_speed"), editing.maximumReelSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_REEL_SPEED).setMin(HookReelConfig.MIN_MAXIMUM_REEL_SPEED).setMax(HookReelConfig.MAX_MAXIMUM_REEL_SPEED)
			.setSaveConsumer(value -> editing.maximumReelSpeed = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reel_arrival_distance"), editing.reelArrivalDistance)
			.setDefaultValue(HookReelConfig.DEFAULT_REEL_ARRIVAL_DISTANCE).setMin(HookReelConfig.MIN_REEL_ARRIVAL_DISTANCE).setMax(HookReelConfig.MAX_REEL_ARRIVAL_DISTANCE)
			.setSaveConsumer(value -> editing.reelArrivalDistance = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.maximum_reel_up_duration_seconds"), editing.maximumReelUpDurationSeconds)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_REEL_UP_DURATION_SECONDS).setMin(HookReelConfig.MIN_MAXIMUM_REEL_UP_DURATION_SECONDS).setMax(HookReelConfig.MAX_MAXIMUM_REEL_UP_DURATION_SECONDS)
			.setSaveConsumer(value -> editing.maximumReelUpDurationSeconds = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reel_target_vertical_offset"), editing.reelTargetVerticalOffset)
			.setDefaultValue(HookReelConfig.DEFAULT_REEL_TARGET_VERTICAL_OFFSET).setMin(HookReelConfig.MIN_REEL_TARGET_VERTICAL_OFFSET).setMax(HookReelConfig.MAX_REEL_TARGET_VERTICAL_OFFSET)
			.setSaveConsumer(value -> editing.reelTargetVerticalOffset = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.reel_target_surface_offset"), editing.reelTargetSurfaceOffset)
			.setDefaultValue(HookReelConfig.DEFAULT_REEL_TARGET_SURFACE_OFFSET).setMin(HookReelConfig.MIN_REEL_TARGET_SURFACE_OFFSET).setMax(HookReelConfig.MAX_REEL_TARGET_SURFACE_OFFSET)
			.setSaveConsumer(value -> editing.reelTargetSurfaceOffset = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_detection_distance"), editing.wallDetectionDistance)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_DETECTION_DISTANCE).setMin(HookReelConfig.MIN_WALL_DETECTION_DISTANCE).setMax(HookReelConfig.MAX_WALL_DETECTION_DISTANCE)
			.setSaveConsumer(value -> editing.wallDetectionDistance = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_cling_duration_seconds"), editing.wallClingDurationSeconds)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_CLING_DURATION_SECONDS).setMin(HookReelConfig.MIN_WALL_CLING_DURATION_SECONDS).setMax(HookReelConfig.MAX_WALL_CLING_DURATION_SECONDS)
			.setSaveConsumer(value -> editing.wallClingDurationSeconds = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_cling_strength"), editing.wallClingStrength)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_CLING_STRENGTH).setMin(HookReelConfig.MIN_WALL_CLING_STRENGTH).setMax(HookReelConfig.MAX_WALL_CLING_STRENGTH)
			.setSaveConsumer(value -> editing.wallClingStrength = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_climb_speed"), editing.wallClimbSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_CLIMB_SPEED).setMin(HookReelConfig.MIN_WALL_CLIMB_SPEED).setMax(HookReelConfig.MAX_WALL_CLIMB_SPEED)
			.setSaveConsumer(value -> editing.wallClimbSpeed = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_climb_down_speed"), editing.wallClimbDownSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_CLIMB_DOWN_SPEED).setMin(HookReelConfig.MIN_WALL_CLIMB_DOWN_SPEED).setMax(HookReelConfig.MAX_WALL_CLIMB_DOWN_SPEED)
			.setSaveConsumer(value -> editing.wallClimbDownSpeed = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_horizontal_move_speed"), editing.wallHorizontalMoveSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_HORIZONTAL_MOVE_SPEED).setMin(HookReelConfig.MIN_WALL_HORIZONTAL_MOVE_SPEED).setMax(HookReelConfig.MAX_WALL_HORIZONTAL_MOVE_SPEED)
			.setSaveConsumer(value -> editing.wallHorizontalMoveSpeed = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_jump_up_velocity"), editing.wallJumpUpVelocity)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_JUMP_UP_VELOCITY).setMin(HookReelConfig.MIN_WALL_JUMP_UP_VELOCITY).setMax(HookReelConfig.MAX_WALL_JUMP_UP_VELOCITY)
			.setSaveConsumer(value -> editing.wallJumpUpVelocity = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.wall_jump_out_velocity"), editing.wallJumpOutVelocity)
			.setDefaultValue(HookReelConfig.DEFAULT_WALL_JUMP_OUT_VELOCITY).setMin(HookReelConfig.MIN_WALL_JUMP_OUT_VELOCITY).setMax(HookReelConfig.MAX_WALL_JUMP_OUT_VELOCITY)
			.setSaveConsumer(value -> editing.wallJumpOutVelocity = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.swing_control_strength"), editing.swingControlStrength)
			.setDefaultValue(HookReelConfig.DEFAULT_SWING_CONTROL_STRENGTH).setMin(HookReelConfig.MIN_SWING_CONTROL_STRENGTH).setMax(HookReelConfig.MAX_SWING_CONTROL_STRENGTH)
			.setSaveConsumer(value -> editing.swingControlStrength = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.maximum_swing_speed"), editing.maximumSwingSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_MAXIMUM_SWING_SPEED).setMin(HookReelConfig.MIN_MAXIMUM_SWING_SPEED).setMax(HookReelConfig.MAX_MAXIMUM_SWING_SPEED)
			.setSaveConsumer(value -> editing.maximumSwingSpeed = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.rope_constraint_strength"), editing.ropeConstraintStrength)
			.setDefaultValue(HookReelConfig.DEFAULT_ROPE_CONSTRAINT_STRENGTH).setMin(HookReelConfig.MIN_ROPE_CONSTRAINT_STRENGTH).setMax(HookReelConfig.MAX_ROPE_CONSTRAINT_STRENGTH)
			.setSaveConsumer(value -> editing.ropeConstraintStrength = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.rope_damping"), editing.ropeDamping)
			.setDefaultValue(HookReelConfig.DEFAULT_ROPE_DAMPING).setMin(HookReelConfig.MIN_ROPE_DAMPING).setMax(HookReelConfig.MAX_ROPE_DAMPING)
			.setSaveConsumer(value -> editing.ropeDamping = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.rope_tolerance"), editing.ropeTolerance)
			.setDefaultValue(HookReelConfig.DEFAULT_ROPE_TOLERANCE).setMin(HookReelConfig.MIN_ROPE_TOLERANCE).setMax(HookReelConfig.MAX_ROPE_TOLERANCE)
			.setSaveConsumer(value -> editing.ropeTolerance = value).build());
		swing.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.rappel_enabled"),
				editing.rappelEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_RAPPEL_ENABLED)
			.setSaveConsumer(value -> editing.rappelEnabled = value)
			.build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.rappel_speed"), editing.rappelSpeed)
			.setDefaultValue(HookReelConfig.DEFAULT_RAPPEL_SPEED).setMin(HookReelConfig.MIN_RAPPEL_SPEED).setMax(HookReelConfig.MAX_RAPPEL_SPEED)
			.setSaveConsumer(value -> editing.rappelSpeed = value).build());
		swing.addEntry(entries.startDoubleField(Component.translatable("option.hook_and_reel.rappel_initial_slack"), editing.rappelInitialSlack)
			.setDefaultValue(HookReelConfig.DEFAULT_RAPPEL_INITIAL_SLACK).setMin(HookReelConfig.MIN_RAPPEL_INITIAL_SLACK).setMax(HookReelConfig.MAX_RAPPEL_INITIAL_SLACK)
			.setSaveConsumer(value -> editing.rappelInitialSlack = value).build());
		swing.addEntry(entries.startBooleanToggle(Component.translatable("option.hook_and_reel.auto_detach_on_ground"), editing.autoDetachOnGround)
			.setDefaultValue(HookReelConfig.DEFAULT_AUTO_DETACH_ON_GROUND).setSaveConsumer(value -> editing.autoDetachOnGround = value).build());
		swing.addEntry(entries.startBooleanToggle(Component.translatable("option.hook_and_reel.show_rope_length_hud"), editing.showRopeLengthHud)
			.setDefaultValue(HookReelConfig.DEFAULT_SHOW_ROPE_LENGTH_HUD).setSaveConsumer(value -> editing.showRopeLengthHud = value).build());
		swing.addEntry(entries.startIntField(Component.translatable("option.hook_and_reel.anchor_durability_cost"), editing.anchorDurabilityCost)
			.setDefaultValue(HookReelConfig.DEFAULT_ANCHOR_DURABILITY_COST).setMin(HookReelConfig.MIN_ANCHOR_DURABILITY_COST).setMax(HookReelConfig.MAX_ANCHOR_DURABILITY_COST)
			.setSaveConsumer(value -> editing.anchorDurabilityCost = value).build());
		swing.addEntry(entries.startBooleanToggle(Component.translatable("option.hook_and_reel.show_wall_cling_timer_hud"), editing.showWallClingTimerHud)
			.setDefaultValue(HookReelConfig.DEFAULT_SHOW_WALL_CLING_TIMER_HUD).setSaveConsumer(value -> editing.showWallClingTimerHud = value).build());

		builder.setSavingRunnable(() -> HookReelConfigManager.save(editing));
		return builder.build();
	}

	private static AbstractConfigListEntry<?> categoryWeightEntry(
		ConfigEntryBuilder entries,
		String key,
		double value,
		double defaultValue,
		DoubleConsumer saveConsumer
	) {
		return entries.startDoubleField(
				Component.translatable("option.hook_and_reel." + key),
				value
			)
			.setDefaultValue(defaultValue)
			.setMin(HookReelConfig.MIN_FISHING_ENTITY_CATEGORY_WEIGHT)
			.setMax(HookReelConfig.MAX_FISHING_ENTITY_CATEGORY_WEIGHT)
			.setTooltip(Component.translatable("option.hook_and_reel." + key + ".tooltip"))
			.setSaveConsumer(saveConsumer::accept)
			.build();
	}
}
