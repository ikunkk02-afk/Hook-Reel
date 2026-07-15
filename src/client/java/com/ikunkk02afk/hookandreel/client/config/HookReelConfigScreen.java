package com.ikunkk02afk.hookandreel.client.config;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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
		grapple.addEntry(entries.startIntField(
				Component.translatable("option.hook_and_reel.grapple_cooldown_seconds"),
				editing.grappleCooldownSeconds
			)
			.setDefaultValue(HookReelConfig.DEFAULT_GRAPPLE_COOLDOWN_SECONDS)
			.setMin(HookReelConfig.MIN_GRAPPLE_COOLDOWN_SECONDS)
			.setMax(HookReelConfig.MAX_GRAPPLE_COOLDOWN_SECONDS)
			.setSaveConsumer(value -> editing.grappleCooldownSeconds = value)
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
			Component.translatable("category.hook_and_reel.future_swing")
		);
		swing.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.swing_enabled"),
				editing.swingEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_SWING_ENABLED)
			.setSaveConsumer(value -> editing.swingEnabled = value)
			.build());
		swing.addEntry(entries.startBooleanToggle(
				Component.translatable("option.hook_and_reel.rappel_enabled"),
				editing.rappelEnabled
			)
			.setDefaultValue(HookReelConfig.DEFAULT_RAPPEL_ENABLED)
			.setSaveConsumer(value -> editing.rappelEnabled = value)
			.build());

		builder.setSavingRunnable(() -> HookReelConfigManager.save(editing));
		return builder.build();
	}
}
