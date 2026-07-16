package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.tag.ModEntityTypeTags;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;

public final class FishableEntitySelector {
	private FishableEntitySelector() {
	}

	public static Selection select(ServerLevel level, HookReelConfig config) {
		if (!config.allowFishingEntities) {
			return null;
		}
		Registry<EntityType<?>> registry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
		List<CategoryPool> pools = new ArrayList<>();
		for (FishingEntityCategory category : FishingEntityCategory.values()) {
			double weight = category.weight(config);
			if (!category.isEnabled(level, config) || !FishingEntityCategoryWeights.isPositiveFinite(weight)) {
				continue;
			}
			LinkedHashSet<EntityType<?>> uniqueCandidates = registry.stream()
				.filter(type -> isAllowed(type, category, level, config))
				.collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
			if (!uniqueCandidates.isEmpty()) {
				pools.add(new CategoryPool(category, List.copyOf(uniqueCandidates), weight));
			}
		}
		if (pools.isEmpty()) {
			return null;
		}

		double[] weights = pools.stream().mapToDouble(CategoryPool::weight).toArray();
		int selectedPoolIndex = FishingEntityCategoryWeights.selectIndex(
			weights,
			level.getRandom().nextDouble()
		);
		if (selectedPoolIndex < 0) {
			return null;
		}
		CategoryPool selectedPool = pools.get(selectedPoolIndex);
		EntityType<?> selectedType = selectedPool.candidates().get(
			level.getRandom().nextInt(selectedPool.candidates().size())
		);
		return new Selection(selectedPool.category(), selectedType);
	}

	public static boolean isAllowed(
		EntityType<?> type,
		FishingEntityCategory category,
		ServerLevel level,
		HookReelConfig config
	) {
		if (
			!category.isEnabled(level, config)
				|| !type.is(category.tag())
				|| type.is(ModEntityTypeTags.UNFISHABLE_ENTITIES)
				|| !type.canSummon()
				|| type == EntityType.PLAYER
		) {
			return false;
		}
		return type != EntityType.ENDER_DRAGON
			|| (category.isBoss() && config.allowBossEntities && config.allowEnderDragonFishing);
	}

	public record Selection(FishingEntityCategory category, EntityType<?> type) {
	}

	private record CategoryPool(
		FishingEntityCategory category,
		List<EntityType<?>> candidates,
		double weight
	) {
	}
}
