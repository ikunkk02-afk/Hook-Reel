package com.ikunkk02afk.hookandreel.network;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WallClingStatePayload(boolean active, long endGameTime) implements CustomPacketPayload {
	public static final Type<WallClingStatePayload> TYPE = new Type<>(HookReel.id("wall_cling_state"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WallClingStatePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		WallClingStatePayload::active,
		ByteBufCodecs.VAR_LONG,
		WallClingStatePayload::endGameTime,
		WallClingStatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
