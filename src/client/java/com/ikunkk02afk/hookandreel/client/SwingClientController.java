package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.grapple.GrapplingBobberAccess;
import com.ikunkk02afk.hookandreel.grapple.HookState;
import com.ikunkk02afk.hookandreel.network.StartReelUpRequestPayload;
import com.ikunkk02afk.hookandreel.network.SwingInputPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

public final class SwingClientController {
	private static final int INPUT_HEARTBEAT_TICKS = 5;
	private static byte lastLeft;
	private static byte lastForward;
	private static int ticksSinceInput;
	private static boolean wasActive;
	private static boolean jumpWasDown;

	private SwingClientController() {
	}

	public static void tick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		boolean jumpDown = minecraft.options.keyJump.isDown();
		if (player == null || minecraft.level == null) {
			wasActive = false;
			ticksSinceInput = 0;
			jumpWasDown = jumpDown;
			return;
		}

		FishingHook hook = player.fishing;
		GrapplingBobberAccess access = hook instanceof GrapplingBobberAccess candidate ? candidate : null;
		boolean anchored = access != null && access.hookAndReel$getHookState().isAnchored();
		double gameTime = minecraft.level.getGameTime();
		boolean wallCling = WallClingClientState.isActive(gameTime);
		boolean active = anchored || wallCling;
		if (active) {
			byte left = quantize(player.input.leftImpulse);
			byte forward = quantize(player.input.forwardImpulse);
			if (
				ClientPlayNetworking.canSend(SwingInputPayload.TYPE)
					&& (!wasActive || left != lastLeft || forward != lastForward || ++ticksSinceInput >= INPUT_HEARTBEAT_TICKS)
			) {
				ClientPlayNetworking.send(new SwingInputPayload(left, forward));
				lastLeft = left;
				lastForward = forward;
				ticksSinceInput = 0;
			}
			wasActive = true;
		} else {
			wasActive = false;
			ticksSinceInput = 0;
		}

		boolean reelRequestState = wallCling
			|| access != null && access.hookAndReel$getHookState() == HookState.ANCHORED_IDLE;
		if (
			jumpDown
				&& !jumpWasDown
				&& reelRequestState
				&& ClientPlayNetworking.canSend(StartReelUpRequestPayload.TYPE)
		) {
			ClientPlayNetworking.send(StartReelUpRequestPayload.INSTANCE);
		}
		jumpWasDown = jumpDown;
	}

	public static void clear() {
		wasActive = false;
		jumpWasDown = false;
		ticksSinceInput = 0;
		lastLeft = 0;
		lastForward = 0;
	}

	private static byte quantize(float value) {
		return (byte) Math.round(Math.clamp(value, -1.0F, 1.0F) * 127.0F);
	}
}
