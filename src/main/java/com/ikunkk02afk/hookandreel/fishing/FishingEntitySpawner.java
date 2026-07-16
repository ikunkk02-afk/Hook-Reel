package com.ikunkk02afk.hookandreel.fishing;

import com.ikunkk02afk.hookandreel.HookReel;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.AABB;

public final class FishingEntitySpawner {
	private static final int NORMAL_HORIZONTAL_RADIUS = 4;
	private static final int NORMAL_VERTICAL_RADIUS = 2;
	private static final int NORMAL_CANDIDATE_LIMIT = 128;
	private static final int LARGE_HORIZONTAL_RADIUS = 8;
	private static final int LARGE_VERTICAL_RADIUS = 4;
	private static final int LARGE_CANDIDATE_LIMIT = 256;
	private static final float LARGE_WIDTH = 1.5F;
	private static final float LARGE_HEIGHT = 2.0F;

	private FishingEntitySpawner() {
	}

	public static SpawnedEntity trySpawn(
		FishingHook hook,
		FishableEntitySelector.Selection selection,
		ServerPlayer player,
		HookReelConfig config
	) {
		if (
			selection == null
				|| !(hook.level() instanceof ServerLevel level)
				|| player.level() != level
				|| !FishableEntitySelector.isAllowed(selection.type(), selection.category(), level, config)
				|| selection.type() == EntityType.ENDER_DRAGON
		) {
			return null;
		}

		boolean large = isLarge(selection);
		List<BlockPos> candidates = candidates(
			hook.blockPosition(),
			large ? LARGE_HORIZONTAL_RADIUS : NORMAL_HORIZONTAL_RADIUS,
			large ? LARGE_VERTICAL_RADIUS : NORMAL_VERTICAL_RADIUS,
			large ? LARGE_CANDIDATE_LIMIT : NORMAL_CANDIDATE_LIMIT,
			level.getRandom()
		);
		for (BlockPos pos : candidates) {
			if (!isCandidateSafe(level, selection, pos)) {
				continue;
			}
			Mob mob = createMob(level, selection.type(), pos);
			if (mob == null) {
				return null;
			}
			AABB actualBox = mob.getBoundingBox();
			if (
				!isFiniteBox(actualBox)
					|| !areChunksLoaded(level, actualBox)
					|| !isEnvironmentSafe(level, selection.category(), pos, actualBox)
					|| !level.noCollision(actualBox)
					|| intersectsPlayer(level, actualBox)
			) {
				mob.discard();
				continue;
			}
			try {
				if (!level.addFreshEntity(mob)) {
					mob.discard();
					continue;
				}
				return new SpawnedEntity(mob, large);
			} catch (RuntimeException exception) {
				mob.discard();
				HookReel.LOGGER.warn(
					"Could not add fishing entity {} safely; falling back to loot",
					EntityType.getKey(selection.type()),
					exception
				);
				return null;
			}
		}
		return null;
	}

	private static Mob createMob(ServerLevel level, EntityType<?> type, BlockPos pos) {
		Entity created = null;
		try {
			created = type.create(level, null, pos, MobSpawnType.TRIGGERED, false, false);
			if (created instanceof Mob mob) {
				return mob;
			}
			if (created != null) {
				created.discard();
			}
			return null;
		} catch (RuntimeException exception) {
			if (created != null) {
				created.discard();
			}
			HookReel.LOGGER.warn(
				"Could not create fishing entity {} safely; falling back to loot",
				EntityType.getKey(type),
				exception
			);
			return null;
		}
	}

	private static boolean isCandidateSafe(
		ServerLevel level,
		FishableEntitySelector.Selection selection,
		BlockPos pos
	) {
		AABB spawnBox = selection.type().getSpawnAABB(
			pos.getX() + 0.5D,
			pos.getY(),
			pos.getZ() + 0.5D
		);
		return isFiniteBox(spawnBox)
			&& areChunksLoaded(level, spawnBox)
			&& level.isPositionEntityTicking(pos)
			&& isInsideWorldBorder(level, spawnBox)
			&& isEnvironmentSafe(level, selection.category(), pos, spawnBox)
			&& level.noCollision(spawnBox)
			&& !intersectsPlayer(level, spawnBox);
	}

	private static boolean isEnvironmentSafe(
		ServerLevel level,
		FishingEntityCategory category,
		BlockPos pos,
		AABB box
	) {
		return category.isAquatic()
			? level.getFluidState(pos).is(FluidTags.WATER)
			: !level.containsAnyLiquid(box);
	}

	private static boolean isInsideWorldBorder(ServerLevel level, AABB box) {
		return level.getWorldBorder().isWithinBounds(BlockPos.containing(box.minX, box.minY, box.minZ))
			&& level.getWorldBorder().isWithinBounds(BlockPos.containing(
				Math.nextDown(box.maxX),
				Math.nextDown(box.maxY),
				Math.nextDown(box.maxZ)
			));
	}

	private static boolean areChunksLoaded(ServerLevel level, AABB box) {
		int minimumChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.minX));
		int maximumChunkX = SectionPos.blockToSectionCoord((int) Math.floor(Math.nextDown(box.maxX)));
		int minimumChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.minZ));
		int maximumChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(Math.nextDown(box.maxZ)));
		for (int chunkX = minimumChunkX; chunkX <= maximumChunkX; chunkX++) {
			for (int chunkZ = minimumChunkZ; chunkZ <= maximumChunkZ; chunkZ++) {
				if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean intersectsPlayer(ServerLevel level, AABB box) {
		return !level.getPlayers(player -> player.getBoundingBox().intersects(box)).isEmpty();
	}

	private static boolean isFiniteBox(AABB box) {
		return positiveFinite(box.getXsize())
			&& positiveFinite(box.getYsize())
			&& positiveFinite(box.getZsize())
			&& Double.isFinite(box.minX)
			&& Double.isFinite(box.minY)
			&& Double.isFinite(box.minZ)
			&& Double.isFinite(box.maxX)
			&& Double.isFinite(box.maxY)
			&& Double.isFinite(box.maxZ);
	}

	private static boolean positiveFinite(double value) {
		return Double.isFinite(value) && value > 0.0D;
	}

	private static boolean isLarge(FishableEntitySelector.Selection selection) {
		return selection.category().isBoss()
			|| selection.type().getWidth() > LARGE_WIDTH
			|| selection.type().getHeight() > LARGE_HEIGHT;
	}

	private static List<BlockPos> candidates(
		BlockPos center,
		int horizontalRadius,
		int verticalRadius,
		int limit,
		RandomSource random
	) {
		List<BlockPos> offsets = new ArrayList<>();
		for (int y = -verticalRadius; y <= verticalRadius; y++) {
			for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
				for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
					if (x != 0 || y != 0 || z != 0) {
						offsets.add(center.offset(x, y, z));
					}
				}
			}
		}
		for (int index = offsets.size() - 1; index > 0; index--) {
			Collections.swap(offsets, index, random.nextInt(index + 1));
		}
		List<BlockPos> result = new ArrayList<>(Math.min(limit, offsets.size() + 1));
		result.add(center.immutable());
		int remaining = Math.min(limit - 1, offsets.size());
		result.addAll(offsets.subList(0, remaining));
		return result;
	}

	public record SpawnedEntity(Mob mob, boolean large) {
	}
}
