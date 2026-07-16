package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.tag.ModEntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public enum FishingEntityCategory {
	AQUATIC(ModEntityTypeTags.FISHABLE_AQUATIC_ENTITIES),
	LAND_ANIMAL(ModEntityTypeTags.FISHABLE_LAND_ANIMALS),
	LAND_MONSTER(ModEntityTypeTags.FISHABLE_LAND_MONSTERS),
	NETHER(ModEntityTypeTags.FISHABLE_NETHER_ENTITIES),
	BOSS(ModEntityTypeTags.FISHABLE_BOSS_ENTITIES);

	private final TagKey<EntityType<?>> tag;

	FishingEntityCategory(TagKey<EntityType<?>> tag) {
		this.tag = tag;
	}

	public TagKey<EntityType<?>> tag() {
		return tag;
	}

	public boolean isEnabled(ServerLevel level, HookReelConfig config) {
		if (!config.allowFishingEntities) {
			return false;
		}
		return switch (this) {
			case AQUATIC -> config.allowAquaticEntities;
			case LAND_ANIMAL -> config.allowLandAnimals;
			case LAND_MONSTER -> config.allowLandMonsters;
			case NETHER -> config.allowNetherEntities
				&& (!config.netherEntitiesOnlyInNether || level.dimension() == Level.NETHER);
			case BOSS -> config.allowBossEntities;
		};
	}

	public double weight(HookReelConfig config) {
		return switch (this) {
			case AQUATIC -> config.aquaticEntityCategoryWeight;
			case LAND_ANIMAL -> config.landAnimalCategoryWeight;
			case LAND_MONSTER -> config.landMonsterCategoryWeight;
			case NETHER -> config.netherEntityCategoryWeight;
			case BOSS -> config.bossEntityCategoryWeight;
		};
	}

	public boolean isAquatic() {
		return this == AQUATIC;
	}

	public boolean isBoss() {
		return this == BOSS;
	}
}
