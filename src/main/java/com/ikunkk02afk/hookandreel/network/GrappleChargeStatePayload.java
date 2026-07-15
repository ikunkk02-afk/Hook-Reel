package com.ikunkk02afk.hookandreel.network;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GrappleChargeStatePayload(boolean active, long startGameTime, int maximumChargeTicks)
	implements CustomPacketPayload {
	public static final Type<GrappleChargeStatePayload> TYPE = new Type<>(HookReel.id("grapple_charge_state"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GrappleChargeStatePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		GrappleChargeStatePayload::active,
		ByteBufCodecs.VAR_LONG,
		GrappleChargeStatePayload::startGameTime,
		ByteBufCodecs.VAR_INT,
		GrappleChargeStatePayload::maximumChargeTicks,
		GrappleChargeStatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
