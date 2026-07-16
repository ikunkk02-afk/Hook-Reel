package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public final class FishingCatchCompletion {
	private FishingCatchCompletion() {
	}

	public static boolean complete(FishingHook hook) {
		if (
			hook.isRemoved()
				|| !(hook.getPlayerOwner() instanceof ServerPlayer player)
				|| !(hook instanceof GrapplingBobberAccess access)
		) {
			return false;
		}
		ItemStack rod = access.hookAndReel$getFishingRod();
		if (rod.isEmpty() || player.getItemInHand(access.hookAndReel$getFishingHand()) != rod) {
			return false;
		}

		int durability = hook.retrieve(rod);
		rod.hurtAndBreak(
			durability,
			player,
			LivingEntity.getSlotForHand(access.hookAndReel$getFishingHand())
		);
		hook.level().playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.FISHING_BOBBER_RETRIEVE,
			SoundSource.NEUTRAL,
			1.0F,
			0.4F / (hook.level().getRandom().nextFloat() * 0.4F + 0.8F)
		);
		player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
		return true;
	}
}
