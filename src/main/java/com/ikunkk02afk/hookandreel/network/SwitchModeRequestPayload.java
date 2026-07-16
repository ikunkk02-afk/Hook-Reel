package com.ikunkk02afk.hookandreel.network;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SwitchModeRequestPayload() implements CustomPacketPayload {
	public static final SwitchModeRequestPayload INSTANCE = new SwitchModeRequestPayload();
	public static final Type<SwitchModeRequestPayload> TYPE = new Type<>(HookReel.id("switch_mode"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SwitchModeRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
