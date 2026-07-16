package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public final class AnchorHookLogic {
	private AnchorHookLogic() {
	}

	public static int getLevel(Level level, ItemStack stack) {
		Registry<Enchantment> enchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> anchorHook = enchantments.getHolderOrThrow(ModEnchantments.ANCHOR_HOOK);
		return EnchantmentHelper.getItemEnchantmentLevel(anchorHook, stack);
	}

	public static double maximumRangeForLevel(int level, HookReelConfig config) {
		return switch (Math.clamp(level, 1, 3)) {
			case 1 -> config.anchorLevel1MaxRange;
			case 2 -> config.anchorLevel2MaxRange;
			default -> config.anchorLevel3MaxRange;
		};
	}
}
