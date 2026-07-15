package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class PulledBlockRenderer extends EntityRenderer<PulledBlockEntity> {
	private final BlockRenderDispatcher blockRenderer;

	public PulledBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		blockRenderer = context.getBlockRenderDispatcher();
		shadowRadius = 0.5F;
	}

	@Override
	public void render(
		PulledBlockEntity entity,
		float yaw,
		float partialTick,
		PoseStack poseStack,
		MultiBufferSource buffers,
		int packedLight
	) {
		BlockState state = entity.getBlockState();
		if (state.getRenderShape() == RenderShape.MODEL) {
			poseStack.pushPose();
			poseStack.translate(-0.5D, 0.0D, -0.5D);
			VertexConsumer consumer = buffers.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state));
			blockRenderer.getModelRenderer().tesselateBlock(
				entity.level(),
				blockRenderer.getBlockModel(state),
				state,
				entity.blockPosition(),
				poseStack,
				consumer,
				false,
				RandomSource.create(),
				entity.getUUID().getLeastSignificantBits(),
				OverlayTexture.NO_OVERLAY
			);
			poseStack.popPose();
		}
		super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(PulledBlockEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
