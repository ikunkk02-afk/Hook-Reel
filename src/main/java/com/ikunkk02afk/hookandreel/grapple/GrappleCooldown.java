package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GrappleCooldown {
	public static final int CANCEL_COOLDOWN_TICKS = 20;

	private GrappleCooldown() {
	}

	public static long remainingTicks(ItemStack stack, Level level, int maximumExpectedTicks) {
		long until = stack.getOrDefault(ModDataComponents.GRAPPLE_COOLDOWN_UNTIL, 0L);
		return calculateRemaining(until, level.getGameTime(), maximumExpectedTicks);
	}

	public static long calculateRemaining(long until, long gameTime, int maximumExpectedTicks) {
		long remaining = until - gameTime;
		if (remaining <= 0L) {
			return 0L;
		}
		return Math.min(remaining, Math.max(0, maximumExpectedTicks));
	}

	public static long sanitizeAndGetRemaining(ItemStack stack, Level level, int maximumExpectedTicks) {
		long maximum = Math.max(0, maximumExpectedTicks);
		long until = stack.getOrDefault(ModDataComponents.GRAPPLE_COOLDOWN_UNTIL, 0L);
		long remaining = until - level.getGameTime();
		if (remaining <= 0L || maximum == 0L) {
			stack.remove(ModDataComponents.GRAPPLE_COOLDOWN_UNTIL);
			return 0L;
		}
		if (remaining > maximum) {
			set(stack, level, (int) maximum);
			return maximum;
		}
		return remaining;
	}

	public static void set(ItemStack stack, Level level, int ticks) {
		if (stack.isEmpty() || ticks <= 0) {
			stack.remove(ModDataComponents.GRAPPLE_COOLDOWN_UNTIL);
			return;
		}
		stack.set(ModDataComponents.GRAPPLE_COOLDOWN_UNTIL, level.getGameTime() + ticks);
	}
}
