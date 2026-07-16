package com.ikunkk02afk.hookandreel.network;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StartReelUpRequestPayload() implements CustomPacketPayload {
	public static final StartReelUpRequestPayload INSTANCE = new StartReelUpRequestPayload();
	public static final Type<StartReelUpRequestPayload> TYPE = new Type<>(HookReel.id("start_reel_up"));
	public static final StreamCodec<RegistryFriendlyByteBuf, StartReelUpRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
