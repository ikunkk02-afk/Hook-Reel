package com.ikunkk02afk.hookandreel.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ikunkk02afk.hookandreel.config.HookReelConfig;
import org.junit.jupiter.api.Test;

class LuckyCatchLogicTest {
	@Test
	void bonusIncreasesLinearlyAcrossAllThreeLevels() {
		HookReelConfig config = new HookReelConfig();

		assertEquals(0.0F, LuckyCatchLogic.calculateExtraLuck(0, false, config));
		assertEquals(1.5F, LuckyCatchLogic.calculateExtraLuck(1, false, config));
		assertEquals(3.0F, LuckyCatchLogic.calculateExtraLuck(2, false, config));
		assertEquals(4.5F, LuckyCatchLogic.calculateExtraLuck(3, false, config));
	}

	@Test
	void configurationCanDisableEffectWithoutRemovingEnchantment() {
		HookReelConfig config = new HookReelConfig();
		config.luckyEnchantmentEnabled = false;

		assertEquals(0.0F, LuckyCatchLogic.calculateExtraLuck(3, false, config));
	}

	@Test
	void disallowedVanillaStackReceivesNoLuckyCatchBonus() {
		HookReelConfig config = new HookReelConfig();
		config.allowStackWithLuckOfTheSea = false;

		assertEquals(0.0F, LuckyCatchLogic.calculateExtraLuck(3, true, config));
		assertEquals(4.5F, LuckyCatchLogic.calculateExtraLuck(3, false, config));
	}
}
