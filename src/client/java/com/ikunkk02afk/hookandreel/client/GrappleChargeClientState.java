package com.ikunkk02afk.hookandreel.client;

import com.ikunkk02afk.hookandreel.network.GrappleChargeStatePayload;

public final class GrappleChargeClientState {
	private static boolean active;
	private static long startGameTime;
	private static int maximumChargeTicks;

	private GrappleChargeClientState() {
	}

	public static void update(GrappleChargeStatePayload payload) {
		active = payload.active();
		startGameTime = payload.startGameTime();
		maximumChargeTicks = Math.max(1, payload.maximumChargeTicks());
	}

	public static void clear() {
		active = false;
		startGameTime = 0L;
		maximumChargeTicks = 1;
	}

	public static boolean isActive() {
		return active;
	}

	public static double progress(double gameTime) {
		if (!active) {
			return 0.0D;
		}
		return Math.clamp((gameTime - startGameTime) / maximumChargeTicks, 0.0D, 1.0D);
	}
}
