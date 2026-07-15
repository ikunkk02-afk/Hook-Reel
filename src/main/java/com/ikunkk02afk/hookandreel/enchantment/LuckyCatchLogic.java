package com.ikunkk02afk.hookandreel.enchantment;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class LuckyCatchLogic {
	private LuckyCatchLogic() {
	}

	public static float addFishingLuck(float vanillaLuck, ItemStack rod, ServerLevel level) {
		Registry<Enchantment> enchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> luckyCatch = enchantments.getHolderOrThrow(ModEnchantments.LUCKY_CATCH);
		int luckyLevel = EnchantmentHelper.getItemEnchantmentLevel(luckyCatch, rod);
		if (luckyLevel <= 0) {
			return vanillaLuck;
		}

		Holder<Enchantment> luckOfTheSea = enchantments.getHolderOrThrow(Enchantments.LUCK_OF_THE_SEA);
		boolean hasLuckOfTheSea = EnchantmentHelper.getItemEnchantmentLevel(luckOfTheSea, rod) > 0;
		return vanillaLuck + calculateExtraLuck(
			luckyLevel,
			hasLuckOfTheSea,
			HookReelConfigManager.get()
		);
	}

	public static float calculateExtraLuck(
		int luckyLevel,
		boolean hasLuckOfTheSea,
		HookReelConfig config
	) {
		if (!config.luckyEnchantmentEnabled || luckyLevel <= 0) {
			return 0.0F;
		}
		if (hasLuckOfTheSea && !config.allowStackWithLuckOfTheSea) {
			return 0.0F;
		}
		return (float) (luckyLevel * config.luckyBonusPerLevel);
	}
}
