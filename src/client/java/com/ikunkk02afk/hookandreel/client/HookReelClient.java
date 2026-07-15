package com.ikunkk02afk.hookandreel.client;

import net.fabricmc.api.ClientModInitializer;

public class HookReelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HookReelKeybindings.initialize();
	}
}
