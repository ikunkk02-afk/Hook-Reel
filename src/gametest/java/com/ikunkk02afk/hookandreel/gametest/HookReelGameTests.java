package com.ikunkk02afk.hookandreel.gametest;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.entity.ModEntityTypes;
import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import com.ikunkk02afk.hookandreel.grapple.GrappleBlockController;
import com.ikunkk02afk.hookandreel.grapple.GrappleCooldown;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HookReelGameTests implements FabricGameTest {
	private static final AtomicBoolean DENY_BREAK_EVENT = new AtomicBoolean();
	private static final AtomicInteger CANCELED_EVENT_COUNT = new AtomicInteger();
	private static final AtomicInteger AFTER_EVENT_COUNT = new AtomicInteger();

	static {
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> !DENY_BREAK_EVENT.get());
		PlayerBlockBreakEvents.CANCELED.register((level, player, pos, state, blockEntity) -> {
			if (DENY_BREAK_EVENT.get()) {
				CANCELED_EVENT_COUNT.incrementAndGet();
			}
		});
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (DENY_BREAK_EVENT.get()) {
				AFTER_EVENT_COUNT.incrementAndGet();
			}
		});
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void extractsStoneDirtAndLogWithoutDuplicatingOwnership(GameTestHelper helper) {
		List<BlockState> states = List.of(
			Blocks.STONE.defaultBlockState(),
			Blocks.DIRT.defaultBlockState(),
			Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X)
		);
		for (int index = 0; index < states.size(); index++) {
			BlockPos relative = new BlockPos(2 + index * 2, 2, 2);
			PullRig rig = begin(helper, relative, states.get(index), GameType.SURVIVAL);
			helper.assertTrue(rig.started(), "ordinary block should start pulling");
			helper.assertTrue(rig.level().getBlockState(rig.blockPos()).isAir(), "origin must be empty after transfer");
			int matchingOwners = rig.level().getEntities(
				ModEntityTypes.PULLED_BLOCK,
				entity -> entity.getOriginPos().equals(rig.blockPos())
			).size();
			helper.assertValueEqual(matchingOwners, 1, "one entity owns the block");
			rig.hook().discard();
			helper.assertValueEqual(states.get(index), rig.level().getBlockState(rig.blockPos()), "recovery must restore the exact state");
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void preservesStairDirectionLogAxisAndWaterloggedState(GameTestHelper helper) {
		BlockState stair = Blocks.OAK_STAIRS.defaultBlockState()
			.setValue(StairBlock.FACING, Direction.EAST)
			.setValue(StairBlock.HALF, Half.TOP)
			.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
		BlockState log = Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
		BlockState waterlogged = stair.setValue(BlockStateProperties.WATERLOGGED, true);
		List<BlockState> states = List.of(stair, log, waterlogged);
		for (int index = 0; index < states.size(); index++) {
			PullRig rig = begin(helper, new BlockPos(2 + index * 2, 2, 4), states.get(index), GameType.SURVIVAL);
			helper.assertTrue(rig.started(), "stateful block should start pulling");
			helper.assertValueEqual(states.get(index), rig.pulled().getBlockState(), "entity must carry the complete block state");
			rig.hook().discard();
			helper.assertValueEqual(states.get(index), rig.level().getBlockState(rig.blockPos()), "recovered state must be unchanged");
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void pulledEntityIsSaveableNotSummonableAndRoundTripsOwnershipData(GameTestHelper helper) {
		PullRig rig = begin(helper, new BlockPos(3, 2, 3), Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup pull must start");
		helper.assertTrue(ModEntityTypes.PULLED_BLOCK.canSerialize(), "pulled blocks must be saved with chunks");
		helper.assertFalse(ModEntityTypes.PULLED_BLOCK.canSummon(), "pulled blocks must not be command-summonable");
		helper.assertTrue(Math.abs(rig.pulled().getBbWidth() - 0.98F) < 1.0E-6F, "collision box width must be 0.98 blocks");
		helper.assertTrue(Math.abs(rig.pulled().getBbHeight() - 0.98F) < 1.0E-6F, "collision box height must be 0.98 blocks");

		CompoundTag saved = rig.pulled().saveWithoutId(new CompoundTag());
		helper.assertValueEqual(saved.getString("OwnershipState"), "CARRYING", "ownership phase must be saved");
		helper.assertTrue(saved.hasUUID("Player"), "player UUID must be saved");
		helper.assertTrue(saved.hasUUID("Hook"), "hook UUID must be saved");
		helper.assertTrue(saved.contains("OriginPos"), "origin position must be saved");
		helper.assertTrue(saved.contains("OriginDimension"), "origin dimension must be saved");
		helper.assertTrue(saved.contains("SurvivalTicks"), "survival tick count must be saved");

		PulledBlockEntity loaded = new PulledBlockEntity(ModEntityTypes.PULLED_BLOCK, rig.level());
		loaded.load(saved);
		helper.assertValueEqual(loaded.getBlockState(), rig.pulled().getBlockState(), "complete block state must round-trip");
		helper.assertValueEqual(loaded.getOriginPos(), rig.blockPos(), "origin position must round-trip");
		helper.assertValueEqual(loaded.getOriginDimension(), rig.level().dimension(), "origin dimension must round-trip");
		helper.assertValueEqual(loaded.getPlayerUuid(), rig.player().getUUID(), "player UUID must round-trip");
		helper.assertValueEqual(loaded.getHookUuid(), rig.hook().getUUID(), "hook UUID must round-trip");
		helper.assertValueEqual(loaded.getOwnershipState(), PulledBlockEntity.OwnershipState.CARRYING, "phase must round-trip");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void rejectsTechnicalMultiblockAndBlockEntityTargets(GameTestHelper helper) {
		List<BlockState> rejected = List.of(
			Blocks.BEDROCK.defaultBlockState(),
			Blocks.BARRIER.defaultBlockState(),
			Blocks.COMMAND_BLOCK.defaultBlockState(),
			Blocks.NETHER_PORTAL.defaultBlockState(),
			Blocks.CHEST.defaultBlockState(),
			Blocks.FURNACE.defaultBlockState(),
			Blocks.SHULKER_BOX.defaultBlockState(),
			Blocks.OAK_DOOR.defaultBlockState(),
			Blocks.RED_BED.defaultBlockState()
		);
		for (int index = 0; index < rejected.size(); index++) {
			BlockPos relative = new BlockPos(1 + index % 6, 2 + index / 6, 1);
			PullRig rig = begin(helper, relative, rejected.get(index), GameType.CREATIVE);
			helper.assertFalse(rig.started(), "unsafe target must be rejected: " + rejected.get(index));
			helper.assertValueEqual(rejected.get(index), rig.level().getBlockState(rig.blockPos()), "rejected target must remain in place");
			rig.hook().discard();
		}
		int localPulledEntities = helper.getLevel().getEntitiesOfClass(
			PulledBlockEntity.class,
			helper.getBounds(),
			entity -> true
		).size();
		helper.assertValueEqual(localPulledEntities, 0, "no pulled entity may be created");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void rejectsProtectionEventSpectatorAndAdventureWithoutCanBreak(GameTestHelper helper) {
		DENY_BREAK_EVENT.set(true);
		CANCELED_EVENT_COUNT.set(0);
		AFTER_EVENT_COUNT.set(0);
		PullRig protectedRig;
		try {
			protectedRig = begin(helper, new BlockPos(2, 2, 2), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
		} finally {
			DENY_BREAK_EVENT.set(false);
		}
		helper.assertFalse(protectedRig.started(), "BEFORE cancellation must reject pulling");
		helper.assertValueEqual(1, CANCELED_EVENT_COUNT.get(), "CANCELED must be fired exactly once");
		helper.assertValueEqual(AFTER_EVENT_COUNT.get(), 0, "semantic AFTER event must not run for a canceled transfer");
		protectedRig.hook().discard();

		PullRig spectator = begin(helper, new BlockPos(4, 2, 2), Blocks.DIRT.defaultBlockState(), GameType.SPECTATOR);
		helper.assertFalse(spectator.started(), "spectators cannot pull blocks");
		spectator.hook().discard();

		PullRig adventure = begin(helper, new BlockPos(6, 2, 2), Blocks.OAK_LOG.defaultBlockState(), GameType.ADVENTURE);
		helper.assertFalse(adventure.started(), "adventure rods without CanBreak cannot pull blocks");
		adventure.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
	public void hookRemovalAndPlayerDeathRestoreOwnership(GameTestHelper helper) {
		PullRig removed = begin(helper, new BlockPos(2, 2, 2), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(removed.started(), "setup pull must start");
		removed.hook().discard();
		helper.assertValueEqual(Blocks.STONE.defaultBlockState(), removed.level().getBlockState(removed.blockPos()), "hook removal restores origin");

		PullRig dead = begin(helper, new BlockPos(5, 2, 2), Blocks.DIRT.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(dead.started(), "setup pull must start");
		dead.player().kill();
		helper.runAfterDelay(2, () -> {
			helper.assertValueEqual(Blocks.DIRT.defaultBlockState(), dead.level().getBlockState(dead.blockPos()), "death restores origin");
			helper.assertTrue(dead.hook().isRemoved(), "orphan hook must be removed");
			helper.succeed();
		});
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void occupiedOriginFallsBackToNearbySafePlacement(GameTestHelper helper) {
		PullRig rig = begin(helper, new BlockPos(3, 2, 3), Blocks.OAK_LOG.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup pull must start");
		rig.level().setBlock(rig.blockPos(), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
		rig.hook().discard();
		helper.assertValueEqual(Blocks.COBBLESTONE.defaultBlockState(), rig.level().getBlockState(rig.blockPos()), "occupied origin cannot be overwritten");
		helper.assertValueEqual(1, countBlock(helper, Blocks.OAK_LOG), "carried block must be placed exactly once nearby");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void blockedRecoveryDropsExactlyOneItemAndNoBlockCopy(GameTestHelper helper) {
		PullRig rig = begin(helper, new BlockPos(3, 3, 3), Blocks.DIRT.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup pull must start");
		BlockPos center = rig.pulled().blockPosition();
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					rig.level().setBlock(center.offset(x, y, z), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
		rig.hook().discard();
		List<ItemEntity> drops = rig.level().getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(center).inflate(3.0D), item -> item.getItem().is(Items.DIRT));
		helper.assertValueEqual(1, drops.size(), "fallback must create exactly one corresponding item");
		helper.assertValueEqual(1, drops.getFirst().getItem().getCount(), "fallback stack size must be one");
		helper.assertValueEqual(0, countBlock(helper, Blocks.DIRT), "fallback item and restored block must never coexist");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40, batch = "block_timeout")
	public void timeoutRestoresBlockAndAppliesFullCooldown(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		double previous = config.maxBlockPullDurationSeconds;
		config.maxBlockPullDurationSeconds = 0.5D;
		PullRig rig = begin(helper, new BlockPos(1, 2, 3), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup pull must start");
		rig.player().moveTo(helper.absoluteVec(new Vec3(7.0D, 2.0D, 3.0D)));
		helper.runAfterDelay(12, () -> {
			try {
				helper.assertValueEqual(Blocks.STONE.defaultBlockState(), rig.level().getBlockState(rig.blockPos()), "timeout restores origin");
				long remaining = GrappleCooldown.remainingTicks(rig.rod(), rig.level(), 200);
				helper.assertTrue(remaining > 0L, "timeout applies the full grapple cooldown");
				helper.succeed();
			} finally {
				config.maxBlockPullDurationSeconds = previous;
			}
		});
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 30)
	public void movementIsSpeedCappedAndCannotPassThroughWall(GameTestHelper helper) {
		PullRig rig = begin(helper, new BlockPos(1, 2, 3), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup pull must start");
		rig.player().moveTo(helper.absoluteVec(new Vec3(7.0D, 2.0D, 3.0D)));
		for (int y = 1; y <= 4; y++) {
			helper.setBlock(new BlockPos(4, y, 3), Blocks.BEDROCK);
		}
		double wallMinX = helper.absolutePos(new BlockPos(4, 2, 3)).getX();
		helper.onEachTick(() -> helper.assertTrue(
			rig.pulled().getDeltaMovement().length() <= 1.2D + 1.0E-7D,
			"block velocity must respect the configured multiplied cap"
		));
		helper.runAfterDelay(12, () -> {
			helper.assertTrue(rig.pulled().getBoundingBox().maxX <= wallMinX + 1.0E-6D, "normal collision movement must stop at the wall");
			rig.hook().discard();
			helper.succeed();
		});
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
	public void arrivalPlacesExactlyOneBlockAndChargesConfiguredSuccessCost(GameTestHelper helper) {
		PullRig rig = begin(helper, new BlockPos(1, 2, 3), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup pull must start");
		rig.player().moveTo(helper.absoluteVec(new Vec3(7.0D, 2.0D, 3.0D)));
		double startingX = rig.pulled().getX();
		helper.runAfterDelay(5, () -> {
			helper.assertTrue(rig.pulled().getX() > startingX, "pulled block must move before arrival");
			rig.player().moveTo(rig.pulled().getX() + 2.0D, rig.pulled().getY(), rig.pulled().getZ());
		});
		helper.runAfterDelay(12, () -> {
			helper.assertTrue(rig.pulled().isRemoved(), "successful placement resolves the carrying entity");
			helper.assertValueEqual(countBlock(helper, Blocks.STONE), 1, "successful placement keeps exactly one block owner");
			helper.assertValueEqual(rig.rod().getDamageValue(), 3, "successful block pull uses configured durability cost");
			helper.assertTrue(GrappleCooldown.remainingTicks(rig.rod(), rig.level(), 200) > 0L, "success applies the full cooldown");
			helper.succeed();
		});
	}

	private static PullRig begin(GameTestHelper helper, BlockPos relativePos, BlockState state, GameType gameType) {
		ServerLevel level = helper.getLevel();
		BlockPos absolutePos = helper.absolutePos(relativePos);
		level.setBlock(absolutePos, state, Block.UPDATE_ALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(gameType);
		player.moveTo(helper.absoluteVec(new Vec3(7.0D, 2.0D, 7.0D)));
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		FishingHook hook = new FishingHook(player, level, 0, 0);
		hook.setPos(absolutePos.getX() + 0.5D, absolutePos.getY() + 0.5D, absolutePos.getZ() + 0.5D);
		((GrapplingBobberAccess) hook).hookAndReel$initializeGrapple(rod, InteractionHand.MAIN_HAND, 64.0D);
		level.addFreshEntity(hook);
		boolean started = GrappleBlockController.tryBegin(
			hook,
			new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false)
		);
		PulledBlockEntity pulled = started
			? level.getEntities(ModEntityTypes.PULLED_BLOCK, entity -> entity.getOriginPos().equals(absolutePos)).getFirst()
			: null;
		return new PullRig(level, player, rod, hook, pulled, absolutePos, started);
	}

	private static int countBlock(GameTestHelper helper, Block block) {
		int count = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				for (int z = 0; z < 8; z++) {
					if (helper.getBlockState(new BlockPos(x, y, z)).is(block)) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private record PullRig(
		ServerLevel level,
		ServerPlayer player,
		ItemStack rod,
		FishingHook hook,
		PulledBlockEntity pulled,
		BlockPos blockPos,
		boolean started
	) {
	}
}
