package com.ikunkk02afk.hookandreel.grapple;

import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public enum GrappleTargetType {
	NONE,
	ENTITY,
	ITEM,
	BLOCK;

	public static GrappleTargetType classify(Entity target) {
		return target == null ? NONE : classifyClass(target.getClass());
	}

	public static GrappleTargetType classifyClass(Class<? extends Entity> targetClass) {
		if (PulledBlockEntity.class.isAssignableFrom(targetClass)) {
			return BLOCK;
		}
		if (ItemEntity.class.isAssignableFrom(targetClass)) {
			return ITEM;
		}
		if (LivingEntity.class.isAssignableFrom(targetClass)) {
			return ENTITY;
		}
		return NONE;
	}
}
