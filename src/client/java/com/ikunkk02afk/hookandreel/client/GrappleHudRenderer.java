package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.grapple.GrappleEnchantmentLogic;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

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
		if (
			minecraft.options.hideGui
				|| minecraft.player == null
				|| minecraft.level == null
				|| !GrappleChargeClientState.isActive()
				|| !minecraft.player.isUsingItem()
				|| minecraft.player.fishing != null
				|| GrappleEnchantmentLogic.getLevel(minecraft.level, minecraft.player.getUseItem()) <= 0
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
}
