package com.ikunkk02afk.hookandreel.tag;

import com.ikunkk02afk.hookandreel.HookReel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
	public static final TagKey<Block> GRAPPLE_IMMOVABLE = TagKey.create(
		Registries.BLOCK,
		HookReel.id("grapple_immovable")
	);
	public static final TagKey<Block> GRAPPLE_MULTIBLOCK_UNSAFE = TagKey.create(
		Registries.BLOCK,
		HookReel.id("grapple_multiblock_unsafe")
	);

	private ModBlockTags() {
	}
}
