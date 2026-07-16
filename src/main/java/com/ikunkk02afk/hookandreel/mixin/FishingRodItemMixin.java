package com.ikunkk02afk.hookandreel.mixin;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.component.GrappleModeComponent;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.grapple.AnchorHookLogic;
import com.ikunkk02afk.hookandreel.grapple.GrappleEnchantmentLogic;
import com.ikunkk02afk.hookandreel.grapple.GrappleLauncher;
import com.ikunkk02afk.hookandreel.grapple.GrapplePullController;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookAbilityCooldown;
import com.ikunkk02afk.hookandreel.grapple.HookAbilityCooldownManager;
import com.ikunkk02afk.hookandreel.grapple.HookState;
import com.ikunkk02afk.hookandreel.grapple.SwingController;
import com.ikunkk02afk.hookandreel.grapple.SwingDetachReason;
import com.ikunkk02afk.hookandreel.network.HookReelNetworking;
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
		int anchorLevel = AnchorHookLogic.getLevel(level, stack);
		if (grapplingLevel <= 0 && anchorLevel <= 0) {
			return;
		}
		GrappleMode mode = level.isClientSide
			? GrappleModeComponent.getEffective(level, stack)
			: GrappleModeComponent.getAndRepair(level, stack);
		HookReelConfig config = HookReelConfigManager.get();

		FishingHook existingHook = player.fishing;
		if (existingHook != null) {
			if (existingHook instanceof GrapplingBobberAccess access && access.hookAndReel$isGrapple()) {
				boolean swingHook = access.hookAndReel$getHookState().isAnchored()
					|| !level.isClientSide && access.hookAndReel$getLaunchMode() == GrappleMode.SWING;
				if (swingHook) {
					if (!level.isClientSide) {
						SwingController.detach(existingHook, SwingDetachReason.PLAYER_RETRIEVE);
						playRetrieveFeedback(level, player);
					}
					cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
				} else if (!level.isClientSide && access.hookAndReel$isPulling(existingHook.getHookedIn())) {
					GrapplePullController.manualCancel(existingHook);
					playRetrieveFeedback(level, player);
					cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, false));
				} else if (!level.isClientSide) {
					HookAbilityCooldownManager.set(
						access.hookAndReel$getLaunchRod(),
						level,
						HookAbilityCooldown.PULL,
						config.grapplingHookCooldownSeconds > 0.0D
							? HookAbilityCooldownManager.PULL_CANCEL_COOLDOWN_TICKS
							: 0
					);
				}
			}
			return;
		}

		if (mode == GrappleMode.PULL && !config.grapplingHookEnabled || mode == GrappleMode.SWING && !config.anchorHookEnabled) {
			return;
		}
		HookAbilityCooldown ability = mode == GrappleMode.PULL
			? HookAbilityCooldown.PULL
			: HookAbilityCooldown.ANCHOR;
		int fullCooldownTicks = HookAbilityCooldownManager.secondsToTicks(
			mode == GrappleMode.PULL
				? config.grapplingHookCooldownSeconds
				: config.anchorHookCooldownSeconds
		);
		int expectedMaximum = fullCooldownTicks <= 0
			? 0
			: mode == GrappleMode.PULL
				? Math.max(fullCooldownTicks, HookAbilityCooldownManager.PULL_CANCEL_COOLDOWN_TICKS)
				: Math.max(
					fullCooldownTicks,
					HookAbilityCooldownManager.secondsToTicks(config.anchorHookFailedCastDelaySeconds)
				);
		long remaining = level.isClientSide
			? HookAbilityCooldownManager.remainingTicks(stack, level, ability, expectedMaximum)
			: HookAbilityCooldownManager.sanitizeAndGetRemaining(stack, level, ability, expectedMaximum);
		if (remaining > 0L) {
			if (player instanceof ServerPlayer) {
				String seconds = HookAbilityCooldownManager.formatRemainingSeconds(remaining);
				player.displayClientMessage(
					Component.translatable(
						mode == GrappleMode.PULL
							? "message.hook_and_reel.grapple_cooldown"
							: "message.hook_and_reel.anchor_cooldown",
						seconds
					),
					true
				);
			}
			cir.setReturnValue(InteractionResultHolder.fail(stack));
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

	@Inject(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
			shift = At.Shift.AFTER
		)
	)
	private void hookAndReel$captureVanillaFishingCast(
		Level level,
		Player player,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
	) {
		if (
			!level.isClientSide
				&& player.fishing instanceof GrapplingBobberAccess access
				&& !access.hookAndReel$isGrapple()
		) {
			access.hookAndReel$initializeFishingCast(player.getItemInHand(hand), hand);
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return GRAPPLE_USE_DURATION;
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof Player player)) {
			return;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			HookReelNetworking.sendChargeState(serverPlayer, false, level.getGameTime(), 0);
		}
		if (player.fishing != null) {
			return;
		}
		HookReelConfig config = HookReelConfigManager.get();
		int grapplingLevel = GrappleEnchantmentLogic.getLevel(level, stack);
		int anchorLevel = AnchorHookLogic.getLevel(level, stack);
		if (grapplingLevel <= 0 && anchorLevel <= 0) {
			return;
		}
		GrappleMode mode = level.isClientSide
			? GrappleModeComponent.getEffective(level, stack)
			: GrappleModeComponent.getAndRepair(level, stack);
		if (mode == GrappleMode.PULL && !config.grapplingHookEnabled || mode == GrappleMode.SWING && !config.anchorHookEnabled) {
			return;
		}
		int chargeTicks = Math.max(0, GRAPPLE_USE_DURATION - remainingUseTicks);
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
