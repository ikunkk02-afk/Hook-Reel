package com.ikunkk02afk.hookandreel.component;

import com.mojang.serialization.Codec;
import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModDataComponents {
	public static final DataComponentType<GrappleMode> GRAPPLE_MODE = DataComponentType
		.<GrappleMode>builder()
		.persistent(GrappleMode.CODEC)
		.networkSynchronized(GrappleMode.STREAM_CODEC)
		.build();
	public static final DataComponentType<Long> PULL_COOLDOWN_UNTIL = DataComponentType
		.<Long>builder()
		.persistent(Codec.LONG)
		.networkSynchronized(ByteBufCodecs.VAR_LONG)
		.build();
	public static final DataComponentType<Long> ANCHOR_COOLDOWN_UNTIL = DataComponentType
		.<Long>builder()
		.persistent(Codec.LONG)
		.networkSynchronized(ByteBufCodecs.VAR_LONG)
		.build();

	private ModDataComponents() {
	}

	public static void register() {
		Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			HookReel.id("grapple_mode"),
			GRAPPLE_MODE
		);
		Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			HookReel.id("grapple_cooldown_until"),
			PULL_COOLDOWN_UNTIL
		);
		Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			HookReel.id("anchor_cooldown_until"),
			ANCHOR_COOLDOWN_UNTIL
		);
	}
}
