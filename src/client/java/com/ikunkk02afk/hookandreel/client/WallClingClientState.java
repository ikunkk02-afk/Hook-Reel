package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.network.WallClingStatePayload;

public final class WallClingClientState {
	private static boolean active;
	private static long endGameTime;

	private WallClingClientState() {
	}

	public static void update(WallClingStatePayload payload) {
		active = payload.active();
		endGameTime = payload.active() ? payload.endGameTime() : 0L;
	}

	public static void clear() {
		active = false;
		endGameTime = 0L;
	}

	public static boolean isActive(double gameTime) {
		return active && gameTime < endGameTime;
	}

	public static double remainingSeconds(double gameTime) {
		return isActive(gameTime) ? Math.max(0.0D, (endGameTime - gameTime) / 20.0D) : 0.0D;
	}
}
