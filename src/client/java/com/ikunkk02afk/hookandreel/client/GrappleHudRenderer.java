package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.config.HookReelConfigManager;
import com.ikunkk02afk.hookandreel.grapple.AnchorHookLogic;
import com.ikunkk02afk.hookandreel.grapple.GrappleEnchantmentLogic;
import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookState;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GrappleHudRenderer {
	private static final int BAR_WIDTH = 80;
	private static final int BAR_HEIGHT = 6;
	private static final int BORDER_COLOR = 0xCC000000;
	private static final int BACKGROUND_COLOR = 0xAA202020;
	private static final int FILL_COLOR = 0xFF36C95F;
	private static final int FULL_COLOR = 0xFFFFC83D;

	private GrappleHudRenderer() {
	}

	public static void initialize() {
		HudRenderCallback.EVENT.register(GrappleHudRenderer::render);
	}

	private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.hideGui || minecraft.player == null || minecraft.level == null) {
			return;
		}
		renderCharge(graphics, deltaTracker, minecraft);
		renderRopeLength(graphics, minecraft);
		renderClimbTimer(graphics, deltaTracker, minecraft);
	}

	private static void renderCharge(GuiGraphics graphics, DeltaTracker deltaTracker, Minecraft minecraft) {
		ItemStack useItem = minecraft.player.getUseItem();
		if (
			!GrappleChargeClientState.isActive()
				|| !minecraft.player.isUsingItem()
				|| minecraft.player.fishing != null
				|| GrappleEnchantmentLogic.getLevel(minecraft.level, useItem) <= 0
					&& AnchorHookLogic.getLevel(minecraft.level, useItem) <= 0
		) {
			return;
		}

		double gameTime = minecraft.level.getGameTime()
			+ deltaTracker.getGameTimeDeltaPartialTick(false);
		double progress = GrappleChargeClientState.progress(gameTime);
		int left = (graphics.guiWidth() - BAR_WIDTH) / 2;
		int top = graphics.guiHeight() / 2 + 12;
		int filledWidth = (int) Math.round((BAR_WIDTH - 2) * progress);

		graphics.fill(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, BORDER_COLOR);
		graphics.fill(left + 1, top + 1, left + BAR_WIDTH - 1, top + BAR_HEIGHT - 1, BACKGROUND_COLOR);
		if (filledWidth > 0) {
			graphics.fill(
				left + 1,
				top + 1,
				left + 1 + filledWidth,
				top + BAR_HEIGHT - 1,
				progress >= 1.0D ? FULL_COLOR : FILL_COLOR
			);
		}
	}

	private static void renderRopeLength(GuiGraphics graphics, Minecraft minecraft) {
		if (
			!HookReelConfigManager.get().showRopeLengthHud
				|| minecraft.player.fishing == null
				|| !(minecraft.player.fishing instanceof GrapplingBobberAccess access)
				|| !access.hookAndReel$getHookState().isAnchored()
		) {
			return;
		}
		Component text = Component.translatable(
			"hud.hook_and_reel.rope_length",
			String.format(Locale.ROOT, "%.1f", access.hookAndReel$getRopeLength()),
			String.format(Locale.ROOT, "%.1f", access.hookAndReel$getMaximumRopeLength())
		);
		graphics.drawCenteredString(
			minecraft.font,
			text,
			graphics.guiWidth() / 2,
			graphics.guiHeight() / 2 + 23,
			0xFFFFFFFF
		);
	}

	private static void renderClimbTimer(GuiGraphics graphics, DeltaTracker deltaTracker, Minecraft minecraft) {
		if (!HookReelConfigManager.get().showWallClingTimerHud) {
			return;
		}
		double gameTime = minecraft.level.getGameTime()
			+ deltaTracker.getGameTimeDeltaPartialTick(false);
		double remaining = WallClingClientState.remainingSeconds(gameTime);
		if (remaining <= 0.0D) {
			return;
		}
		Component text = Component.translatable(
			"hud.hook_and_reel.wall_cling_timer",
			String.format(Locale.ROOT, "%.1f", remaining)
		);
		graphics.drawCenteredString(
			minecraft.font,
			text,
			graphics.guiWidth() / 2,
			graphics.guiHeight() / 2 + 23,
			0xFFFFFFFF
		);
	}
}
