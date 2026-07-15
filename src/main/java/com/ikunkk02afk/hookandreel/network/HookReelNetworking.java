package com.ikunkk02afk.hookandreel.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class HookReelNetworking {
	private HookReelNetworking() {
	}

	public static void initialize() {
		PayloadTypeRegistry.playS2C().register(
			GrappleChargeStatePayload.TYPE,
			GrappleChargeStatePayload.CODEC
		);
	}

	public static void sendChargeState(
		ServerPlayer player,
		boolean active,
		long startGameTime,
		int maximumChargeTicks
	) {
		if (ServerPlayNetworking.canSend(player, GrappleChargeStatePayload.TYPE)) {
			ServerPlayNetworking.send(
				player,
				new GrappleChargeStatePayload(active, startGameTime, maximumChargeTicks)
			);
		}
	}
}
