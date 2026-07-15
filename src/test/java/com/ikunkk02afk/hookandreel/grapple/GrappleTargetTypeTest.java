package com.ikunkk02afk.hookandreel.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ikunkk02afk.hookandreel.entity.PulledBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import org.junit.jupiter.api.Test;

class GrappleTargetTypeTest {
	@Test
	void classifiesAllSupportedOwnershipKindsWithoutBooleanStateCombinations() {
		assertEquals(GrappleTargetType.NONE, GrappleTargetType.classify(null));
		assertEquals(GrappleTargetType.NONE, GrappleTargetType.classifyClass(Entity.class));
		assertEquals(GrappleTargetType.ENTITY, GrappleTargetType.classifyClass(Zombie.class));
		assertEquals(GrappleTargetType.ITEM, GrappleTargetType.classifyClass(ItemEntity.class));
		assertEquals(GrappleTargetType.BLOCK, GrappleTargetType.classifyClass(PulledBlockEntity.class));
	}
}
