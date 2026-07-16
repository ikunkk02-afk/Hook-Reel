package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.enchantment.LuckyCatchLogic;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import java.util.Collections;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public final class FishingEntityController {
	private FishingEntityController() {
	}

	public static boolean tryCompleteEntityCatch(FishingHook hook, ItemStack rod) {
		if (
			!(hook.level() instanceof ServerLevel level)
				|| !(hook.getPlayerOwner() instanceof ServerPlayer player)
				|| hook.getHookedIn() != null
		) {
			return false;
		}
		HookReelConfig config = HookReelConfigManager.get();
		int luckyLevel = LuckyCatchLogic.getLevel(level, rod);
		double chance = FishingEntityChance.calculate(config, luckyLevel);
		if (chance <= 0.0D || level.getRandom().nextDouble() >= chance) {
			return false;
		}

		FishableEntitySelector.Selection selection = FishableEntitySelector.select(level, config);
		if (selection == null) {
			return false;
		}
		FishingEntitySpawner.SpawnedEntity spawned = FishingEntitySpawner.trySpawn(
			hook,
			selection,
			player,
			config
		);
		if (spawned == null) {
			return false;
		}
		Mob caught = spawned.mob();

		((GrapplingBobberAccess) hook).hookAndReel$setVanillaHookedEntity(caught);
		CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, rod, hook, Collections.emptyList());
		player.awardStat(Stats.FISH_CAUGHT, 1);
		level.addFreshEntity(
			new ExperienceOrb(
				level,
				player.getX(),
				player.getY() + 0.5D,
				player.getZ() + 0.5D,
				level.getRandom().nextInt(6) + 1
			)
		);
		FishingEntityPullController.start(player, caught, spawned.large());
		hook.discard();
		return true;
	}
}
