package com.ikunkk02afk.hookandreel.gametest;

import com.ikunkk02afk.hookandreel.component.GrappleMode;
import com.ikunkk02afk.hookandreel.component.GrappleModeComponent;
import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.fishing.FishableEntitySelector;
import com.ikunkk02afk.hookandreel.fishing.FishingEntityCategory;
import com.ikunkk02afk.hookandreel.fishing.FishingEntityPullController;
import com.ikunkk02afk.hookandreel.fishing.FishingEntitySpawner;
import com.ikunkk02afk.hookandreel.fishing.LuckyInstantCatchController;
import com.ikunkk02afk.hookandreel.entity.ModEntityTypes;
import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import com.ikunkk02afk.hookandreel.grapple.GrappleBlockController;
import com.ikunkk02afk.hookandreel.grapple.ClimbableSurfaceDetector;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookAbilityCooldown;
import com.ikunkk02afk.hookandreel.grapple.HookAbilityCooldownManager;
import com.ikunkk02afk.hookandreel.grapple.HookState;
import com.ikunkk02afk.hookandreel.grapple.HookModeController;
import com.ikunkk02afk.hookandreel.grapple.ReelMovementMath;
import com.ikunkk02afk.hookandreel.grapple.SwingController;
import com.ikunkk02afk.hookandreel.grapple.SwingDetachReason;
import com.ikunkk02afk.hookandreel.grapple.WallClingController;
import com.ikunkk02afk.hookandreel.enchantment.ModEnchantments;
import com.ikunkk02afk.hookandreel.tag.ModEntityTypeTags;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.AABB;
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
				long remaining = HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.PULL, 200);
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
			helper.assertTrue(HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.PULL, 200) > 0L, "success applies the full cooldown");
			helper.succeed();
		});
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void swingAnchorsStoneAtTheHitSurfaceWithoutExtractingIt(GameTestHelper helper) {
		SwingRig rig = beginSwing(helper, new BlockPos(3, 3, 3), Blocks.STONE.defaultBlockState());
		GrapplingBobberAccess access = (GrapplingBobberAccess) rig.hook();

		helper.assertFalse(
			GrappleBlockController.tryBegin(rig.hook(), rig.hitResult()),
			"SWING must never enter the PULL block controller"
		);
		helper.assertTrue(SwingController.tryAnchor(rig.hook(), rig.hitResult()), "stone should accept a swing anchor");
		helper.assertValueEqual(Blocks.STONE.defaultBlockState(), rig.level().getBlockState(rig.blockPos()), "anchor must not remove the block");
		helper.assertValueEqual(HookState.ANCHORED_IDLE, access.hookAndReel$getHookState(), "hook hit must only establish an idle anchor");
		helper.assertTrue(
			access.hookAndReel$getAnchorPosition().distanceTo(rig.hitResult().getLocation().add(0.0D, 0.01D, 0.0D)) < 1.0E-6D,
			"anchor must preserve the real surface hit location"
		);
		helper.assertValueEqual(1, rig.rod().getDamageValue(), "a successful anchor charges durability once");
		helper.assertValueEqual(0, rig.level().getEntitiesOfClass(PulledBlockEntity.class, helper.getBounds(), entity -> true).size(), "swing must not create a pulled block");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void highAnchorWaitsForSpaceAndOneRequestKeepsReeling(GameTestHelper helper) {
		SwingRig rig = beginSwing(helper, new BlockPos(3, 9, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(rig.hook(), rig.hitResult()), "high anchor setup must succeed");
		GrapplingBobberAccess access = (GrapplingBobberAccess) rig.hook();
		rig.player().setDeltaMovement(Vec3.ZERO);
		double ropeBefore = access.hookAndReel$getRopeLength();

		SwingController.tickAnchored(rig.hook());
		helper.assertValueEqual(Vec3.ZERO, rig.player().getDeltaMovement(), "idle anchor must not auto-reel the player");
		helper.assertValueEqual(HookState.ANCHORED_IDLE, access.hookAndReel$getHookState(), "idle anchor must wait for Space");

		SwingController.handleStartReelRequest(rig.player());
		helper.assertValueEqual(HookState.REELING_UP, access.hookAndReel$getHookState(), "one validated Space request starts reel-up");
		Vec3 target = access.hookAndReel$getReelTargetPosition();
		helper.assertTrue(target != null, "server must calculate a dedicated reel target");
		AABB targetBox = rig.player().getBoundingBox().move(target.subtract(rig.player().getBoundingBox().getCenter()));
		var targetCollisions = rig.level().getBlockCollisions(rig.player(), targetBox).iterator();
		Object firstTargetCollision = targetCollisions.hasNext() ? targetCollisions.next() : null;
		helper.assertTrue(
			firstTargetCollision == null,
			"reel target must keep the player collision box outside solid blocks: target="
				+ target + ", box=" + targetBox + ", collision=" + firstTargetCollision
		);
		SwingController.tickAnchored(rig.hook());

		helper.assertTrue(rig.player().getDeltaMovement().y > 0.0D, "server reel-up must add upward velocity without holding Space");
		helper.assertValueEqual(ropeBefore, access.hookAndReel$getRopeLength(), "ordinary rope constraint must be bypassed during reel-up");
		helper.assertValueEqual(HookState.REELING_UP, access.hookAndReel$getHookState(), "reel-up continues after the request packet");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void reelingUsesServerSideViewRelativeLateralIntent(GameTestHelper helper) {
		SwingRig rig = beginSwing(helper, new BlockPos(3, 9, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(rig.hook(), rig.hitResult()), "high anchor setup must succeed");
		GrapplingBobberAccess access = (GrapplingBobberAccess) rig.hook();
		SwingController.handleStartReelRequest(rig.player());
		Vec3 attachment = rig.player().getBoundingBox().getCenter();
		Vec3 target = access.hookAndReel$getReelTargetPosition();
		helper.assertTrue(target != null, "reel-up must have an authoritative server target");
		rig.player().setYRot(0.0F);
		Vec3 screenRightTangent = ReelMovementMath.strafeDirection(attachment, target, rig.player().getYRot());

		access.hookAndReel$updateSwingInput(-1.0F, 0.0F, rig.level().getGameTime());
		SwingController.tickAnchored(rig.hook());
		helper.assertTrue(
			rig.player().getDeltaMovement().dot(screenRightTangent) > 0.0D,
			"the server must convert the existing client D impulse into screen-right reel tangent motion"
		);

		rig.player().setDeltaMovement(Vec3.ZERO);
		access.hookAndReel$updateSwingInput(1.0F, 0.0F, rig.level().getGameTime());
		SwingController.tickAnchored(rig.hook());
		helper.assertTrue(
			rig.player().getDeltaMovement().dot(screenRightTangent) < 0.0D,
			"the server must convert the existing client A impulse into screen-left reel tangent motion"
		);
		rig.player().setShiftKeyDown(true);
		SwingController.tickAnchored(rig.hook());
		helper.assertValueEqual(HookState.ANCHORED_IDLE, access.hookAndReel$getHookState(), "Shift must exit reel-up before another controller runs");
		helper.assertValueEqual(0.0F, access.hookAndReel$getSwingLeftImpulse(), "leaving reel-up must clear stale A/D intent");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void reelingLateralChannelProbeRejectsBlockedSideOnly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 position = helper.absoluteVec(new Vec3(4.5D, 3.0D, 4.5D));
		player.moveTo(position);
		BlockPos rightBlock = BlockPos.containing(position.add(-1.0D, 0.5D, 0.0D));
		level.setBlock(rightBlock, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

		ClimbableSurfaceDetector.MovementChannels channels = ClimbableSurfaceDetector.probeMovementChannels(
			level,
			player,
			new Vec3(-1.0D, 0.0D, 0.0D),
			new Vec3(0.0D, 0.0D, 1.0D),
			0.60D
		);

		helper.assertTrue(channels.leftClear(), "the open screen-left channel must remain available");
		helper.assertFalse(channels.rightClear(), "the solid screen-right channel must be rejected");
		helper.assertTrue(channels.forwardClear(), "an unrelated open forward channel must remain available");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void bedrockCanAnchorButIndependentSwingBlacklistCannot(GameTestHelper helper) {
		SwingRig bedrock = beginSwing(helper, new BlockPos(2, 3, 2), Blocks.BEDROCK.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(bedrock.hook(), bedrock.hitResult()), "bedrock is immovable but should remain anchorable");
		bedrock.hook().discard();

		SwingRig barrier = beginSwing(helper, new BlockPos(5, 3, 2), Blocks.BARRIER.defaultBlockState());
		helper.assertFalse(SwingController.tryAnchor(barrier.hook(), barrier.hitResult()), "swing_unhookable must reject barrier");
		helper.assertValueEqual(HookState.HOOK_FLYING, ((GrapplingBobberAccess) barrier.hook()).hookAndReel$getHookState(), "rejected hit remains safely retractable");
		barrier.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void rappelIncreasesRopeLengthAndConstraintPreservesTangentialMotion(GameTestHelper helper) {
		SwingRig rig = beginSwing(helper, new BlockPos(3, 4, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(rig.hook(), rig.hitResult()), "setup anchor must succeed");
		GrapplingBobberAccess access = (GrapplingBobberAccess) rig.hook();
		double attachmentDistance = rig.player().getBoundingBox().getCenter().distanceTo(access.hookAndReel$getAnchorPosition());
		access.hookAndReel$setRopeLength(Math.max(0.5D, attachmentDistance - 1.0D));
		rig.player().setShiftKeyDown(true);
		double beforeRappel = access.hookAndReel$getRopeLength();
		rig.player().fallDistance = 20.0F;
		Vec3 radial = rig.player().getBoundingBox().getCenter().subtract(access.hookAndReel$getAnchorPosition()).normalize();
		Vec3 tangent = new Vec3(-radial.z, 0.0D, radial.x).normalize().scale(0.35D);
		rig.player().setDeltaMovement(radial.scale(0.5D).add(tangent));

		SwingController.tickAnchored(rig.hook());

		helper.assertTrue(access.hookAndReel$getRopeLength() > beforeRappel, "Shift must pay out rope instead of forcing downward velocity");
		helper.assertValueEqual(HookState.RAPPELLING, access.hookAndReel$getHookState(), "Shift must have priority over active ascent");
		helper.assertTrue(access.hookAndReel$getRopeLength() <= access.hookAndReel$getMaximumRopeLength(), "rappel must respect the level maximum");
		helper.assertTrue(rig.player().getDeltaMovement().dot(radial) <= 1.0E-6D, "outward radial velocity must be removed");
		helper.assertTrue(Math.abs(rig.player().getDeltaMovement().dot(tangent.normalize())) > 0.1D, "tangential swing velocity must survive");
		helper.assertValueEqual(0.0F, rig.player().fallDistance, "valid anchoring must clear stale fall distance");
		rig.player().setShiftKeyDown(false);
		SwingController.tickAnchored(rig.hook());
		helper.assertValueEqual(HookState.ANCHORED_IDLE, access.hookAndReel$getHookState(), "releasing Shift must return to idle anchor state");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void activeRetrieveUsesDetectionOnlyWindowAndModeSwitchDoesNot(GameTestHelper helper) {
		SwingRig retrieveRig = beginSwing(helper, new BlockPos(3, 5, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(retrieveRig.hook(), retrieveRig.hitResult()), "retrieve setup anchor must succeed");
		Vec3 inertia = new Vec3(0.6D, 0.35D, -0.2D);
		retrieveRig.player().setDeltaMovement(inertia);
		SwingController.detach(retrieveRig.hook(), SwingDetachReason.PLAYER_RETRIEVE);
		helper.assertValueEqual(inertia, retrieveRig.player().getDeltaMovement(), "active retrieve must preserve current velocity");
		helper.assertFalse(WallClingController.isActive(retrieveRig.player()), "airborne retrieve must not grant wall cling");
		BlockPos wall = helper.absolutePos(new BlockPos(6, 3, 6));
		retrieveRig.level().setBlock(wall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		retrieveRig.player().moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);
		WallClingController.tickPlayer(retrieveRig.player());
		helper.assertTrue(WallClingController.isActive(retrieveRig.player()), "release window starts cling only after a real wall is detected");
		helper.assertTrue(WallClingController.remainingTicks(retrieveRig.player()) > 0L, "wall detection starts the server-owned ten-second deadline");
		WallClingController.clear(retrieveRig.player());

		SwingRig switchRig = beginSwing(helper, new BlockPos(6, 5, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(switchRig.hook(), switchRig.hitResult()), "mode switch setup anchor must succeed");
		SwingController.detach(switchRig.hook(), SwingDetachReason.MODE_SWITCH);
		switchRig.player().moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);
		WallClingController.tickPlayer(switchRig.player());
		helper.assertFalse(WallClingController.isActive(switchRig.player()), "mode switching must not arm wall capture");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void wallClingUsesShapeContactForMovementAndWallJump(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
		level.setBlock(wall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);
		player.setOnGround(false);
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, ModEnchantments.ANCHOR_HOOK, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		ClimbableSurfaceDetector.SurfaceContact contact = ClimbableSurfaceDetector.findNearest(level, player, 0.4D).orElseThrow();
		helper.assertTrue(WallClingController.start(player, rod, InteractionHand.MAIN_HAND, contact), "solid collision surface must start wall cling");
		HookReelConfig config = HookReelConfigManager.get();
		player.setDeltaMovement(new Vec3(0.0D, -0.08D, 0.0D));
		player.fallDistance = 12.0F;

		WallClingController.tickPlayer(player);

		helper.assertValueEqual(0.0D, player.getDeltaMovement().y, "idle cling must hold Y at zero after vanilla gravity");
		helper.assertValueEqual(0.0F, player.fallDistance, "valid wall cling clears accumulated fall distance");

		WallClingController.updateInput(player, 1.0F, 0.0F);
		WallClingController.tickPlayer(player);
		Vec3 tangent = new Vec3(-contact.outwardNormal().z, 0.0D, contact.outwardNormal().x).normalize();
		helper.assertTrue(Math.abs(player.getDeltaMovement().dot(tangent)) > 0.0D, "A/D input must retain horizontal movement along the wall");
		helper.assertValueEqual(0.0D, player.getDeltaMovement().y, "horizontal movement must not introduce vertical drift");

		WallClingController.updateInput(player, 0.0F, 1.0F);
		WallClingController.tickPlayer(player);
		helper.assertValueEqual(config.wallClimbSpeed, player.getDeltaMovement().y, "fresh W input must use the configured upward speed");
		WallClingController.updateInput(player, 0.0F, 0.0F);
		WallClingController.tickPlayer(player);
		helper.assertValueEqual(0.0D, player.getDeltaMovement().y, "releasing W must immediately hold the new height");

		WallClingController.updateInput(player, 0.0F, -1.0F);
		WallClingController.tickPlayer(player);
		helper.assertValueEqual(-config.wallClimbDownSpeed, player.getDeltaMovement().y, "fresh S input must use the configured downward speed");
		WallClingController.updateInput(player, 0.0F, 0.0F);
		WallClingController.tickPlayer(player);
		helper.assertValueEqual(0.0D, player.getDeltaMovement().y, "releasing S must immediately stop descending");

		player.setShiftKeyDown(true);
		WallClingController.updateInput(player, 0.0F, 1.0F);
		WallClingController.tickPlayer(player);
		helper.assertValueEqual(0.0D, player.getDeltaMovement().y, "Shift must hold height instead of climbing or sliding");
		player.setShiftKeyDown(false);
		helper.assertTrue(WallClingController.wallJump(player), "Space action must perform a wall jump");
		helper.assertFalse(WallClingController.isActive(player), "wall jump must end cling before applying velocity");
		helper.assertTrue(player.getDeltaMovement().dot(contact.outwardNormal()) > 0.0D, "wall jump must move away from the saved surface");
		helper.assertTrue(player.getDeltaMovement().y > 0.0D, "wall jump must also move upward");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void destroyedWallEndsClingWithoutOverwritingFallingVelocity(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
		level.setBlock(wall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);
		player.setOnGround(false);
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, ModEnchantments.ANCHOR_HOOK, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		ClimbableSurfaceDetector.SurfaceContact contact = ClimbableSurfaceDetector.findNearest(level, player, 0.4D).orElseThrow();
		helper.assertTrue(WallClingController.start(player, rod, InteractionHand.MAIN_HAND, contact), "setup wall must start wall cling");
		Vec3 falling = new Vec3(0.0D, -0.20D, 0.0D);
		player.setDeltaMovement(falling);
		level.setBlock(wall, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

		WallClingController.tickPlayer(player);

		helper.assertFalse(WallClingController.isActive(player), "destroying the saved collision surface must end wall cling");
		helper.assertValueEqual(falling, player.getDeltaMovement(), "invalid wall cling must not overwrite normal falling velocity");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 230)
	public void wallClingHoldsHeightForTenSecondsThenRestoresGravity(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
		level.setBlock(wall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);
		player.setOnGround(false);
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, ModEnchantments.ANCHOR_HOOK, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		ClimbableSurfaceDetector.SurfaceContact contact = ClimbableSurfaceDetector.findNearest(level, player, 0.4D).orElseThrow();
		helper.assertTrue(WallClingController.start(player, rod, InteractionHand.MAIN_HAND, contact), "setup wall must start wall cling");
		double heldY = player.getY();
		player.setDeltaMovement(new Vec3(0.0D, -0.08D, 0.0D));
		player.fallDistance = 8.0F;

		helper.runAfterDelay(190, () -> {
			helper.assertTrue(WallClingController.isActive(player), "wall cling must remain active before the ten-second deadline");
			helper.assertTrue(Math.abs(player.getY() - heldY) < 1.0E-6D, "idle wall cling must hold the same height for its full valid duration");
			helper.assertValueEqual(0.0D, player.getDeltaMovement().y, "gravity must not leave a negative final Y while wall cling is valid");
			helper.assertValueEqual(0.0F, player.fallDistance, "wall cling must keep fall distance cleared");
		});
		helper.runAfterDelay(205, () -> {
			helper.assertFalse(WallClingController.isActive(player), "wall cling must end after the ten-second deadline");
			Vec3 falling = new Vec3(0.0D, -0.08D, 0.0D);
			player.setDeltaMovement(falling);
			WallClingController.tickPlayer(player);
			helper.assertValueEqual(falling, player.getDeltaMovement(), "expired wall cling must stop overriding vanilla gravity velocity");
			helper.succeed();
		});
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void wallDetectorSupportsSlabsAndStairsButRejectsAirFluidAndPlants(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
		player.moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);

		level.setBlock(wall, Blocks.STONE_SLAB.defaultBlockState(), Block.UPDATE_ALL);
		helper.assertTrue(ClimbableSurfaceDetector.findNearest(level, player, 0.4D).isPresent(), "slab collision shape must be detectable");
		level.setBlock(wall, Blocks.OAK_STAIRS.defaultBlockState(), Block.UPDATE_ALL);
		helper.assertTrue(ClimbableSurfaceDetector.findNearest(level, player, 0.4D).isPresent(), "stair collision shape must be detectable");
		level.setBlock(wall, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		helper.assertTrue(ClimbableSurfaceDetector.findNearest(level, player, 0.4D).isEmpty(), "air must not be clingable");
		level.setBlock(wall, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		helper.assertTrue(ClimbableSurfaceDetector.findNearest(level, player, 0.4D).isEmpty(), "fluid must not be clingable");
		level.setBlock(wall, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
		helper.assertTrue(ClimbableSurfaceDetector.findNearest(level, player, 0.4D).isEmpty(), "non-colliding fire must not be clingable");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void destroyedWallEndsClingAndMultiplayerSessionsRemainIndependent(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos firstWall = helper.absolutePos(new BlockPos(2, 2, 2));
		BlockPos secondWall = helper.absolutePos(new BlockPos(6, 2, 2));
		level.setBlock(firstWall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(secondWall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer first = wallPlayer(helper, firstWall);
		ServerPlayer second = wallPlayer(helper, secondWall);
		ItemStack firstRod = anchorRod(level);
		ItemStack secondRod = anchorRod(level);
		first.setItemInHand(InteractionHand.MAIN_HAND, firstRod);
		second.setItemInHand(InteractionHand.MAIN_HAND, secondRod);
		ClimbableSurfaceDetector.SurfaceContact firstContact = ClimbableSurfaceDetector.findNearest(level, first, 0.4D).orElseThrow();
		ClimbableSurfaceDetector.SurfaceContact secondContact = ClimbableSurfaceDetector.findNearest(level, second, 0.4D).orElseThrow();
		helper.assertTrue(WallClingController.start(first, firstRod, InteractionHand.MAIN_HAND, firstContact), "first player starts an independent session");
		helper.assertTrue(WallClingController.start(second, secondRod, InteractionHand.MAIN_HAND, secondContact), "second player starts an independent session");

		level.setBlock(firstWall, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		WallClingController.tickPlayer(first);
		helper.assertFalse(WallClingController.isActive(first), "destroying the saved wall must immediately end that cling");
		helper.assertTrue(WallClingController.isActive(second), "another player's wall session must remain active");
		WallClingController.clear(second);
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void destroyedAnchorDetachesWithoutRemovingPlayerInertia(GameTestHelper helper) {
		SwingRig rig = beginSwing(helper, new BlockPos(3, 3, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(rig.hook(), rig.hitResult()), "setup anchor must succeed");
		Vec3 velocity = new Vec3(0.45D, 0.2D, -0.3D);
		rig.player().setDeltaMovement(velocity);
		rig.level().setBlock(rig.blockPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

		SwingController.tickAnchored(rig.hook());

		helper.assertTrue(rig.hook().isRemoved(), "destroying the supporting block must break the rope");
		helper.assertValueEqual(velocity, rig.player().getDeltaMovement(), "rope break must preserve inertia");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void eachDualEnchantedRodKeepsAndSwitchesItsOwnMode(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack first = dualEnchantedRod(level);
		ItemStack second = dualEnchantedRod(level);
		GrappleModeComponent.set(first, GrappleMode.PULL);
		GrappleModeComponent.set(second, GrappleMode.SWING);

		helper.assertValueEqual(GrappleMode.PULL, GrappleModeComponent.get(first.copy()), "copied first rod must retain PULL");
		helper.assertValueEqual(GrappleMode.SWING, GrappleModeComponent.get(second.copy()), "copied second rod must retain SWING");
		player.setItemInHand(InteractionHand.MAIN_HAND, first);
		HookModeController.handleSwitchRequest(player);
		helper.assertValueEqual(GrappleMode.SWING, GrappleModeComponent.get(first), "server request toggles the selected rod");
		helper.assertValueEqual(GrappleMode.SWING, GrappleModeComponent.get(second), "unselected rod remains independent");
		player.setItemInHand(InteractionHand.MAIN_HAND, second);
		HookModeController.handleSwitchRequest(player);
		helper.assertValueEqual(GrappleMode.PULL, GrappleModeComponent.get(second), "second rod toggles independently");
		helper.assertValueEqual(GrappleMode.SWING, GrappleModeComponent.get(first), "first rod retains its saved mode");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void modeSwitchIsRejectedDuringBlockPullAndSwingDelayIsSeparate(GameTestHelper helper) {
		PullRig rig = begin(helper, new BlockPos(3, 3, 3), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
		helper.assertTrue(rig.started(), "setup block pull must start");
		enchant(rig.level(), rig.rod(), ModEnchantments.GRAPPLING_HOOK, 3);
		enchant(rig.level(), rig.rod(), ModEnchantments.ANCHOR_HOOK, 3);
		GrappleModeComponent.set(rig.rod(), GrappleMode.PULL);
		HookModeController.handleSwitchRequest(rig.player());
		helper.assertValueEqual(GrappleMode.PULL, GrappleModeComponent.get(rig.rod()), "active block pull must reject V switching");
		helper.assertFalse(rig.pulled().isRemoved(), "rejected switch must leave pulled-block ownership intact");

		HookAbilityCooldownManager.set(rig.rod(), rig.level(), HookAbilityCooldown.ANCHOR, 7);
		helper.assertValueEqual(7L, HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.ANCHOR, 7), "anchor deadline is stored independently");
		helper.assertValueEqual(0L, HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.PULL, 200), "anchor cooldown must not start the ten-second PULL cooldown");
		HookAbilityCooldownManager.set(rig.rod(), rig.level(), HookAbilityCooldown.PULL, 200);
		helper.assertValueEqual(200L, HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.PULL, 200), "PULL deadline remains independently stored");
		helper.assertValueEqual(7L, HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.ANCHOR, 30), "PULL cooldown must not overwrite the anchor deadline");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void anchorDetachUsesFullCooldownWhileFailedCastUsesShortDelay(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		int expectedFull = HookAbilityCooldownManager.secondsToTicks(config.anchorHookCooldownSeconds);
		int expectedFailure = HookAbilityCooldownManager.secondsToTicks(config.anchorHookFailedCastDelaySeconds);
		int expectedMaximum = Math.max(expectedFull, expectedFailure);
		SwingRig anchored = beginSwing(helper, new BlockPos(3, 3, 3), Blocks.STONE.defaultBlockState());
		helper.assertTrue(SwingController.tryAnchor(anchored.hook(), anchored.hitResult()), "setup anchor must succeed");
		SwingController.detach(anchored.hook(), SwingDetachReason.PLAYER_RETRIEVE);
		helper.assertValueEqual(
			HookAbilityCooldownManager.remainingTicks(anchored.rod(), anchored.level(), HookAbilityCooldown.ANCHOR, expectedMaximum),
			(long) expectedFull,
			"an established anchor uses the configured full anchor cooldown"
		);
		helper.assertValueEqual(
			0L,
			HookAbilityCooldownManager.remainingTicks(anchored.rod(), anchored.level(), HookAbilityCooldown.PULL, 200),
			"anchor retrieval never starts PULL cooldown"
		);

		FlightRig failed = flyingHook(helper, GrappleMode.SWING, new Vec3(1.5D, 8.0D, 1.5D), Vec3.ZERO, 64.0D);
		SwingController.detach(failed.hook(), SwingDetachReason.PLAYER_RETRIEVE);
		helper.assertValueEqual(
			HookAbilityCooldownManager.remainingTicks(failed.rod(), failed.level(), HookAbilityCooldown.ANCHOR, expectedMaximum),
			(long) expectedFailure,
			"an unanchored cast uses only the configured failure delay"
		);
		helper.assertValueEqual(
			0L,
			HookAbilityCooldownManager.remainingTicks(failed.rod(), failed.level(), HookAbilityCooldown.PULL, 200),
			"failed SWING never starts PULL cooldown"
		);
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void configuredAbilityCooldownsUseCurrentValuesRemainIndependentAndAllowZero(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		double previousPullCooldown = config.grapplingHookCooldownSeconds;
		double previousAnchorCooldown = config.anchorHookCooldownSeconds;
		try {
			config.grapplingHookCooldownSeconds = 2.0D;
			PullRig twoSecondPull = begin(helper, new BlockPos(2, 3, 2), Blocks.STONE.defaultBlockState(), GameType.SURVIVAL);
			helper.assertTrue(twoSecondPull.started(), "two-second PULL setup starts normally");
			twoSecondPull.player().moveTo(
				twoSecondPull.pulled().getX() + 2.0D,
				twoSecondPull.pulled().getY(),
				twoSecondPull.pulled().getZ()
			);
			GrappleBlockController.tick(twoSecondPull.pulled());
			helper.assertTrue(twoSecondPull.pulled().isRemoved(), "the configured PULL completes successfully");
			helper.assertValueEqual(
				40L,
				HookAbilityCooldownManager.remainingTicks(
					twoSecondPull.rod(),
					twoSecondPull.level(),
					HookAbilityCooldown.PULL,
					40
				),
				"2.0 seconds starts exactly 40 ticks of PULL cooldown"
			);
			helper.assertValueEqual(
				0L,
				HookAbilityCooldownManager.remainingTicks(
					twoSecondPull.rod(),
					twoSecondPull.level(),
					HookAbilityCooldown.ANCHOR,
					30
				),
				"PULL completion never starts the Anchor cooldown"
			);

			config.grapplingHookCooldownSeconds = 0.0D;
			PullRig zeroPull = begin(helper, new BlockPos(5, 3, 2), Blocks.DIRT.defaultBlockState(), GameType.SURVIVAL);
			helper.assertTrue(zeroPull.started(), "zero-cooldown PULL setup starts normally");
			zeroPull.player().moveTo(
				zeroPull.pulled().getX() + 2.0D,
				zeroPull.pulled().getY(),
				zeroPull.pulled().getZ()
			);
			GrappleBlockController.tick(zeroPull.pulled());
			helper.assertTrue(zeroPull.pulled().isRemoved(), "zero-cooldown PULL still completes normally");
			helper.assertValueEqual(
				0L,
				HookAbilityCooldownManager.remainingTicks(
					zeroPull.rod(),
					zeroPull.level(),
					HookAbilityCooldown.PULL,
					HookAbilityCooldownManager.PULL_CANCEL_COOLDOWN_TICKS
				),
				"0.0 seconds stores no PULL cooldown"
			);
			PullRig cancelledZeroPull = begin(helper, new BlockPos(7, 3, 2), Blocks.OAK_LOG.defaultBlockState(), GameType.SURVIVAL);
			helper.assertTrue(cancelledZeroPull.started(), "zero-cooldown cancellation setup starts normally");
			GrappleBlockController.manualCancel(cancelledZeroPull.hook(), cancelledZeroPull.pulled());
			helper.assertValueEqual(
				0L,
				HookAbilityCooldownManager.remainingTicks(
					cancelledZeroPull.rod(),
					cancelledZeroPull.level(),
					HookAbilityCooldown.PULL,
					HookAbilityCooldownManager.PULL_CANCEL_COOLDOWN_TICKS
				),
				"0.0 seconds also suppresses the existing short PULL cancellation protection"
			);

			config.anchorHookCooldownSeconds = 0.5D;
			SwingRig halfSecondAnchor = beginSwing(helper, new BlockPos(2, 4, 5), Blocks.STONE.defaultBlockState());
			helper.assertTrue(SwingController.tryAnchor(halfSecondAnchor.hook(), halfSecondAnchor.hitResult()), "half-second Anchor setup succeeds");
			SwingController.detach(halfSecondAnchor.hook(), SwingDetachReason.PLAYER_RETRIEVE);
			helper.assertValueEqual(
				10L,
				HookAbilityCooldownManager.remainingTicks(
					halfSecondAnchor.rod(),
					halfSecondAnchor.level(),
					HookAbilityCooldown.ANCHOR,
					10
				),
				"0.5 seconds starts exactly 10 ticks of Anchor cooldown"
			);
			helper.assertValueEqual(
				0L,
				HookAbilityCooldownManager.remainingTicks(
					halfSecondAnchor.rod(),
					halfSecondAnchor.level(),
					HookAbilityCooldown.PULL,
					40
				),
				"Anchor detachment never starts the PULL cooldown"
			);

			config.anchorHookCooldownSeconds = 0.0D;
			SwingRig zeroAnchor = beginSwing(helper, new BlockPos(5, 4, 5), Blocks.STONE.defaultBlockState());
			helper.assertTrue(SwingController.tryAnchor(zeroAnchor.hook(), zeroAnchor.hitResult()), "zero-cooldown Anchor setup succeeds");
			SwingController.detach(zeroAnchor.hook(), SwingDetachReason.PLAYER_RETRIEVE);
			helper.assertValueEqual(
				0L,
				HookAbilityCooldownManager.remainingTicks(
					zeroAnchor.rod(),
					zeroAnchor.level(),
					HookAbilityCooldown.ANCHOR,
					HookAbilityCooldownManager.secondsToTicks(config.anchorHookFailedCastDelaySeconds)
				),
				"0.0 seconds stores no full Anchor cooldown"
			);
			FlightRig failedZeroAnchor = flyingHook(
				helper,
				GrappleMode.SWING,
				new Vec3(7.0D, 8.0D, 7.0D),
				Vec3.ZERO,
				64.0D
			);
			SwingController.detach(failedZeroAnchor.hook(), SwingDetachReason.PLAYER_RETRIEVE);
			helper.assertValueEqual(
				0L,
				HookAbilityCooldownManager.remainingTicks(
					failedZeroAnchor.rod(),
					failedZeroAnchor.level(),
					HookAbilityCooldown.ANCHOR,
					HookAbilityCooldownManager.secondsToTicks(config.anchorHookFailedCastDelaySeconds)
				),
				"0.0 seconds also suppresses the existing short failed-Anchor delay"
			);
		} finally {
			config.grapplingHookCooldownSeconds = previousPullCooldown;
			config.anchorHookCooldownSeconds = previousAnchorCooldown;
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void highSpeedAnchorUsesTheGravityAdjustedSweep(GameTestHelper helper) {
		BlockPos relativeBlock = new BlockPos(4, 3, 3);
		BlockPos blockPos = helper.absolutePos(relativeBlock);
		helper.getLevel().setBlock(blockPos, Blocks.OAK_SLAB.defaultBlockState(), Block.UPDATE_ALL);
		FlightRig rig = flyingHook(
			helper,
			GrappleMode.SWING,
			new Vec3(1.5D, 3.515D, 3.5D),
			new Vec3(3.84D, 0.0D, 0.0D),
			64.0D
		);

		rig.hook().tick();
		GrapplingBobberAccess access = (GrapplingBobberAccess) rig.hook();
		helper.assertValueEqual(HookState.ANCHORED_IDLE, access.hookAndReel$getHookState(), "gravity-adjusted sweep must hit the slab edge");
		helper.assertValueEqual(blockPos, access.hookAndReel$getAnchorBlockPos(), "anchor must retain the swept BlockHitResult block position");
		helper.assertValueEqual(Direction.WEST, access.hookAndReel$getAnchorFace(), "swept hit must retain the exact slab face");
		helper.assertTrue(
			Math.abs(access.hookAndReel$getAnchorPosition().x - (blockPos.getX() - 0.01D)) < 1.0E-6D,
			"anchor must use the exact hit position with only the existing surface offset"
		);
		helper.assertValueEqual(Vec3.ZERO, rig.hook().getDeltaMovement(), "successful anchor must stop in the collision tick");

		rig.hook().tick();
		helper.assertValueEqual(1, rig.rod().getDamageValue(), "anchored follow-up ticks must not charge durability twice");
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void highSpeedSweepUsesRealFullAndPartialCollisionShapes(GameTestHelper helper) {
		List<CollisionShapeCase> cases = List.of(
			new CollisionShapeCase("stone", Blocks.STONE.defaultBlockState(), 0.5D),
			new CollisionShapeCase("glass", Blocks.GLASS.defaultBlockState(), 0.5D),
			new CollisionShapeCase("leaves", Blocks.OAK_LEAVES.defaultBlockState(), 0.5D),
			new CollisionShapeCase("slab", Blocks.OAK_SLAB.defaultBlockState(), 0.25D),
			new CollisionShapeCase("stairs", Blocks.OAK_STAIRS.defaultBlockState(), 0.25D),
			new CollisionShapeCase("glass pane", Blocks.GLASS_PANE.defaultBlockState(), 0.5D),
			new CollisionShapeCase("iron bars", Blocks.IRON_BARS.defaultBlockState(), 0.5D),
			new CollisionShapeCase("fence", Blocks.OAK_FENCE.defaultBlockState(), 0.5D),
			new CollisionShapeCase("wall", Blocks.COBBLESTONE_WALL.defaultBlockState(), 0.5D),
			new CollisionShapeCase("trapdoor", Blocks.OAK_TRAPDOOR.defaultBlockState(), 0.1D)
		);
		BlockPos relativeBlock = new BlockPos(4, 3, 3);
		BlockPos blockPos = helper.absolutePos(relativeBlock);

		for (CollisionShapeCase testCase : cases) {
			helper.getLevel().setBlock(blockPos, testCase.state(), Block.UPDATE_ALL);
			FlightRig rig = flyingHook(
				helper,
				GrappleMode.SWING,
				new Vec3(1.5D, relativeBlock.getY() + testCase.yOffset(), relativeBlock.getZ() + 0.5D),
				new Vec3(4.5D, 0.0D, 0.0D),
				64.0D
			);
			rig.hook().tick();
			GrapplingBobberAccess access = (GrapplingBobberAccess) rig.hook();
			helper.assertValueEqual(
				HookState.ANCHORED_IDLE,
				access.hookAndReel$getHookState(),
				"high-speed sweep must respect the real " + testCase.name() + " collision shape"
			);
			helper.assertValueEqual(blockPos, access.hookAndReel$getAnchorBlockPos(), testCase.name() + " must report the first swept block");
			rig.hook().discard();
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void highSpeedPullChoosesTheNearestBlockOrEntity(GameTestHelper helper) {
		BlockPos blockingWall = helper.absolutePos(new BlockPos(4, 3, 2));
		helper.getLevel().setBlock(blockingWall, Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
		var behindWall = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 3, 2));
		FlightRig blockedRig = flyingHook(
			helper,
			GrappleMode.PULL,
			new Vec3(1.5D, 3.5D, 2.5D),
			new Vec3(6.0D, 0.0D, 0.0D),
			64.0D
		);
		blockedRig.hook().tick();
		helper.assertTrue(blockedRig.hook().getHookedIn() == null, "a wall must occlude an entity behind it");
		helper.assertValueEqual(
			HookState.HOOK_FLYING,
			((GrapplingBobberAccess) blockedRig.hook()).hookAndReel$getHookState(),
			"an immovable blocking wall must not become an entity pull"
		);
		helper.assertTrue(blockedRig.hook().getX() <= blockingWall.getX() + 0.01D, "blocked hook must not move through the wall this tick");
		blockedRig.hook().discard();
		behindWall.discard();

		BlockPos rearWall = helper.absolutePos(new BlockPos(6, 3, 5));
		helper.getLevel().setBlock(rearWall, Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
		var inFront = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 3, 5));
		FlightRig frontRig = flyingHook(
			helper,
			GrappleMode.PULL,
			new Vec3(1.5D, 3.5D, 5.5D),
			new Vec3(6.0D, 0.0D, 0.0D),
			64.0D
		);
		frontRig.hook().tick();
		helper.assertTrue(frontRig.hook().getHookedIn() == inFront, "an entity before the wall must be the nearest hit");
		helper.assertValueEqual(
			HookState.PULLING_ENTITY,
			((GrapplingBobberAccess) frontRig.hook()).hookAndReel$getHookState(),
			"nearest front entity must enter the existing entity pull state"
		);
		frontRig.hook().discard();
		inFront.discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void highSpeedBlockHitStartsExactlyOneBlockPull(GameTestHelper helper) {
		BlockPos blockPos = helper.absolutePos(new BlockPos(4, 3, 3));
		helper.getLevel().setBlock(blockPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		FlightRig rig = flyingHook(
			helper,
			GrappleMode.PULL,
			new Vec3(1.5D, 3.5D, 3.5D),
			new Vec3(5.0D, 0.0D, 0.0D),
			64.0D
		);

		rig.hook().tick();
		rig.hook().tick();
		helper.assertValueEqual(
			HookState.PULLING_BLOCK,
			((GrapplingBobberAccess) rig.hook()).hookAndReel$getHookState(),
			"first block hit must enter the existing block pull state"
		);
		helper.assertValueEqual(
			1,
			rig.level().getEntities(ModEntityTypes.PULLED_BLOCK, entity -> entity.getOriginPos().equals(blockPos)).size(),
			"the collision and follow-up tick must create exactly one pulled block entity"
		);
		rig.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void unloadedCollisionPathEndsTheHookWithoutLoadingChunks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Vec3 start = helper.absoluteVec(new Vec3(1.5D, 20.0D, 1.5D));
		int startChunkX = SectionPos.blockToSectionCoord((int) Math.floor(start.x));
		int startChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(start.z));
		int targetChunkX = Integer.MIN_VALUE;
		for (int distance = 1; distance <= 15 && targetChunkX == Integer.MIN_VALUE; distance++) {
			for (int direction : new int[] { 1, -1 }) {
				int candidate = startChunkX + distance * direction;
				if (!level.getChunkSource().hasChunk(candidate, startChunkZ)) {
					targetChunkX = candidate;
					break;
				}
			}
		}
		helper.assertTrue(targetChunkX != Integer.MIN_VALUE, "test server must expose an unloaded chunk within the bounded path window");
		Vec3 end = new Vec3(targetChunkX * 16.0D + 8.0D, start.y, start.z);
		FlightRig rig = flyingHookAbsolute(helper, GrappleMode.SWING, start, end.subtract(start), 4096.0D);

		rig.hook().tick();
		helper.assertTrue(rig.hook().isRemoved(), "hook must end before raycasting into an unloaded chunk");
		helper.assertFalse(level.getChunkSource().hasChunk(targetChunkX, startChunkZ), "collision preflight must not load the destination chunk");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void ordinaryFishingHookKeepsVanillaGravityAndMovement(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Vec3 start = helper.absoluteVec(new Vec3(2.5D, 12.0D, 2.5D));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(start.x - 1.0D, start.y, start.z);
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		FishingHook hook = new FishingHook(player, level, 0, 0);
		hook.setPos(start);
		hook.setDeltaMovement(1.0D, 0.0D, 0.0D);
		level.addFreshEntity(hook);

		hook.tick();
		helper.assertValueEqual(HookState.VANILLA, ((GrapplingBobberAccess) hook).hookAndReel$getHookState(), "ordinary fishing hook must not enter custom collision control");
		helper.assertTrue(Math.abs(hook.getY() - (start.y - 0.03D)) < 1.0E-6D, "ordinary fishing hook must retain vanilla gravity-before-move result");
		helper.assertTrue(Math.abs(hook.getDeltaMovement().y - (-0.03D * 0.92D)) < 1.0E-6D, "ordinary fishing hook must retain vanilla post-move drag");
		hook.discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void luckyOneAndTwoKeepVanillaWaitingWhileLuckyThreeWaitsForWater(GameTestHelper helper) {
		fillWater(helper, new BlockPos(3, 2, 3), 2, 1);
		VanillaFishingRig luckyOne = vanillaFishingRig(helper, new BlockPos(2, 2, 3), 1);
		VanillaFishingRig luckyTwo = vanillaFishingRig(helper, new BlockPos(4, 2, 3), 2);
		for (int tick = 0; tick < 25; tick++) {
			LuckyInstantCatchController.tickValidWater(luckyOne.hook());
			LuckyInstantCatchController.tickValidWater(luckyTwo.hook());
		}
		helper.assertValueEqual(0, luckyOne.access().hookAndReel$getLuckyWaterTicks(), "Lucky I must keep vanilla timing");
		helper.assertValueEqual(0, luckyTwo.access().hookAndReel$getLuckyWaterTicks(), "Lucky II must keep vanilla timing");
		helper.assertFalse(luckyOne.access().hookAndReel$isLuckyCatchArmed(), "Lucky I never arms instant catch");
		helper.assertFalse(luckyTwo.access().hookAndReel$isLuckyCatchArmed(), "Lucky II never arms instant catch");
		luckyOne.hook().discard();
		luckyTwo.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void luckyThreeCompletesExactlyOnceAfterTwentyValidWaterTicks(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		boolean previousMaster = config.luckyEnchantmentEnabled;
		boolean previousInstant = config.luckyThreeInstantCatchEnabled;
		double previousDelay = config.luckyThreeInstantCatchDelaySeconds;
		boolean previousRetract = config.luckyThreeAutoRetract;
		boolean previousEntities = config.allowFishingEntities;
		try {
			config.luckyEnchantmentEnabled = true;
			config.luckyThreeInstantCatchEnabled = true;
			config.luckyThreeInstantCatchDelaySeconds = 1.0D;
			config.luckyThreeAutoRetract = true;
			config.allowFishingEntities = false;
			fillWater(helper, new BlockPos(3, 2, 3), 2, 1);
			VanillaFishingRig rig = vanillaFishingRig(helper, new BlockPos(3, 2, 3), 3);
			int startingStat = rig.player().getStats().getValue(Stats.CUSTOM, Stats.FISH_CAUGHT);

			for (int tick = 0; tick < 19; tick++) {
				helper.assertFalse(LuckyInstantCatchController.tickValidWater(rig.hook()), "catch must not complete before 20 valid water ticks");
			}
			helper.assertFalse(rig.hook().isRemoved(), "hook remains active before the configured delay");
			helper.assertFalse(rig.access().hookAndReel$isLuckyCatchArmed(), "bite is not armed before tick 20");

			helper.assertFalse(LuckyInstantCatchController.tickValidWater(rig.hook()), "tick 20 only arms a legal bite");
			helper.assertTrue(rig.access().hookAndReel$isLuckyCatchArmed(), "valid water tick 20 arms the bite");
			helper.assertFalse(rig.hook().isRemoved(), "open-water state gets one final vanilla update before retrieval");
			helper.assertTrue(LuckyInstantCatchController.tickValidWater(rig.hook()), "the following valid-water tick auto-retrieves");
			helper.assertTrue(rig.hook().isRemoved(), "automatic retrieval removes the hook");
			helper.assertTrue(rig.access().hookAndReel$isInstantCatchTriggered(), "the cast is marked before rewards are generated");
			helper.assertValueEqual(1, rig.rod().getDamageValue(), "automatic vanilla retrieval consumes normal durability");
			helper.assertValueEqual(startingStat + 1, rig.player().getStats().getValue(Stats.CUSTOM, Stats.FISH_CAUGHT), "automatic retrieval awards the normal fishing statistic once");
		AABB rewards = new AABB(rig.hook().blockPosition()).inflate(12.0D);
			helper.assertTrue(!rig.level().getEntitiesOfClass(ItemEntity.class, rewards).isEmpty(), "automatic retrieval uses the fishing loot table");
			helper.assertTrue(!rig.level().getEntitiesOfClass(ExperienceOrb.class, rewards).isEmpty(), "automatic retrieval awards fishing experience");
		} finally {
			config.luckyEnchantmentEnabled = previousMaster;
			config.luckyThreeInstantCatchEnabled = previousInstant;
			config.luckyThreeInstantCatchDelaySeconds = previousDelay;
			config.luckyThreeAutoRetract = previousRetract;
			config.allowFishingEntities = previousEntities;
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void luckyThreeRejectsAirAndCustomHookStates(GameTestHelper helper) {
		VanillaFishingRig air = vanillaFishingRig(helper, new BlockPos(3, 6, 3), 3);
		for (int tick = 0; tick < 25; tick++) {
			LuckyInstantCatchController.tickValidWater(air.hook());
		}
		helper.assertValueEqual(0, air.access().hookAndReel$getLuckyWaterTicks(), "a hook in air never starts the timer");

		fillWater(helper, new BlockPos(5, 2, 5), 1, 0);
		VanillaFishingRig ability = vanillaFishingRig(helper, new BlockPos(5, 2, 5), 3);
		ability.access().hookAndReel$initializeHook(
			ability.rod(),
			InteractionHand.MAIN_HAND,
			GrappleMode.SWING,
			32.0D,
			32.0D,
			3
		);
		for (int tick = 0; tick < 25; tick++) {
			LuckyInstantCatchController.tickValidWater(ability.hook());
		}
		helper.assertValueEqual(0, ability.access().hookAndReel$getLuckyWaterTicks(), "SWING states never start the Lucky III timer");
		helper.assertFalse(ability.access().hookAndReel$isLuckyCatchArmed(), "custom hook states never manufacture a fishing bite");
		air.hook().discard();
		ability.hook().discard();
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void entityFishingIsExclusiveAwardsOnceAndStartsCooldownFreePull(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		boolean previousAllow = config.allowFishingEntities;
		double previousBase = config.fishingEntityBaseChance;
		double previousBonus = config.fishingEntityChanceBonusPerLuckyLevel;
		double previousMaximum = config.maximumFishingEntityChance;
		try {
			config.allowFishingEntities = true;
			config.fishingEntityBaseChance = 1.0D;
			config.fishingEntityChanceBonusPerLuckyLevel = 0.0D;
			config.maximumFishingEntityChance = 1.0D;
			fillWater(helper, new BlockPos(3, 3, 3), 2, 2);
			VanillaFishingRig rig = vanillaFishingRig(helper, new BlockPos(3, 3, 3), 0);
			int startingStat = rig.player().getStats().getValue(Stats.CUSTOM, Stats.FISH_CAUGHT);
			rig.access().hookAndReel$armForcedBite(10);
			int durability = rig.hook().retrieve(rig.rod());

			helper.assertValueEqual(1, durability, "an entity catch returns one normal fishing durability point");
			helper.assertTrue(rig.hook().isRemoved(), "entity catch completes and removes this cast");
			AABB area = new AABB(helper.absolutePos(new BlockPos(3, 3, 3))).inflate(12.0D);
			List<Mob> caught = rig.level().getEntitiesOfClass(Mob.class, area);
			helper.assertValueEqual(1, caught.size(), "entity branch creates exactly one living mob");
			helper.assertTrue(
				List.of(
					EntityType.COD,
					EntityType.SALMON,
					EntityType.TROPICAL_FISH,
					EntityType.PUFFERFISH,
					EntityType.SQUID,
					EntityType.GLOW_SQUID,
					EntityType.TADPOLE,
					EntityType.AXOLOTL
				).contains(caught.getFirst().getType()),
				"the default selection is one of the aquatic entity types"
			);
			helper.assertTrue(rig.level().getEntitiesOfClass(ItemEntity.class, area).isEmpty(), "entity and item loot branches are mutually exclusive");
			helper.assertValueEqual(1, rig.level().getEntitiesOfClass(ExperienceOrb.class, area).size(), "entity catch awards one experience result");
			helper.assertValueEqual(startingStat + 1, rig.player().getStats().getValue(Stats.CUSTOM, Stats.FISH_CAUGHT), "entity catch awards the fishing statistic once");
			helper.assertTrue(FishingEntityPullController.isPulling(caught.getFirst()), "new entity enters the dedicated fishing pull controller");
			helper.assertValueEqual(0L, HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.PULL, 200), "fishing pull never starts PULL cooldown");
			helper.assertValueEqual(0L, HookAbilityCooldownManager.remainingTicks(rig.rod(), rig.level(), HookAbilityCooldown.ANCHOR, 30), "fishing pull never starts ANCHOR cooldown");
		} finally {
			config.allowFishingEntities = previousAllow;
			config.fishingEntityBaseChance = previousBase;
			config.fishingEntityChanceBonusPerLuckyLevel = previousBonus;
			config.maximumFishingEntityChance = previousMaximum;
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void allDefaultFishableTypesCreateSafelyAndSpawnFailureFallsBackToLoot(GameTestHelper helper) {
		fillWater(helper, new BlockPos(3, 3, 3), 2, 2);
		VanillaFishingRig waterRig = vanillaFishingRig(helper, new BlockPos(3, 3, 3), 0);
		HookReelConfig config = HookReelConfigManager.get();
		for (EntityType<?> type : List.of(
			EntityType.COD,
			EntityType.SALMON,
			EntityType.TROPICAL_FISH,
			EntityType.PUFFERFISH,
			EntityType.SQUID,
			EntityType.GLOW_SQUID,
			EntityType.TADPOLE,
			EntityType.AXOLOTL
		)) {
			FishingEntitySpawner.SpawnedEntity spawned = FishingEntitySpawner.trySpawn(
				waterRig.hook(),
				new FishableEntitySelector.Selection(FishingEntityCategory.AQUATIC, type),
				waterRig.player(),
				config
			);
			helper.assertTrue(spawned != null, "default fishable type must create in loaded safe water: " + type);
			helper.assertValueEqual(type, spawned.mob().getType(), "spawner retains the selected entity type");
			spawned.mob().discard();
		}
		waterRig.hook().discard();

		boolean previousAllow = config.allowFishingEntities;
		double previousBase = config.fishingEntityBaseChance;
		double previousMaximum = config.maximumFishingEntityChance;
		try {
			config.allowFishingEntities = true;
			config.fishingEntityBaseChance = 1.0D;
			config.maximumFishingEntityChance = 1.0D;
			VanillaFishingRig dryRig = vanillaFishingRig(helper, new BlockPos(7, 8, 7), 0);
			dryRig.access().hookAndReel$armForcedBite(10);
			int durability = dryRig.hook().retrieve(dryRig.rod());
			AABB dryArea = new AABB(helper.absolutePos(new BlockPos(7, 8, 7))).inflate(5.0D);
			helper.assertValueEqual(1, durability, "failed entity creation falls back to normal fishing durability");
			helper.assertTrue(!dryRig.level().getEntitiesOfClass(ItemEntity.class, dryArea).isEmpty(), "failed entity creation falls back to fishing loot");
		} finally {
			config.allowFishingEntities = previousAllow;
			config.fishingEntityBaseChance = previousBase;
			config.maximumFishingEntityChance = previousMaximum;
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void categorySelectionHonorsSwitchesWeightsDimensionsBlacklistAndDragonGate(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		HookReelConfig snapshot = config.copy();
		ServerLevel level = helper.getLevel();
		try {
			config.allowFishingEntities = true;
			disableAllFishingEntityCategories(config);
			config.allowAquaticEntities = true;
			config.aquaticEntityCategoryWeight = 70.0D;
			for (int attempt = 0; attempt < 32; attempt++) {
				FishableEntitySelector.Selection selection = FishableEntitySelector.select(level, config);
				helper.assertTrue(selection != null, "an enabled non-empty aquatic category can be selected");
				helper.assertValueEqual(FishingEntityCategory.AQUATIC, selection.category(), "only the enabled category participates");
				helper.assertTrue(selection.type().is(ModEntityTypeTags.FISHABLE_AQUATIC_ENTITIES), "the result comes from the aquatic tag");
			}

			disableAllFishingEntityCategories(config);
			helper.assertTrue(FishableEntitySelector.select(level, config) == null, "all categories disabled returns to normal loot");

			config.allowNetherEntities = true;
			config.netherEntityCategoryWeight = 2.5D;
			config.netherEntitiesOnlyInNether = true;
			helper.assertTrue(FishableEntitySelector.select(level, config) == null, "Nether-only categories do not run in the overworld");
			config.netherEntitiesOnlyInNether = false;
			FishableEntitySelector.Selection netherSelection = FishableEntitySelector.select(level, config);
			helper.assertTrue(netherSelection != null, "Nether entities can be selected in any dimension when unrestricted");
			helper.assertValueEqual(FishingEntityCategory.NETHER, netherSelection.category(), "the unrestricted result stays in the Nether category");
			helper.assertTrue(netherSelection.type().is(ModEntityTypeTags.FISHABLE_NETHER_ENTITIES), "the unrestricted result comes from the Nether tag");

			disableAllFishingEntityCategories(config);
			config.allowAquaticEntities = true;
			config.aquaticEntityCategoryWeight = 0.0D;
			helper.assertTrue(FishableEntitySelector.select(level, config) == null, "zero effective category weight returns to normal loot without division");

			disableAllFishingEntityCategories(config);
			config.allowBossEntities = true;
			config.bossEntityCategoryWeight = 0.5D;
			config.allowEnderDragonFishing = false;
			helper.assertFalse(
				FishableEntitySelector.isAllowed(EntityType.ENDER_DRAGON, FishingEntityCategory.BOSS, level, config),
				"the Boss switch alone never enables the Ender Dragon"
			);
			config.allowEnderDragonFishing = true;
			helper.assertTrue(
				FishableEntitySelector.isAllowed(EntityType.ENDER_DRAGON, FishingEntityCategory.BOSS, level, config),
				"both switches allow the Ender Dragon to enter the candidate pool"
			);
			VanillaFishingRig dragonRig = vanillaFishingRig(helper, new BlockPos(4, 4, 4), 0);
			helper.assertTrue(
				FishingEntitySpawner.trySpawn(
					dragonRig.hook(),
					new FishableEntitySelector.Selection(FishingEntityCategory.BOSS, EntityType.ENDER_DRAGON),
					dragonRig.player(),
					config
				) == null,
				"the current implementation safely rejects Ender Dragon creation"
			);
			dragonRig.hook().discard();

			helper.assertTrue(EntityType.PLAYER.is(ModEntityTypeTags.UNFISHABLE_ENTITIES), "the player is always blacklisted");
			helper.assertTrue(ModEntityTypes.PULLED_BLOCK.is(ModEntityTypeTags.UNFISHABLE_ENTITIES), "the technical pulled-block entity is always blacklisted");
		} finally {
			restoreFishingEntityConfig(config, snapshot);
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void landNetherAndBossSpawnsAreFreshAndLargePullIsSingleImpulse(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		HookReelConfig snapshot = config.copy();
		try {
			config.allowFishingEntities = true;
			config.allowLandAnimals = true;
			config.allowLandMonsters = true;
			config.allowNetherEntities = true;
			config.netherEntitiesOnlyInNether = false;
			config.allowBossEntities = true;
			config.allowEnderDragonFishing = true;
			fillFloor(helper, new BlockPos(4, 2, 4), 3);
			VanillaFishingRig rig = vanillaFishingRig(helper, new BlockPos(4, 3, 4), 0);

			FishingEntitySpawner.SpawnedEntity animal = spawnSelected(rig, FishingEntityCategory.LAND_ANIMAL, EntityType.DONKEY, config);
			helper.assertTrue(animal != null, "a land animal can spawn in loaded dry space");
			helper.assertTrue(animal.mob() instanceof AbstractChestedHorse, "the selected donkey is created as a fresh chested horse");
			helper.assertFalse(((AbstractChestedHorse) animal.mob()).hasChest(), "a newly caught donkey does not copy a chest");
			helper.assertTrue(animal.mob() instanceof AbstractHorse, "the selected donkey exposes normal horse state");
			helper.assertFalse(((AbstractHorse) animal.mob()).isTamed(), "a newly caught animal does not copy an owner");
			helper.assertTrue(animal.mob().getPassengers().isEmpty(), "a newly caught animal has no copied passengers");
			animal.mob().discard();

			FishingEntitySpawner.SpawnedEntity monster = spawnSelected(rig, FishingEntityCategory.LAND_MONSTER, EntityType.ZOMBIE, config);
			helper.assertTrue(monster != null, "a land monster can spawn in loaded dry space");
			helper.assertFalse(monster.mob().isNoAi(), "a caught monster keeps its normal hostile AI");
			monster.mob().discard();

			FishingEntitySpawner.SpawnedEntity nether = spawnSelected(rig, FishingEntityCategory.NETHER, EntityType.BLAZE, config);
			helper.assertTrue(nether != null, "an enabled Nether entity can spawn outside the Nether when unrestricted");
			helper.assertValueEqual(EntityType.BLAZE, nether.mob().getType(), "the selected Nether type is retained");
			nether.mob().discard();

			FishingEntitySpawner.SpawnedEntity boss = spawnSelected(rig, FishingEntityCategory.BOSS, EntityType.WITHER, config);
			helper.assertTrue(boss != null, "a non-dragon Boss can spawn after the expanded safety search succeeds");
			helper.assertTrue(boss.large(), "Boss entities use the large-entity safety and pull policy");
			FishingEntityPullController.start(rig.player(), boss.mob(), boss.large());
			helper.assertTrue(boss.mob().getDeltaMovement().lengthSqr() > 0.0D, "a Boss receives one bounded initial fishing velocity");
			helper.assertFalse(FishingEntityPullController.isPulling(boss.mob()), "a Boss never enters the persistent fishing pull session");
			boss.mob().discard();
			rig.hook().discard();
		} finally {
			restoreFishingEntityConfig(config, snapshot);
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void disabledCategoriesFallBackToNormalFishingLoot(GameTestHelper helper) {
		HookReelConfig config = HookReelConfigManager.get();
		HookReelConfig snapshot = config.copy();
		try {
			config.allowFishingEntities = true;
			config.fishingEntityBaseChance = 1.0D;
			config.fishingEntityChanceBonusPerLuckyLevel = 0.0D;
			config.maximumFishingEntityChance = 1.0D;
			disableAllFishingEntityCategories(config);
			fillWater(helper, new BlockPos(3, 3, 3), 2, 2);
			VanillaFishingRig rig = vanillaFishingRig(helper, new BlockPos(3, 3, 3), 0);
			rig.access().hookAndReel$armForcedBite(10);
			int durability = rig.hook().retrieve(rig.rod());
			AABB area = new AABB(helper.absolutePos(new BlockPos(3, 3, 3))).inflate(8.0D);
			helper.assertValueEqual(1, durability, "disabled categories keep normal fishing durability");
			helper.assertTrue(!rig.level().getEntitiesOfClass(ItemEntity.class, area).isEmpty(), "disabled categories fall back to normal item loot");
			helper.assertTrue(rig.level().getEntitiesOfClass(Mob.class, area).isEmpty(), "disabled categories never create an entity result");
		} finally {
			restoreFishingEntityConfig(config, snapshot);
		}
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void simultaneousHighSpeedAnchorsRemainPlayerScoped(GameTestHelper helper) {
		BlockPos firstBlock = helper.absolutePos(new BlockPos(4, 3, 2));
		BlockPos secondBlock = helper.absolutePos(new BlockPos(4, 3, 6));
		helper.getLevel().setBlock(firstBlock, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		helper.getLevel().setBlock(secondBlock, Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
		FlightRig first = flyingHook(helper, GrappleMode.SWING, new Vec3(1.5D, 3.5D, 2.5D), new Vec3(5.0D, 0.0D, 0.0D), 64.0D);
		FlightRig second = flyingHook(helper, GrappleMode.SWING, new Vec3(1.5D, 3.5D, 6.5D), new Vec3(5.0D, 0.0D, 0.0D), 64.0D);

		first.hook().tick();
		second.hook().tick();
		helper.assertValueEqual(firstBlock, ((GrapplingBobberAccess) first.hook()).hookAndReel$getAnchorBlockPos(), "first player must retain its own anchor hit");
		helper.assertValueEqual(secondBlock, ((GrapplingBobberAccess) second.hook()).hookAndReel$getAnchorBlockPos(), "second player must retain its own anchor hit");
		helper.assertValueEqual(1, first.rod().getDamageValue(), "first anchor must charge once");
		helper.assertValueEqual(1, second.rod().getDamageValue(), "second anchor must charge once");
		first.hook().discard();
		second.hook().discard();
		helper.succeed();
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

	private static VanillaFishingRig vanillaFishingRig(
		GameTestHelper helper,
		BlockPos relativeHookPos,
		int luckyLevel
	) {
		ServerLevel level = helper.getLevel();
		BlockPos hookPos = helper.absolutePos(relativeHookPos);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(helper.absoluteVec(new Vec3(7.5D, 3.0D, 7.5D)));
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		if (luckyLevel > 0) {
			enchant(level, rod, ModEnchantments.LUCKY_CATCH, luckyLevel);
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		FishingHook hook = new FishingHook(player, level, 0, 0);
		hook.setPos(Vec3.atCenterOf(hookPos));
		level.addFreshEntity(hook);
		GrapplingBobberAccess access = (GrapplingBobberAccess) hook;
		access.hookAndReel$initializeFishingCast(rod, InteractionHand.MAIN_HAND);
		return new VanillaFishingRig(level, player, rod, hook, access);
	}

	private static void fillWater(
		GameTestHelper helper,
		BlockPos relativeCenter,
		int horizontalRadius,
		int verticalRadius
	) {
		BlockPos center = helper.absolutePos(relativeCenter);
		for (int y = -verticalRadius; y <= verticalRadius; y++) {
			for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
				for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
					helper.getLevel().setBlock(center.offset(x, y, z), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
	}

	private static void fillFloor(GameTestHelper helper, BlockPos relativeCenter, int horizontalRadius) {
		BlockPos center = helper.absolutePos(relativeCenter);
		for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
			for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
				helper.getLevel().setBlock(center.offset(x, 0, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
	}

	private static FishingEntitySpawner.SpawnedEntity spawnSelected(
		VanillaFishingRig rig,
		FishingEntityCategory category,
		EntityType<?> type,
		HookReelConfig config
	) {
		return FishingEntitySpawner.trySpawn(
			rig.hook(),
			new FishableEntitySelector.Selection(category, type),
			rig.player(),
			config
		);
	}

	private static void disableAllFishingEntityCategories(HookReelConfig config) {
		config.allowAquaticEntities = false;
		config.allowLandAnimals = false;
		config.allowLandMonsters = false;
		config.allowNetherEntities = false;
		config.allowBossEntities = false;
	}

	private static void restoreFishingEntityConfig(HookReelConfig target, HookReelConfig source) {
		target.allowFishingEntities = source.allowFishingEntities;
		target.fishingEntityBaseChance = source.fishingEntityBaseChance;
		target.fishingEntityChanceBonusPerLuckyLevel = source.fishingEntityChanceBonusPerLuckyLevel;
		target.maximumFishingEntityChance = source.maximumFishingEntityChance;
		target.allowAquaticEntities = source.allowAquaticEntities;
		target.allowLandAnimals = source.allowLandAnimals;
		target.allowLandMonsters = source.allowLandMonsters;
		target.allowNetherEntities = source.allowNetherEntities;
		target.netherEntitiesOnlyInNether = source.netherEntitiesOnlyInNether;
		target.allowBossEntities = source.allowBossEntities;
		target.allowEnderDragonFishing = source.allowEnderDragonFishing;
		target.aquaticEntityCategoryWeight = source.aquaticEntityCategoryWeight;
		target.landAnimalCategoryWeight = source.landAnimalCategoryWeight;
		target.landMonsterCategoryWeight = source.landMonsterCategoryWeight;
		target.netherEntityCategoryWeight = source.netherEntityCategoryWeight;
		target.bossEntityCategoryWeight = source.bossEntityCategoryWeight;
	}

	private static FlightRig flyingHook(
		GameTestHelper helper,
		GrappleMode mode,
		Vec3 relativeStart,
		Vec3 velocity,
		double maximumRange
	) {
		return flyingHookAbsolute(helper, mode, helper.absoluteVec(relativeStart), velocity, maximumRange);
	}

	private static FlightRig flyingHookAbsolute(
		GameTestHelper helper,
		GrappleMode mode,
		Vec3 start,
		Vec3 velocity,
		double maximumRange
	) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 ownerOffset = velocity.lengthSqr() > 1.0E-9D ? velocity.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
		player.moveTo(start.subtract(ownerOffset));
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, mode == GrappleMode.SWING ? ModEnchantments.ANCHOR_HOOK : ModEnchantments.GRAPPLING_HOOK, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		FishingHook hook = new FishingHook(player, level, 0, 0);
		hook.setPos(start);
		((GrapplingBobberAccess) hook).hookAndReel$initializeHook(
			rod,
			InteractionHand.MAIN_HAND,
			mode,
			maximumRange,
			maximumRange,
			mode == GrappleMode.SWING ? 3 : 0
		);
		hook.setDeltaMovement(velocity);
		level.addFreshEntity(hook);
		return new FlightRig(level, player, rod, hook);
	}

	private static SwingRig beginSwing(GameTestHelper helper, BlockPos relativePos, BlockState state) {
		ServerLevel level = helper.getLevel();
		BlockPos absolutePos = helper.absolutePos(relativePos);
		level.setBlock(absolutePos, state, Block.UPDATE_ALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(helper.absoluteVec(new Vec3(7.0D, 3.0D, 7.0D)));
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, ModEnchantments.ANCHOR_HOOK, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, rod);
		FishingHook hook = new FishingHook(player, level, 0, 0);
		hook.setPos(Vec3.atCenterOf(absolutePos));
		((GrapplingBobberAccess) hook).hookAndReel$initializeHook(
			rod,
			InteractionHand.MAIN_HAND,
			GrappleMode.SWING,
			64.0D,
			64.0D,
			3
		);
		level.addFreshEntity(hook);
		Vec3 hitLocation = new Vec3(absolutePos.getX() + 0.35D, absolutePos.getY() + 1.0D, absolutePos.getZ() + 0.65D);
		BlockHitResult hitResult = new BlockHitResult(hitLocation, Direction.UP, absolutePos, false);
		return new SwingRig(level, player, rod, hook, absolutePos, hitResult);
	}

	private static ServerPlayer wallPlayer(GameTestHelper helper, BlockPos wall) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(wall.getX() - 0.4D, wall.getY(), wall.getZ() + 0.5D);
		player.setOnGround(false);
		return player;
	}

	private static ItemStack anchorRod(ServerLevel level) {
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, ModEnchantments.ANCHOR_HOOK, 3);
		return rod;
	}

	private static ItemStack dualEnchantedRod(ServerLevel level) {
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		enchant(level, rod, ModEnchantments.GRAPPLING_HOOK, 3);
		enchant(level, rod, ModEnchantments.ANCHOR_HOOK, 3);
		return rod;
	}

	private static void enchant(
		ServerLevel level,
		ItemStack stack,
		net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment,
		int enchantmentLevel
	) {
		stack.enchant(
			level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(enchantment),
			enchantmentLevel
		);
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

	private record SwingRig(
		ServerLevel level,
		ServerPlayer player,
		ItemStack rod,
		FishingHook hook,
		BlockPos blockPos,
		BlockHitResult hitResult
	) {
	}

	private record FlightRig(ServerLevel level, ServerPlayer player, ItemStack rod, FishingHook hook) {
	}

	private record VanillaFishingRig(
		ServerLevel level,
		ServerPlayer player,
		ItemStack rod,
		FishingHook hook,
		GrapplingBobberAccess access
	) {
	}

	private record CollisionShapeCase(String name, BlockState state, double yOffset) {
	}
}
