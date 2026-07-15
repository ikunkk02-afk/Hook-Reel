package com.ikunkk02afk.hookandreel.component;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModDataComponents {
	public static final DataComponentType<GrappleMode> GRAPPLE_MODE = DataComponentType
		.<GrappleMode>builder()
		.persistent(GrappleMode.CODEC)
		.networkSynchronized(GrappleMode.STREAM_CODEC)
		.build();

	private ModDataComponents() {
	}

	public static void register() {
		Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			HookReel.id("grapple_mode"),
			GRAPPLE_MODE
		);
	}
}
