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
	public static final TagKey<EntityType<?>> FISHABLE_AQUATIC_ENTITIES = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("fishable_aquatic_entities")
	);
	public static final TagKey<EntityType<?>> FISHABLE_LAND_ANIMALS = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("fishable_land_animals")
	);
	public static final TagKey<EntityType<?>> FISHABLE_LAND_MONSTERS = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("fishable_land_monsters")
	);
	public static final TagKey<EntityType<?>> FISHABLE_NETHER_ENTITIES = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("fishable_nether_entities")
	);
	public static final TagKey<EntityType<?>> FISHABLE_BOSS_ENTITIES = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("fishable_boss_entities")
	);
	public static final TagKey<EntityType<?>> UNFISHABLE_ENTITIES = TagKey.create(
		Registries.ENTITY_TYPE,
		HookReel.id("unfishable_entities")
	);

	private ModEntityTypeTags() {
	}
}
