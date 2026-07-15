package com.ikunkk02afk.hookandreel.mixin;

import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentCompatibilityMixin {
	@Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
	private static void hookAndReel$applyConfiguredCompatibility(
		Holder<Enchantment> first,
		Holder<Enchantment> second,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (
			!HookReelConfigManager.get().allowStackWithLuckOfTheSea
				&& ModEnchantments.isLuckyAndLuckOfTheSeaPair(first, second)
		) {
			cir.setReturnValue(false);
		}
	}
}
