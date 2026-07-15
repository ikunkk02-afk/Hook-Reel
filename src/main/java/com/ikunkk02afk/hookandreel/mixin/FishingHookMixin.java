package com.ikunkk02afk.hookandreel.mixin;

import com.ikunkk02afk.hookandreel.enchantment.LuckyCatchLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
	@Unique
	private ItemStack hookAndReel$retrievalRod = ItemStack.EMPTY;

	@Inject(method = "retrieve", at = @At("HEAD"))
	private void hookAndReel$captureRetrievalRod(
		ItemStack rod,
		CallbackInfoReturnable<Integer> cir
	) {
		hookAndReel$retrievalRod = rod;
	}

	@ModifyArg(
		method = "retrieve",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withLuck(F)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;"
		),
		index = 0
	)
	private float hookAndReel$addLuckyCatchLuck(float vanillaLuck) {
		FishingHook hook = (FishingHook) (Object) this;
		return LuckyCatchLogic.addFishingLuck(
			vanillaLuck,
			hookAndReel$retrievalRod,
			(ServerLevel) hook.level()
		);
	}

	@Inject(method = "retrieve", at = @At("RETURN"))
	private void hookAndReel$clearRetrievalRod(
		ItemStack rod,
		CallbackInfoReturnable<Integer> cir
	) {
		hookAndReel$retrievalRod = ItemStack.EMPTY;
	}
}
