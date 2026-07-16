package com.ikunkk02afk.hookandreel.component;

import com.ikunkk02afk.hookandreel.grapple.AnchorHookLogic;
import com.ikunkk02afk.hookandreel.grapple.GrappleEnchantmentLogic;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GrappleModeComponent {
	private GrappleModeComponent() {
	}

	public static GrappleMode get(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.GRAPPLE_MODE, GrappleMode.PULL);
	}

	public static void set(ItemStack stack, GrappleMode mode) {
		stack.set(ModDataComponents.GRAPPLE_MODE, mode);
	}

	public static GrappleMode getEffective(Level level, ItemStack stack) {
		return resolve(
			get(stack),
			GrappleEnchantmentLogic.getLevel(level, stack) > 0,
			AnchorHookLogic.getLevel(level, stack) > 0
		);
	}

	public static GrappleMode getAndRepair(Level level, ItemStack stack) {
		boolean hasPull = GrappleEnchantmentLogic.getLevel(level, stack) > 0;
		boolean hasSwing = AnchorHookLogic.getLevel(level, stack) > 0;
		if (!hasPull && !hasSwing) {
			if (stack.has(ModDataComponents.GRAPPLE_MODE)) {
				stack.remove(ModDataComponents.GRAPPLE_MODE);
			}
			return GrappleMode.PULL;
		}
		GrappleMode stored = stack.get(ModDataComponents.GRAPPLE_MODE);
		GrappleMode resolved = resolve(stored, hasPull, hasSwing);
		if (stored != resolved) {
			set(stack, resolved);
		}
		return resolved;
	}

	public static GrappleMode resolve(GrappleMode stored, boolean hasPull, boolean hasSwing) {
		if (!hasPull && !hasSwing) {
			return GrappleMode.PULL;
		}
		if (hasSwing && !hasPull) {
			return GrappleMode.SWING;
		}
		if (hasPull && !hasSwing) {
			return GrappleMode.PULL;
		}
		return stored == null ? GrappleMode.PULL : stored;
	}
}
