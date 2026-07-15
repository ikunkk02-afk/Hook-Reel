package com.ikunkk02afk.hookandreel.mixin;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.grapple.GrappleCooldown;
import com.ikunkk02afk.hookandreel.grapple.GrappleEnchantmentLogic;
import com.ikunkk02afk.hookandreel.grapple.GrappleLauncher;
import com.ikunkk02afk.hookandreel.grapple.GrapplePullController;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.network.HookReelNetworking;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin extends Item {
	private static final int GRAPPLE_USE_DURATION = 72000;

	protected FishingRodItemMixin(Properties properties) {
		super(properties);
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void hookAndReel$startChargeOrCancel(
		Level level,
		Player player,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
	) {
		ItemStack stack = player.getItemInHand(hand);
		int grapplingLevel = GrappleEnchantmentLogic.getLevel(level, stack);
		if (grapplingLevel <= 0) {
			return;
		}

		FishingHook existingHook = player.fishing;
		if (existingHook != null) {
			if (!level.isClientSide && existingHook instanceof GrapplingBobberAccess access && access.hookAndReel$isGrapple()) {
				if (access.hookAndReel$isPulling(existingHook.getHookedIn())) {
					GrapplePullController.manualCancel(existingHook);
					playRetrieveFeedback(level, player);
					cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, false));
				} else {
					GrappleCooldown.set(access.hookAndReel$getLaunchRod(), level, GrappleCooldown.CANCEL_COOLDOWN_TICKS);
				}
			}
			return;
		}

		HookReelConfig config = HookReelConfigManager.get();
		if (!config.grapplingHookEnabled) {
			return;
		}
		int fullCooldownTicks = Math.max(0, config.grappleCooldownSeconds * 20);
		int expectedMaximum = Math.max(fullCooldownTicks, GrappleCooldown.CANCEL_COOLDOWN_TICKS);
		long remaining = level.isClientSide
			? GrappleCooldown.remainingTicks(stack, level, expectedMaximum)
			: GrappleCooldown.sanitizeAndGetRemaining(stack, level, expectedMaximum);
		if (remaining > 0L) {
			if (player instanceof ServerPlayer) {
				String seconds = String.format(Locale.ROOT, "%.1f", remaining / 20.0D);
				player.displayClientMessage(
					Component.translatable("message.hook_and_reel.grapple_cooldown", seconds),
					true
				);
			}
			return;
		}

		player.startUsingItem(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			int maximumChargeTicks = GrappleEnchantmentLogic.secondsToTicks(config.maxChargeTimeSeconds);
			HookReelNetworking.sendChargeState(
				serverPlayer,
				true,
				level.getGameTime(),
				maximumChargeTicks
			);
		}
		cir.setReturnValue(InteractionResultHolder.consume(stack));
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return GRAPPLE_USE_DURATION;
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof Player player) || player.fishing != null) {
			return;
		}
		HookReelConfig config = HookReelConfigManager.get();
		if (!config.grapplingHookEnabled || GrappleEnchantmentLogic.getLevel(level, stack) <= 0) {
			return;
		}
		int chargeTicks = Math.max(0, GRAPPLE_USE_DURATION - remainingUseTicks);
		if (player instanceof ServerPlayer serverPlayer) {
			HookReelNetworking.sendChargeState(serverPlayer, false, level.getGameTime(), 0);
		}
		GrappleLauncher.launch(
			(FishingRodItem) (Object) this,
			level,
			player,
			player.getUsedItemHand(),
			stack,
			chargeTicks
		);
	}

	private static void playRetrieveFeedback(Level level, Player player) {
		level.playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.FISHING_BOBBER_RETRIEVE,
			SoundSource.NEUTRAL,
			1.0F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
	}
}
