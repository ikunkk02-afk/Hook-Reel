package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.component.ModDataComponents;
import java.util.Locale;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HookAbilityCooldownManager {
	public static final int PULL_CANCEL_COOLDOWN_TICKS = 20;

	private HookAbilityCooldownManager() {
	}

	public static int secondsToTicks(double seconds) {
		if (!Double.isFinite(seconds) || seconds <= 0.0D) {
			return 0;
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.round(seconds * 20.0D));
	}

	public static String formatRemainingSeconds(long remainingTicks) {
		return String.format(Locale.ROOT, "%.1f", Math.max(0L, remainingTicks) / 20.0D);
	}

	public static long remainingTicks(
		ItemStack stack,
		Level level,
		HookAbilityCooldown ability,
		int maximumExpectedTicks
	) {
		long until = stack.getOrDefault(component(ability), 0L);
		return calculateRemaining(until, level.getGameTime(), maximumExpectedTicks);
	}

	public static long calculateRemaining(long until, long gameTime, int maximumExpectedTicks) {
		long remaining = until - gameTime;
		if (remaining <= 0L) {
			return 0L;
		}
		return Math.min(remaining, Math.max(0, maximumExpectedTicks));
	}

	public static long sanitizeAndGetRemaining(
		ItemStack stack,
		Level level,
		HookAbilityCooldown ability,
		int maximumExpectedTicks
	) {
		long maximum = Math.max(0, maximumExpectedTicks);
		DataComponentType<Long> component = component(ability);
		long until = stack.getOrDefault(component, 0L);
		long remaining = until - level.getGameTime();
		if (remaining <= 0L || maximum == 0L) {
			stack.remove(component);
			return 0L;
		}
		if (remaining > maximum) {
			set(stack, level, ability, (int) maximum);
			return maximum;
		}
		return remaining;
	}

	public static void set(ItemStack stack, Level level, HookAbilityCooldown ability, int ticks) {
		if (stack.isEmpty()) {
			return;
		}
		DataComponentType<Long> component = component(ability);
		if (ticks <= 0) {
			stack.remove(component);
			return;
		}
		stack.set(component, level.getGameTime() + ticks);
	}

	private static DataComponentType<Long> component(HookAbilityCooldown ability) {
		return switch (ability) {
			case PULL -> ModDataComponents.PULL_COOLDOWN_UNTIL;
			case ANCHOR -> ModDataComponents.ANCHOR_COOLDOWN_UNTIL;
		};
	}
}
