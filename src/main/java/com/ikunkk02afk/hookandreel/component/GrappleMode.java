package com.ikunkk02afk.hookandreel.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum GrappleMode implements StringRepresentable {
	PULL("pull"),
	SWING("swing");

	public static final Codec<GrappleMode> CODEC = StringRepresentable.fromEnum(GrappleMode::values);
	public static final StreamCodec<ByteBuf, GrappleMode> STREAM_CODEC = ByteBufCodecs.idMapper(
		GrappleMode::byId,
		GrappleMode::ordinal
	);

	private final String serializedName;

	GrappleMode(String serializedName) {
		this.serializedName = serializedName;
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}

	private static GrappleMode byId(int id) {
		GrappleMode[] values = values();
		return id >= 0 && id < values.length ? values[id] : PULL;
	}
}
