package com.ikunkk02afk.hookandreel.grapple;

public enum HookState {
	VANILLA,
	HOOK_FLYING,
	PULLING_ENTITY,
	PULLING_ITEM,
	PULLING_BLOCK,
	ANCHORED_IDLE,
	REELING_UP,
	RAPPELLING;

	public boolean isPulling() {
		return this == PULLING_ENTITY || this == PULLING_ITEM || this == PULLING_BLOCK;
	}

	public boolean isAnchored() {
		return this == ANCHORED_IDLE || this == REELING_UP || this == RAPPELLING;
	}

	public static HookState byId(int id) {
		HookState[] values = values();
		return id >= 0 && id < values.length ? values[id] : VANILLA;
	}
}
