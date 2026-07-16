package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ReelTargetCalculator {
	private static final int SEARCH_RADIUS = 2;
	private static final double TANGENTIAL_INSET = 0.05D;
	private static final double COLLISION_EPSILON = 1.0E-7D;

	private ReelTargetCalculator() {
	}

	public static ReelTarget calculate(
		ServerLevel level,
		ServerPlayer player,
		BlockPos anchorBlock,
		Vec3 anchorPosition,
		Direction hitFace,
		HookReelConfig config
	) {
		Direction outward = horizontalOutwardDirection(player, anchorBlock, hitFace);
		double safeOffset = player.getBbWidth() * 0.5D + config.reelTargetSurfaceOffset;
		AABB playerBox = player.getBoundingBox();
		Vec3 playerCenter = playerBox.getCenter();
		Candidate best = null;

		for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
			for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
				for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
					BlockPos pos = anchorBlock.offset(dx, dy, dz);
					if (!level.isLoaded(pos)) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (state.isAir() || !state.getFluidState().isEmpty()) {
						continue;
					}
					VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.of(player));
					if (shape.isEmpty()) {
						continue;
					}
					BlockState aboveState = level.getBlockState(pos.above());
					if (
						!aboveState.getFluidState().isEmpty()
							|| !aboveState.getCollisionShape(level, pos.above(), CollisionContext.of(player)).isEmpty()
					) {
						continue;
					}
					double shapeTop = pos.getY() + shape.max(Direction.Axis.Y);
					for (AABB localBox : shape.toAabbs()) {
						AABB shapeBox = localBox.move(pos);
						if (Math.abs(shapeBox.maxY - shapeTop) > 1.0E-6D) {
							continue;
						}
						Vec3 target = targetOutside(shapeBox, anchorPosition, outward, safeOffset, config.reelTargetVerticalOffset);
						AABB targetBox = playerBox.move(target.subtract(playerCenter));
						if (!isBlockCollisionFree(level, player, targetBox)) {
							continue;
						}
						double score = target.distanceToSqr(anchorPosition)
							+ (pos.equals(anchorBlock) ? -0.5D : 0.0D);
						Candidate candidate = new Candidate(target, score);
						if (best == null || candidate.score() < best.score()) {
							best = candidate;
						}
					}
				}
			}
		}
		if (best != null) {
			return new ReelTarget(best.position(), outward, true);
		}

		Vec3 normal = Vec3.atLowerCornerOf(outward.getNormal());
		Vec3 fallback = anchorPosition.add(normal.scale(safeOffset)).add(0.0D, config.reelTargetVerticalOffset, 0.0D);
		for (int step = 0; step <= 10; step++) {
			Vec3 candidate = fallback.add(normal.scale(step * 0.1D));
			if (isBlockCollisionFree(level, player, playerBox.move(candidate.subtract(playerCenter)))) {
				return new ReelTarget(candidate, outward, false);
			}
		}

		VoxelShape anchorShape = level.getBlockState(anchorBlock).getCollisionShape(
			level,
			anchorBlock,
			CollisionContext.of(player)
		);
		double anchorTop = anchorShape.isEmpty()
			? anchorPosition.y
			: anchorBlock.getY() + anchorShape.max(Direction.Axis.Y);
		double halfPlayerHeight = (playerBox.maxY - playerBox.minY) * 0.5D;
		Vec3 elevatedFallback = new Vec3(
			fallback.x,
			Math.max(fallback.y, anchorTop + halfPlayerHeight + config.reelTargetVerticalOffset),
			fallback.z
		);
		for (int step = 0; step <= 12; step++) {
			Vec3 candidate = elevatedFallback.add(0.0D, step * 0.25D, 0.0D);
			if (isBlockCollisionFree(level, player, playerBox.move(candidate.subtract(playerCenter)))) {
				return new ReelTarget(candidate, outward, false);
			}
		}
		return new ReelTarget(playerCenter, outward, false);
	}

	private static boolean isBlockCollisionFree(ServerLevel level, ServerPlayer player, AABB box) {
		int minimumX = Mth.floor(box.minX + COLLISION_EPSILON);
		int minimumY = Mth.floor(box.minY + COLLISION_EPSILON);
		int minimumZ = Mth.floor(box.minZ + COLLISION_EPSILON);
		int maximumX = Mth.floor(box.maxX - COLLISION_EPSILON);
		int maximumY = Mth.floor(box.maxY - COLLISION_EPSILON);
		int maximumZ = Mth.floor(box.maxZ - COLLISION_EPSILON);
		AABB innerBox = box.deflate(COLLISION_EPSILON);
		for (BlockPos pos : BlockPos.betweenClosed(minimumX, minimumY, minimumZ, maximumX, maximumY, maximumZ)) {
			if (!level.isLoaded(pos)) {
				return false;
			}
			VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos, CollisionContext.of(player));
			for (AABB localBox : shape.toAabbs()) {
				if (innerBox.intersects(localBox.move(pos))) {
					return false;
				}
			}
		}
		return true;
	}

	private static Vec3 targetOutside(
		AABB shapeBox,
		Vec3 anchorPosition,
		Direction outward,
		double safeOffset,
		double verticalOffset
	) {
		double x;
		double z;
		if (outward.getAxis() == Direction.Axis.X) {
			x = (outward == Direction.EAST ? shapeBox.maxX : shapeBox.minX)
				+ outward.getStepX() * safeOffset;
			z = clampToInset(anchorPosition.z, shapeBox.minZ, shapeBox.maxZ);
		} else {
			x = clampToInset(anchorPosition.x, shapeBox.minX, shapeBox.maxX);
			z = (outward == Direction.SOUTH ? shapeBox.maxZ : shapeBox.minZ)
				+ outward.getStepZ() * safeOffset;
		}
		return new Vec3(x, shapeBox.maxY + verticalOffset, z);
	}

	private static double clampToInset(double value, double minimum, double maximum) {
		double insetMinimum = minimum + TANGENTIAL_INSET;
		double insetMaximum = maximum - TANGENTIAL_INSET;
		return insetMinimum <= insetMaximum
			? Math.clamp(value, insetMinimum, insetMaximum)
			: (minimum + maximum) * 0.5D;
	}

	private static Direction horizontalOutwardDirection(
		ServerPlayer player,
		BlockPos anchorBlock,
		Direction hitFace
	) {
		if (hitFace.getAxis().isHorizontal()) {
			return hitFace;
		}
		Vec3 center = Vec3.atCenterOf(anchorBlock);
		Vec3 playerCenter = player.getBoundingBox().getCenter();
		double dx = playerCenter.x - center.x;
		double dz = playerCenter.z - center.z;
		if (Math.abs(dx) >= Math.abs(dz)) {
			return dx >= 0.0D ? Direction.EAST : Direction.WEST;
		}
		return dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
	}

	private record Candidate(Vec3 position, double score) {
	}

	public record ReelTarget(Vec3 position, Direction outwardFace, boolean ledgeTarget) {
	}
}
