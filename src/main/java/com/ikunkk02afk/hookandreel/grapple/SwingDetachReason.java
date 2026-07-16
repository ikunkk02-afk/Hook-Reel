package com.ikunkk02afk.hookandreel.grapple;

public enum SwingDetachReason {
	PLAYER_RETRIEVE(CooldownPolicy.STATE_BASED, true),
	MODE_SWITCH(CooldownPolicy.STATE_BASED, false),
	AUTO_GROUND(CooldownPolicy.FULL, false),
	WALL_CLING_CAPTURE(CooldownPolicy.NONE, false),
	LIFECYCLE_ABORT(CooldownPolicy.NONE, false);

	private final CooldownPolicy cooldownPolicy;
	private final boolean armsWallCapture;

	SwingDetachReason(CooldownPolicy cooldownPolicy, boolean armsWallCapture) {
		this.cooldownPolicy = cooldownPolicy;
		this.armsWallCapture = armsWallCapture;
	}

	public CooldownPolicy cooldownPolicy() {
		return cooldownPolicy;
	}

	public boolean armsWallCapture() {
		return armsWallCapture;
	}

	public enum CooldownPolicy {
		NONE,
		FULL,
		STATE_BASED
	}
}
