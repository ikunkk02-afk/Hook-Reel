package com.ikunkk02afk.hookandreel;

import net.fabricmc.api.ModInitializer;

import com.ikunkk02afk.hookandreel.component.ModDataComponents;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.entity.ModEntityTypes;
import com.ikunkk02afk.hookandreel.fishing.FishingEntityPullController;
import com.ikunkk02afk.hookandreel.grapple.HookModeLifecycle;
import com.ikunkk02afk.hookandreel.grapple.WallClingController;
import com.ikunkk02afk.hookandreel.network.HookReelNetworking;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookReel implements ModInitializer {
	public static final String MOD_ID = "hook_and_reel";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HookReelConfigManager.load();
		ModDataComponents.register();
		ModEntityTypes.register();
		HookReelNetworking.initialize();
		HookModeLifecycle.initialize();
		WallClingController.initialize();
		FishingEntityPullController.initialize();
		LOGGER.info("Hook & Reel initialized");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
