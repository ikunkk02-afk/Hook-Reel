package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.network.SwitchModeRequestPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class HookReelKeybindings {
	private static final KeyMapping SWITCH_MODE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.hook_and_reel.switch_mode",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_V,
		"key.category.hook_and_reel"
	));

	private HookReelKeybindings() {
	}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (SWITCH_MODE.consumeClick()) {
				if (client.player != null && ClientPlayNetworking.canSend(SwitchModeRequestPayload.TYPE)) {
					ClientPlayNetworking.send(SwitchModeRequestPayload.INSTANCE);
				}
			}
			SwingClientController.tick(client);
		});
	}
}
