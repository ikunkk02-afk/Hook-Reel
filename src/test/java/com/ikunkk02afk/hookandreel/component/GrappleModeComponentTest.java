package com.ikunkk02afk.hookandreel.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GrappleModeComponentTest {
	@Test
	void legacyAndSingleEnchantmentRodsResolveToTheirOnlyLegalMode() {
		assertEquals(GrappleMode.PULL, GrappleModeComponent.resolve(GrappleMode.SWING, false, false));
		assertEquals(GrappleMode.PULL, GrappleModeComponent.resolve(null, true, true));
		assertEquals(GrappleMode.PULL, GrappleModeComponent.resolve(GrappleMode.SWING, true, false));
		assertEquals(GrappleMode.SWING, GrappleModeComponent.resolve(GrappleMode.PULL, false, true));
	}

	@Test
	void dualEnchantmentRodsPreserveTheirOwnStoredMode() {
		assertEquals(GrappleMode.PULL, GrappleModeComponent.resolve(GrappleMode.PULL, true, true));
		assertEquals(GrappleMode.SWING, GrappleModeComponent.resolve(GrappleMode.SWING, true, true));
	}
}
