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
			Component.translatable("category.hook_and_reel.future_grapple")
		);
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
