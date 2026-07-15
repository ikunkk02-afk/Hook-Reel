package com.ikunkk02afk.hookandreel.grapple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public final class BlockPlacementSearch {
	public static final int RADIUS = 2;
	public static final int MAX_CANDIDATES = 125;
	private static final List<BlockPos> OFFSETS = createOffsets();

	private BlockPlacementSearch() {
	}

	public static List<BlockPos> offsets() {
		return OFFSETS;
	}

	public static BlockPos find(BlockPos center, Predicate<BlockPos> predicate) {
		for (BlockPos offset : OFFSETS) {
			BlockPos candidate = center.offset(offset);
			if (predicate.test(candidate)) {
				return candidate;
			}
		}
		return null;
	}

	private static List<BlockPos> createOffsets() {
		List<BlockPos> offsets = new ArrayList<>(MAX_CANDIDATES);
		for (int y = -RADIUS; y <= RADIUS; y++) {
			for (int x = -RADIUS; x <= RADIUS; x++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					offsets.add(new BlockPos(x, y, z));
				}
			}
		}
		offsets.sort(Comparator
			.comparingInt(BlockPlacementSearch::distanceSquared)
			.thenComparingInt(pos -> Math.abs(pos.getY()))
			.thenComparingInt(BlockPos::getY)
			.thenComparingInt(BlockPos::getX)
			.thenComparingInt(BlockPos::getZ));
		return List.copyOf(offsets);
	}

	private static int distanceSquared(BlockPos pos) {
		return pos.getX() * pos.getX() + pos.getY() * pos.getY() + pos.getZ() * pos.getZ();
	}
}
