package com.ikunkk02afk.hookandreel.grapple;

import net.minecraft.server.level.ServerPlayer;

public enum SwingState {
	IDLE,
	HOOK_FLYING,
	ANCHORED_IDLE,
	REELING_UP,
	RAPPELLING,
	WALL_CLING;

	public static SwingState get(ServerPlayer player) {
		if (WallClingController.isActive(player)) {
			return WALL_CLING;
		}
		if (!(player.fishing instanceof GrapplingBobberAccess access)) {
			return IDLE;
		}
		if (access.hookAndReel$getLaunchMode() != com.ikunkk02afk.hookandreel.component.GrappleMode.SWING) {
			return IDLE;
		}
		return switch (access.hookAndReel$getHookState()) {
			case HOOK_FLYING -> HOOK_FLYING;
			case ANCHORED_IDLE -> ANCHORED_IDLE;
			case REELING_UP -> REELING_UP;
			case RAPPELLING -> RAPPELLING;
			default -> IDLE;
		};
	}
}
