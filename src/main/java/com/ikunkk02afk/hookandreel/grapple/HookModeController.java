package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.component.GrappleModeComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HookModeController {
	private HookModeController() {
	}

	public static void handleSwitchRequest(ServerPlayer player) {
		RodSelection selection = selectRod(player);
		if (selection == null) {
			return;
		}
		ItemStack rod = selection.stack();
		int pullLevel = GrappleEnchantmentLogic.getLevel(player.level(), rod);
		int swingLevel = AnchorHookLogic.getLevel(player.level(), rod);
		if (pullLevel <= 0 && swingLevel <= 0) {
			GrappleModeComponent.getAndRepair(player.level(), rod);
			return;
		}
		if (player.isUsingItem() && player.getUseItem() == rod) {
			message(player, "message.hook_and_reel.hook_busy");
			return;
		}
		if (pullLevel <= 0) {
			GrappleModeComponent.set(rod, GrappleMode.SWING);
			synchronize(player);
			message(player, "message.hook_and_reel.swing_only");
			return;
		}
		if (swingLevel <= 0) {
			GrappleModeComponent.set(rod, GrappleMode.PULL);
			synchronize(player);
			message(player, "message.hook_and_reel.pull_only");
			return;
		}

		FishingHook activeHook = selection.activeHook();
		if (activeHook != null) {
			GrapplingBobberAccess access = (GrapplingBobberAccess) activeHook;
			if (access.hookAndReel$getLaunchMode() == GrappleMode.PULL && access.hookAndReel$getHookState().isPulling()) {
				message(player, "message.hook_and_reel.hook_busy");
				return;
			}
			if (access.hookAndReel$getLaunchMode() == GrappleMode.SWING) {
				SwingController.detach(activeHook, SwingDetachReason.MODE_SWITCH);
			} else {
				GrapplePullController.manualCancel(activeHook);
			}
		}

		GrappleMode next = GrappleModeComponent.getAndRepair(player.level(), rod) == GrappleMode.PULL
			? GrappleMode.SWING
			: GrappleMode.PULL;
		WallClingController.clear(player);
		GrappleModeComponent.set(rod, next);
		synchronize(player);
		message(
			player,
			next == GrappleMode.PULL ? "message.hook_and_reel.mode_pull" : "message.hook_and_reel.mode_swing"
		);
	}

	private static RodSelection selectRod(ServerPlayer player) {
		if (player.fishing instanceof GrapplingBobberAccess access && access.hookAndReel$isGrapple()) {
			ItemStack launchRod = access.hookAndReel$getLaunchRod();
			InteractionHand launchHand = access.hookAndReel$getLaunchHand();
			if (player.getItemInHand(launchHand) == launchRod && launchRod.is(Items.FISHING_ROD)) {
				return new RodSelection(launchRod, player.fishing);
			}
		}
		ItemStack mainHand = player.getMainHandItem();
		if (isRelatedRod(player, mainHand)) {
			return new RodSelection(mainHand, null);
		}
		ItemStack offHand = player.getOffhandItem();
		if (isRelatedRod(player, offHand)) {
			return new RodSelection(offHand, null);
		}
		return null;
	}

	private static boolean isRelatedRod(ServerPlayer player, ItemStack stack) {
		return stack.is(Items.FISHING_ROD)
			&& (GrappleEnchantmentLogic.getLevel(player.level(), stack) > 0 || AnchorHookLogic.getLevel(player.level(), stack) > 0);
	}

	private static void synchronize(ServerPlayer player) {
		player.getInventory().setChanged();
		player.inventoryMenu.broadcastChanges();
		if (player.containerMenu != player.inventoryMenu) {
			player.containerMenu.broadcastChanges();
		}
	}

	private static void message(ServerPlayer player, String translationKey) {
		player.displayClientMessage(Component.translatable(translationKey), true);
	}

	private record RodSelection(ItemStack stack, FishingHook activeHook) {
	}
}
