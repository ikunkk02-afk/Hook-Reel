package com.ikunkk02afk.hookandreel.enchantment;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ModEnchantments {
	public static final ResourceKey<Enchantment> LUCKY_CATCH = ResourceKey.create(
		Registries.ENCHANTMENT,
		HookReel.id("lucky_catch")
	);
	public static final ResourceKey<Enchantment> GRAPPLING_HOOK = ResourceKey.create(
		Registries.ENCHANTMENT,
		HookReel.id("grappling_hook")
	);

	private ModEnchantments() {
	}

	public static boolean isLuckyAndLuckOfTheSeaPair(
		Holder<Enchantment> first,
		Holder<Enchantment> second
	) {
		return first.is(LUCKY_CATCH) && second.is(Enchantments.LUCK_OF_THE_SEA)
			|| first.is(Enchantments.LUCK_OF_THE_SEA) && second.is(LUCKY_CATCH);
	}
}
