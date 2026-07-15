package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class BlockPlacementSearchTest {
	@Test
	void radiusTwoSearchHasExactlyOneHundredTwentyFiveDeterministicCandidates() {
		assertEquals(BlockPlacementSearch.MAX_CANDIDATES, BlockPlacementSearch.offsets().size());
		assertEquals(BlockPos.ZERO, BlockPlacementSearch.offsets().getFirst());
		assertEquals(
			BlockPlacementSearch.MAX_CANDIDATES,
			new HashSet<>(BlockPlacementSearch.offsets()).size()
		);
		assertTrue(BlockPlacementSearch.offsets().stream().allMatch(pos ->
			Math.abs(pos.getX()) <= 2 && Math.abs(pos.getY()) <= 2 && Math.abs(pos.getZ()) <= 2
		));
	}

	@Test
	void searchStopsAtFirstSafeCandidateAndNeverLeavesTheBoundedSet() {
		BlockPos center = new BlockPos(20, 64, -8);
		BlockPos expected = center.offset(BlockPlacementSearch.offsets().get(17));
		assertEquals(expected, BlockPlacementSearch.find(center, expected::equals));
		assertNull(BlockPlacementSearch.find(center, ignored -> false));
	}
}
