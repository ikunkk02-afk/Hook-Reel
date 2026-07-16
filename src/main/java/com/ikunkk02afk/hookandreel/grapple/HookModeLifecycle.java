package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.GrappleModeComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HookModeLifecycle {
	private HookModeLifecycle() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				repairHeldRod(player, player.getMainHandItem());
				repairHeldRod(player, player.getOffhandItem());
			}
		});
	}

	private static void repairHeldRod(ServerPlayer player, ItemStack stack) {
		if (stack.is(Items.FISHING_ROD)) {
			GrappleModeComponent.getAndRepair(player.level(), stack);
		}
	}
}
