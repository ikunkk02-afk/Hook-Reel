package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.ikunkk02afk.hookandreel.network.GrappleChargeStatePayload;

public class HookReelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntityTypes.PULLED_BLOCK, PulledBlockRenderer::new);
		HookReelKeybindings.initialize();
		GrappleHudRenderer.initialize();
		ClientPlayNetworking.registerGlobalReceiver(
			GrappleChargeStatePayload.TYPE,
			(payload, context) -> GrappleChargeClientState.update(payload)
		);
		ClientPlayConnectionEvents.DISCONNECT.register(
			(handler, client) -> GrappleChargeClientState.clear()
		);
	}
}
