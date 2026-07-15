package com.ikunkk02afk.hookandreel.entity;

import com.ikunkk02afk.hookandreel.grapple.GrappleBlockController;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PulledBlockEntity extends Entity {
	private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(
		PulledBlockEntity.class,
		EntityDataSerializers.BLOCK_STATE
	);

	private OwnershipState ownershipState = OwnershipState.PENDING;
	private BlockPos originPos = BlockPos.ZERO;
	private ResourceKey<Level> originDimension = Level.OVERWORLD;
	private UUID playerUuid;
	private UUID hookUuid;
	private int survivalTicks;
	private boolean resolving;

	public PulledBlockEntity(EntityType<? extends PulledBlockEntity> type, Level level) {
		super(type, level);
		blocksBuilding = true;
	}

	public static PulledBlockEntity pending(
		Level level,
		BlockState blockState,
		BlockPos originPos,
		ResourceKey<Level> originDimension,
		UUID playerUuid,
		UUID hookUuid
	) {
		PulledBlockEntity entity = new PulledBlockEntity(ModEntityTypes.PULLED_BLOCK, level);
		entity.setBlockState(blockState);
		entity.originPos = originPos.immutable();
		entity.originDimension = originDimension;
		entity.playerUuid = playerUuid;
		entity.hookUuid = hookUuid;
		entity.setPos(originPos.getX() + 0.5D, originPos.getY(), originPos.getZ() + 0.5D);
		return entity;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_BLOCK_STATE, Blocks.STONE.defaultBlockState());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setBlockState(NbtUtils.readBlockState(
			level().holderLookup(Registries.BLOCK),
			tag.getCompound("BlockState")
		));
		originPos = BlockPos.of(tag.getLong("OriginPos"));
		ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("OriginDimension"));
		if (dimensionId != null) {
			originDimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
		}
		playerUuid = tag.hasUUID("Player") ? tag.getUUID("Player") : null;
		hookUuid = tag.hasUUID("Hook") ? tag.getUUID("Hook") : null;
		survivalTicks = Math.max(0, tag.getInt("SurvivalTicks"));
		try {
			ownershipState = OwnershipState.valueOf(tag.getString("OwnershipState").toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			ownershipState = OwnershipState.CARRYING;
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
		tag.putLong("OriginPos", originPos.asLong());
		tag.putString("OriginDimension", originDimension.location().toString());
		if (playerUuid != null) {
			tag.putUUID("Player", playerUuid);
		}
		if (hookUuid != null) {
			tag.putUUID("Hook", hookUuid);
		}
		tag.putInt("SurvivalTicks", survivalTicks);
		tag.putString("OwnershipState", ownershipState.name());
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide) {
			GrappleBlockController.tick(this);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (
			!level().isClientSide
				&& ownershipState == OwnershipState.CARRYING
				&& reason != RemovalReason.UNLOADED_TO_CHUNK
				&& !resolving
		) {
			GrappleBlockController.onPulledBlockRemoved(this);
			if (ownershipState == OwnershipState.CARRYING) {
				return;
			}
		}
		if (!isRemoved()) {
			super.remove(reason);
		}
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean canUsePortal(boolean allowVehicles) {
		return false;
	}

	public BlockState getBlockState() {
		return entityData.get(DATA_BLOCK_STATE);
	}

	public void setBlockState(BlockState state) {
		entityData.set(DATA_BLOCK_STATE, state);
	}

	public OwnershipState getOwnershipState() {
		return ownershipState;
	}

	public void markCarrying() {
		if (ownershipState == OwnershipState.PENDING) {
			ownershipState = OwnershipState.CARRYING;
		}
	}

	public void markResolved() {
		ownershipState = OwnershipState.RESOLVED;
		resolving = true;
	}

	public BlockPos getOriginPos() {
		return originPos;
	}

	public ResourceKey<Level> getOriginDimension() {
		return originDimension;
	}

	@Nullable
	public UUID getPlayerUuid() {
		return playerUuid;
	}

	@Nullable
	public UUID getHookUuid() {
		return hookUuid;
	}

	public int incrementAndGetSurvivalTicks() {
		return ++survivalTicks;
	}

	public enum OwnershipState {
		PENDING,
		CARRYING,
		RESOLVED
	}
}
