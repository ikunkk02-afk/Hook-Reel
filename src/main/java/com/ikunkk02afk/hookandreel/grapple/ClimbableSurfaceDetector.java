package com.ikunkk02afk.hookandreel.grapple;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClimbableSurfaceDetector {
	private static final double VERTICAL_INSET = 0.05D;
	private static final double COLLISION_TOLERANCE = 0.05D;
	private static final double OVERLAP_EPSILON = 1.0E-5D;
	private static final double CHANNEL_COLLISION_EPSILON = 1.0E-5D;

	private ClimbableSurfaceDetector() {
	}

	public static Optional<SurfaceContact> findNearest(
		ServerLevel level,
		Player player,
		double maximumDistance
	) {
		double distance = Math.max(0.0D, maximumDistance);
		AABB playerBox = player.getBoundingBox();
		SurfaceContact nearest = null;
		for (Direction face : Direction.Plane.HORIZONTAL) {
			AABB probe = probeForFace(playerBox, face, distance);
			int minX = (int) Math.floor(probe.minX);
			int minY = (int) Math.floor(probe.minY);
			int minZ = (int) Math.floor(probe.minZ);
			int maxX = (int) Math.floor(probe.maxX - 1.0E-7D);
			int maxY = (int) Math.floor(probe.maxY - 1.0E-7D);
			int maxZ = (int) Math.floor(probe.maxZ - 1.0E-7D);
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						BlockPos pos = new BlockPos(x, y, z);
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
						for (AABB localBox : shape.toAabbs()) {
							AABB shapeBox = localBox.move(pos);
							SurfaceContact candidate = contact(playerBox, probe, pos, face, shapeBox, distance);
							if (candidate != null && (nearest == null || candidate.distance() < nearest.distance())) {
								nearest = candidate;
							}
						}
					}
				}
			}
		}
		return Optional.ofNullable(nearest);
	}

	public static boolean isCollisionSurfaceValid(ServerLevel level, Player player, BlockPos pos) {
		if (!level.isLoaded(pos)) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		return !state.isAir()
			&& state.getFluidState().isEmpty()
			&& !state.getCollisionShape(level, pos, CollisionContext.of(player)).isEmpty();
	}

	public static MovementChannels probeMovementChannels(
		ServerLevel level,
		Player player,
		Vec3 rightDirection,
		Vec3 forwardDirection,
		double distance
	) {
		return new MovementChannels(
			isMovementChannelClear(level, player, rightDirection.scale(-1.0D), distance),
			isMovementChannelClear(level, player, rightDirection, distance),
			isMovementChannelClear(level, player, forwardDirection, distance)
		);
	}

	public static boolean isMovementChannelClear(
		ServerLevel level,
		Player player,
		Vec3 direction,
		double distance
	) {
		Vec3 horizontalDirection = new Vec3(direction.x, 0.0D, direction.z);
		double safeDistance = Math.max(0.0D, distance);
		if (safeDistance <= 0.0D || horizontalDirection.lengthSqr() < OVERLAP_EPSILON) {
			return true;
		}

		Vec3 offset = horizontalDirection.normalize().scale(safeDistance);
		AABB sweptBox = player.getBoundingBox()
			.expandTowards(offset)
			.deflate(CHANNEL_COLLISION_EPSILON);
		int minX = (int) Math.floor(sweptBox.minX);
		int minY = (int) Math.floor(sweptBox.minY);
		int minZ = (int) Math.floor(sweptBox.minZ);
		int maxX = (int) Math.floor(sweptBox.maxX - 1.0E-7D);
		int maxY = (int) Math.floor(sweptBox.maxY - 1.0E-7D);
		int maxZ = (int) Math.floor(sweptBox.maxZ - 1.0E-7D);
		CollisionContext context = CollisionContext.of(player);
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (!level.isLoaded(pos)) {
						return false;
					}
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) {
						continue;
					}
					VoxelShape shape = state.getCollisionShape(level, pos, context);
					if (shape.isEmpty()) {
						continue;
					}
					for (AABB localBox : shape.toAabbs()) {
						if (localBox.move(pos).intersects(sweptBox)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private static AABB probeForFace(AABB box, Direction face, double distance) {
		double minY = box.minY + VERTICAL_INSET;
		double maxY = Math.max(minY + OVERLAP_EPSILON, box.maxY - VERTICAL_INSET);
		return switch (face) {
			case WEST -> new AABB(box.maxX, minY, box.minZ, box.maxX + distance, maxY, box.maxZ);
			case EAST -> new AABB(box.minX - distance, minY, box.minZ, box.minX, maxY, box.maxZ);
			case NORTH -> new AABB(box.minX, minY, box.maxZ, box.maxX, maxY, box.maxZ + distance);
			case SOUTH -> new AABB(box.minX, minY, box.minZ - distance, box.maxX, maxY, box.minZ);
			default -> throw new IllegalArgumentException("Only horizontal faces are supported");
		};
	}

	private static SurfaceContact contact(
		AABB playerBox,
		AABB probe,
		BlockPos blockPos,
		Direction face,
		AABB shapeBox,
		double maximumDistance
	) {
		if (!overlaps(probe.minY, probe.maxY, shapeBox.minY, shapeBox.maxY)) {
			return null;
		}
		double gap;
		double surfaceCoordinate;
		if (face.getAxis() == Direction.Axis.X) {
			if (!overlaps(playerBox.minZ, playerBox.maxZ, shapeBox.minZ, shapeBox.maxZ)) {
				return null;
			}
			if (face == Direction.WEST) {
				surfaceCoordinate = shapeBox.minX;
				gap = surfaceCoordinate - playerBox.maxX;
			} else {
				surfaceCoordinate = shapeBox.maxX;
				gap = playerBox.minX - surfaceCoordinate;
			}
		} else {
			if (!overlaps(playerBox.minX, playerBox.maxX, shapeBox.minX, shapeBox.maxX)) {
				return null;
			}
			if (face == Direction.NORTH) {
				surfaceCoordinate = shapeBox.minZ;
				gap = surfaceCoordinate - playerBox.maxZ;
			} else {
				surfaceCoordinate = shapeBox.maxZ;
				gap = playerBox.minZ - surfaceCoordinate;
			}
		}
		if (gap < -COLLISION_TOLERANCE || gap > maximumDistance + COLLISION_TOLERANCE) {
			return null;
		}
		return new SurfaceContact(
			blockPos.immutable(),
			face,
			Vec3.atLowerCornerOf(face.getNormal()),
			surfaceCoordinate,
			Math.max(0.0D, gap)
		);
	}

	private static boolean overlaps(double minA, double maxA, double minB, double maxB) {
		return Math.min(maxA, maxB) - Math.max(minA, minB) > OVERLAP_EPSILON;
	}

	public record SurfaceContact(
		BlockPos blockPos,
		Direction clingFace,
		Vec3 outwardNormal,
		double surfaceCoordinate,
		double distance
	) {
	}

	public record MovementChannels(boolean leftClear, boolean rightClear, boolean forwardClear) {
		public boolean requestedSideClear(float sidewaysInput) {
			if (sidewaysInput > 0.0F) {
				return rightClear;
			}
			if (sidewaysInput < 0.0F) {
				return leftClear;
			}
			return true;
		}
	}
}
