package com.ikunkk02afk.hookandreel.entity;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	public static final EntityType<PulledBlockEntity> PULLED_BLOCK = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		HookReel.id("pulled_block"),
		EntityType.Builder.<PulledBlockEntity>of(PulledBlockEntity::new, MobCategory.MISC)
			.noSummon()
			.sized(0.98F, 0.98F)
			.clientTrackingRange(10)
			.updateInterval(1)
			.build("pulled_block")
	);

	private ModEntityTypes() {
	}

	public static void register() {
		// Loading this class performs the registry insertion.
	}
}
