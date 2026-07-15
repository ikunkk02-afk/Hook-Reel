package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public final class GrappleLauncher {
	private GrappleLauncher() {
	}

	public static void launch(
		FishingRodItem fishingRod,
		Level level,
		Player player,
		InteractionHand hand,
		ItemStack stack,
		int chargeTicks
	) {
		HookReelConfig config = HookReelConfigManager.get();
		int enchantmentLevel = GrappleEnchantmentLogic.getLevel(level, stack);
		int maximumChargeTicks = GrappleEnchantmentLogic.secondsToTicks(config.maxChargeTimeSeconds);
		double maximumRange = GrappleEnchantmentLogic.maximumRangeForLevel(enchantmentLevel, config);
		double actualRange = GrappleMath.actualRange(
			chargeTicks,
			maximumChargeTicks,
			config.minimumGrappleRange,
			maximumRange
		);

		level.playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.FISHING_BOBBER_THROW,
			SoundSource.NEUTRAL,
			0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		if (level instanceof ServerLevel serverLevel) {
			int lureSpeed = (int) (EnchantmentHelper.getFishingTimeReduction(serverLevel, stack, player) * 20.0F);
			int luck = EnchantmentHelper.getFishingLuckBonus(serverLevel, stack, player);
			FishingHook hook = new FishingHook(player, level, luck, lureSpeed);
			((GrapplingBobberAccess) hook).hookAndReel$initializeGrapple(stack, hand, actualRange);
			Vec3 initialDirection = hook.getDeltaMovement();
			if (initialDirection.lengthSqr() > 1.0E-8D) {
				hook.setDeltaMovement(initialDirection.normalize().scale(GrappleMath.launchSpeed(actualRange)));
			}
			level.addFreshEntity(hook);
		}

		player.awardStat(Stats.ITEM_USED.get(fishingRod));
		player.gameEvent(GameEvent.ITEM_INTERACT_START);
	}
}
