package com.ikunkk02afk.hookandreel.network;

import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookModeController;
import com.ikunkk02afk.hookandreel.grapple.SwingController;
import com.ikunkk02afk.hookandreel.grapple.WallClingController;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

public final class HookReelNetworking {
	private HookReelNetworking() {
	}

	public static void initialize() {
		PayloadTypeRegistry.playS2C().register(
			GrappleChargeStatePayload.TYPE,
			GrappleChargeStatePayload.CODEC
		);
		PayloadTypeRegistry.playS2C().register(
			WallClingStatePayload.TYPE,
			WallClingStatePayload.CODEC
		);
		PayloadTypeRegistry.playC2S().register(
			SwitchModeRequestPayload.TYPE,
			SwitchModeRequestPayload.CODEC
		);
		PayloadTypeRegistry.playC2S().register(
			SwingInputPayload.TYPE,
			SwingInputPayload.CODEC
		);
		PayloadTypeRegistry.playC2S().register(
			StartReelUpRequestPayload.TYPE,
			StartReelUpRequestPayload.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
			SwitchModeRequestPayload.TYPE,
			(payload, context) -> HookModeController.handleSwitchRequest(context.player())
		);
		ServerPlayNetworking.registerGlobalReceiver(
			SwingInputPayload.TYPE,
			(payload, context) -> handleSwingInput(context.player(), payload)
		);
		ServerPlayNetworking.registerGlobalReceiver(
			StartReelUpRequestPayload.TYPE,
			(payload, context) -> SwingController.handleStartReelRequest(context.player())
		);
	}

	private static void handleSwingInput(ServerPlayer player, SwingInputPayload payload) {
		if (WallClingController.isActive(player)) {
			WallClingController.updateInput(
				player,
				payload.decodedLeftImpulse(),
				payload.decodedForwardImpulse()
			);
			return;
		}
		FishingHook hook = player.fishing;
		if (!(hook instanceof GrapplingBobberAccess access)) {
			return;
		}
		if (
			!access.hookAndReel$getHookState().isAnchored()
				|| access.hookAndReel$getLaunchOwnerUuid() == null
				|| !access.hookAndReel$getLaunchOwnerUuid().equals(player.getUUID())
		) {
			return;
		}
		access.hookAndReel$updateSwingInput(
			payload.decodedLeftImpulse(),
			payload.decodedForwardImpulse(),
			player.level().getGameTime()
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
