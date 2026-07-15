package com.ikunkk02afk.hookandreel.tag;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTypeTags {
	public static final TagKey<EntityType<?>> GRAPPLE_PULL_BLACKLIST = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("grapple_pull_blacklist")
	);

	private ModEntityTypeTags() {
	}
}
