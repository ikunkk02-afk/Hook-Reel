package com.ikunkk02afk.hookandreel.network;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SwingInputPayload(byte leftImpulse, byte forwardImpulse) implements CustomPacketPayload {
	public static final Type<SwingInputPayload> TYPE = new Type<>(HookReel.id("swing_input"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SwingInputPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.BYTE,
		SwingInputPayload::leftImpulse,
		ByteBufCodecs.BYTE,
		SwingInputPayload::forwardImpulse,
		SwingInputPayload::new
	);

	public float decodedLeftImpulse() {
		return leftImpulse / 127.0F;
	}

	public float decodedForwardImpulse() {
		return forwardImpulse / 127.0F;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
