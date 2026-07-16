package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.enchantment.LuckyCatchLogic;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookAbilityCooldownManager;
import com.ikunkk02afk.hookandreel.grapple.HookState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LuckyInstantCatchController {
	private static final int MANUAL_BITE_WINDOW_TICKS = 40;

	private LuckyInstantCatchController() {
	}

	public static void preTick(FishingHook hook) {
		if (hook.level().isClientSide || !(hook instanceof GrapplingBobberAccess access)) {
			return;
		}
		if (access.hookAndReel$getFishingRod().isEmpty()) {
			return;
		}
		if (!isValidFishingState(hook, access, HookReelConfigManager.get())) {
			access.hookAndReel$resetLuckyCatchState();
		}
	}

	public static boolean tickValidWater(FishingHook hook) {
		if (hook.level().isClientSide || !(hook instanceof GrapplingBobberAccess access)) {
			return false;
		}
		HookReelConfig config = HookReelConfigManager.get();
		if (!isValidFishingState(hook, access, config) || access.hookAndReel$isInstantCatchTriggered()) {
			return false;
		}

		if (access.hookAndReel$isLuckyCatchArmed()) {
			if (config.luckyThreeAutoRetract) {
				access.hookAndReel$setInstantCatchTriggered(true);
				return FishingCatchCompletion.complete(hook);
			}
			if (access.hookAndReel$getNibbleTicks() <= 0) {
				access.hookAndReel$setLuckyCatchArmed(false);
				access.hookAndReel$setLuckyWaterTicks(0);
			}
			return false;
		}

		int waterTicks = access.hookAndReel$getLuckyWaterTicks() + 1;
		access.hookAndReel$setLuckyWaterTicks(waterTicks);
		int delayTicks = Math.max(
			5,
			HookAbilityCooldownManager.secondsToTicks(config.luckyThreeInstantCatchDelaySeconds)
		);
		if (waterTicks >= delayTicks) {
			access.hookAndReel$setLuckyCatchArmed(true);
			access.hookAndReel$armForcedBite(config.luckyThreeAutoRetract ? 2 : MANUAL_BITE_WINDOW_TICKS);
		}
		return false;
	}

	private static boolean isValidFishingState(
		FishingHook hook,
		GrapplingBobberAccess access,
		HookReelConfig config
	) {
		if (
			!config.luckyEnchantmentEnabled
				|| !config.luckyThreeInstantCatchEnabled
				|| access.hookAndReel$getHookState() != HookState.VANILLA
				|| hook.getHookedIn() != null
				|| hook.isRemoved()
				|| !hook.level().getFluidState(hook.blockPosition()).is(FluidTags.WATER)
				|| !(hook.getPlayerOwner() instanceof ServerPlayer player)
				|| player.isRemoved()
				|| !player.isAlive()
				|| player.level() != hook.level()
		) {
			return false;
		}
		ItemStack rod = access.hookAndReel$getFishingRod();
		return !rod.isEmpty()
			&& rod.is(Items.FISHING_ROD)
			&& player.getItemInHand(access.hookAndReel$getFishingHand()) == rod
			&& LuckyCatchLogic.getLevel(hook.level(), rod) >= 3;
	}
}
