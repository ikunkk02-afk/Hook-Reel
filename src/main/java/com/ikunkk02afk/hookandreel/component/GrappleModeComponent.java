package com.ikunkk02afk.hookandreel.component;

import net.minecraft.world.item.ItemStack;

public final class GrappleModeComponent {
	private GrappleModeComponent() {
	}

	public static GrappleMode get(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.GRAPPLE_MODE, GrappleMode.PULL);
	}

	public static void set(ItemStack stack, GrappleMode mode) {
		stack.set(ModDataComponents.GRAPPLE_MODE, mode);
	}
}
