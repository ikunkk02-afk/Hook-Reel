package com.ikunkk02afk.hookandreel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ResourceContractTest {
	@Test
	void fabricMetadataUsesFinalIdentityAndSafeDependencySides() throws IOException {
		JsonObject metadata = json(Path.of("src/main/resources/fabric.mod.json"));

		assertEquals("hook_and_reel", metadata.get("id").getAsString());
		assertEquals("MIT", metadata.get("license").getAsString());
		assertTrue(metadata.getAsJsonObject("depends").has("cloth-config"));
		assertFalse(metadata.getAsJsonObject("depends").has("modmenu"));
		assertTrue(metadata.getAsJsonObject("suggests").has("modmenu"));
		assertTrue(metadata.getAsJsonObject("entrypoints").has("modmenu"));
	}

	@Test
	void luckyCatchIsAThreeLevelFishingEnchantmentInTheVanillaTableChain() throws IOException {
		JsonObject enchantment = json(Path.of(
			"src/main/resources/data/hook_and_reel/enchantment/lucky_catch.json"
		));
		JsonObject nonTreasure = json(Path.of(
			"src/main/resources/data/minecraft/tags/enchantment/non_treasure.json"
		));

		assertEquals(3, enchantment.get("max_level").getAsInt());
		assertEquals(
			"#minecraft:enchantable/fishing",
			enchantment.get("supported_items").getAsString()
		);
		assertEquals(
			"#minecraft:enchantable/fishing",
			enchantment.get("primary_items").getAsString()
		);
		assertFalse(enchantment.has("exclusive_set"));
		assertTrue(nonTreasure.getAsJsonArray("values").toString().contains(
			"hook_and_reel:lucky_catch"
		));
	}

	@Test
	void grapplingHookIsDataDrivenAndBossFilteringIsExtensible() throws IOException {
		JsonObject enchantment = json(Path.of(
			"src/main/resources/data/hook_and_reel/enchantment/grappling_hook.json"
		));
		JsonObject nonTreasure = json(Path.of(
			"src/main/resources/data/minecraft/tags/enchantment/non_treasure.json"
		));
		JsonObject blacklist = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/grapple_pull_blacklist.json"
		));

		assertEquals(3, enchantment.get("max_level").getAsInt());
		assertEquals("#minecraft:enchantable/fishing", enchantment.get("supported_items").getAsString());
		assertEquals("#minecraft:enchantable/fishing", enchantment.get("primary_items").getAsString());
		assertFalse(enchantment.has("exclusive_set"));
		assertTrue(nonTreasure.getAsJsonArray("values").toString().contains("hook_and_reel:grappling_hook"));
		assertTrue(blacklist.getAsJsonArray("values").toString().contains("minecraft:ender_dragon"));
		assertTrue(blacklist.getAsJsonArray("values").toString().contains("minecraft:wither"));
	}

	@Test
	void anchorHookIsTableAvailableCompatibleAndUsesItsOwnAnchorBlacklist() throws IOException {
		JsonObject enchantment = json(Path.of(
			"src/main/resources/data/hook_and_reel/enchantment/anchor_hook.json"
		));
		JsonObject nonTreasure = json(Path.of(
			"src/main/resources/data/minecraft/tags/enchantment/non_treasure.json"
		));
		JsonObject unhookable = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/block/swing_unhookable.json"
		));

		assertEquals(3, enchantment.get("max_level").getAsInt());
		assertEquals("#minecraft:enchantable/fishing", enchantment.get("supported_items").getAsString());
		assertEquals("#minecraft:enchantable/fishing", enchantment.get("primary_items").getAsString());
		assertFalse(enchantment.has("exclusive_set"));
		assertTrue(nonTreasure.getAsJsonArray("values").toString().contains("hook_and_reel:anchor_hook"));
		String values = unhookable.getAsJsonArray("values").toString();
		assertTrue(values.contains("minecraft:barrier"));
		assertTrue(values.contains("minecraft:water"));
		assertTrue(values.contains("minecraft:nether_portal"));
		assertFalse(values.contains("minecraft:bedrock"));
	}

	@Test
	void blockPullingSafetyTagsCoverTechnicalAndMultiblockFamilies() throws IOException {
		JsonObject immovable = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/block/grapple_immovable.json"
		));
		JsonObject multiblock = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/block/grapple_multiblock_unsafe.json"
		));

		String fixed = immovable.getAsJsonArray("values").toString();
		assertTrue(fixed.contains("minecraft:bedrock"));
		assertTrue(fixed.contains("minecraft:barrier"));
		assertTrue(fixed.contains("minecraft:command_block"));
		assertTrue(fixed.contains("minecraft:nether_portal"));
		assertTrue(fixed.contains("minecraft:reinforced_deepslate"));

		String unsafe = multiblock.getAsJsonArray("values").toString();
		assertTrue(unsafe.contains("#minecraft:doors"));
		assertTrue(unsafe.contains("#minecraft:beds"));
		assertTrue(unsafe.contains("minecraft:big_dripleaf"));
		assertTrue(unsafe.contains("minecraft:kelp_plant"));
		assertTrue(unsafe.contains("minecraft:piston"));
	}

	@Test
	void fishingEntityTagsProvideDefaultsAndBlacklistPriorityData() throws IOException {
		JsonObject aggregate = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/fishable_entities.json"
		));
		JsonObject aquatic = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/fishable_aquatic_entities.json"
		));
		JsonObject animals = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/fishable_land_animals.json"
		));
		JsonObject monsters = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/fishable_land_monsters.json"
		));
		JsonObject nether = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/fishable_nether_entities.json"
		));
		JsonObject bosses = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/fishable_boss_entities.json"
		));
		JsonObject unfishable = json(Path.of(
			"src/main/resources/data/hook_and_reel/tags/entity_type/unfishable_entities.json"
		));

		assertContainsAll(aggregate,
			"#hook_and_reel:fishable_aquatic_entities",
			"#hook_and_reel:fishable_land_animals",
			"#hook_and_reel:fishable_land_monsters",
			"#hook_and_reel:fishable_nether_entities",
			"#hook_and_reel:fishable_boss_entities"
		);
		assertContainsAll(aquatic,
			"minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish",
			"minecraft:pufferfish", "minecraft:squid", "minecraft:glow_squid",
			"minecraft:tadpole", "minecraft:axolotl"
		);
		assertFalse(aquatic.getAsJsonArray("values").toString().contains("minecraft:dolphin"));
		assertContainsAll(animals,
			"minecraft:cow", "minecraft:sheep", "minecraft:pig", "minecraft:chicken",
			"minecraft:rabbit", "minecraft:goat", "minecraft:horse", "minecraft:donkey",
			"minecraft:mule", "minecraft:llama", "minecraft:fox", "minecraft:wolf",
			"minecraft:cat", "minecraft:ocelot", "minecraft:panda", "minecraft:turtle",
			"minecraft:frog", "minecraft:armadillo", "minecraft:camel", "minecraft:sniffer"
		);
		assertContainsAll(monsters,
			"minecraft:zombie", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider",
			"minecraft:cave_spider", "minecraft:husk", "minecraft:stray", "minecraft:drowned",
			"minecraft:enderman", "minecraft:witch", "minecraft:slime", "minecraft:pillager",
			"minecraft:vindicator", "minecraft:evoker", "minecraft:ravager", "minecraft:phantom",
			"minecraft:silverfish", "minecraft:endermite"
		);
		assertContainsAll(nether,
			"minecraft:blaze", "minecraft:ghast", "minecraft:magma_cube",
			"minecraft:wither_skeleton", "minecraft:zombified_piglin", "minecraft:piglin",
			"minecraft:piglin_brute", "minecraft:hoglin", "minecraft:zoglin", "minecraft:strider"
		);
		assertContainsAll(bosses,
			"minecraft:wither", "minecraft:elder_guardian", "minecraft:warden", "minecraft:ender_dragon"
		);

		String denied = unfishable.getAsJsonArray("values").toString();
		assertContainsAll(unfishable,
			"minecraft:player", "minecraft:armor_stand", "minecraft:item",
			"minecraft:experience_orb", "minecraft:tnt", "minecraft:falling_block",
			"minecraft:boat", "minecraft:chest_boat", "minecraft:minecart",
			"minecraft:interaction", "minecraft:marker", "minecraft:area_effect_cloud",
			"minecraft:block_display", "minecraft:item_display", "minecraft:text_display",
			"minecraft:lightning_bolt", "minecraft:end_crystal", "minecraft:fishing_bobber",
			"minecraft:arrow", "minecraft:trident", "minecraft:fireball",
			"minecraft:wither_skull", "minecraft:wind_charge", "hook_and_reel:pulled_block"
		);
		assertFalse(denied.contains("\"minecraft:wither\""));
		assertFalse(denied.contains("\"minecraft:warden\""));
		assertFalse(denied.contains("\"minecraft:elder_guardian\""));
		assertFalse(denied.contains("\"minecraft:ghast\""));
	}

	@Test
	void translationsAndClientIsolationArePresent() throws IOException {
		JsonObject english = json(Path.of(
			"src/main/resources/assets/hook_and_reel/lang/en_us.json"
		));
		JsonObject chinese = json(Path.of(
			"src/main/resources/assets/hook_and_reel/lang/zh_cn.json"
		));

		assertEquals("Lucky Catch", english.get("enchantment.hook_and_reel.lucky_catch").getAsString());
		assertEquals("幸运", chinese.get("enchantment.hook_and_reel.lucky_catch").getAsString());
		assertEquals("Grappling Hook", english.get("enchantment.hook_and_reel.grappling_hook").getAsString());
		assertEquals("钩锁", chinese.get("enchantment.hook_and_reel.grappling_hook").getAsString());
		assertEquals("Pulled Block", english.get("entity.hook_and_reel.pulled_block").getAsString());
		assertEquals("被拖拽的方块", chinese.get("entity.hook_and_reel.pulled_block").getAsString());
		assertTrue(english.has("option.hook_and_reel.allow_pull_block_entities.tooltip"));
		assertTrue(chinese.has("option.hook_and_reel.allow_pull_block_entities.tooltip"));
		assertEquals("Anchor Hook", english.get("enchantment.hook_and_reel.anchor_hook").getAsString());
		assertTrue(chinese.has("enchantment.hook_and_reel.anchor_hook"));
		assertTrue(english.has("key.hook_and_reel.switch_mode"));
		assertTrue(chinese.has("key.hook_and_reel.switch_mode"));
		assertTrue(english.has("message.hook_and_reel.mode_swing"));
		assertTrue(chinese.has("hud.hook_and_reel.rope_length"));
		assertTrue(english.has("hud.hook_and_reel.wall_cling_timer"));
		assertTrue(chinese.has("hud.hook_and_reel.wall_cling_timer"));
		assertTrue(english.has("option.hook_and_reel.reel_acceleration"));
		assertTrue(english.has("category.hook_and_reel.anchor_hook_movement"));
		assertTrue(chinese.has("category.hook_and_reel.anchor_hook_movement"));
		assertTrue(english.has("option.hook_and_reel.reeling_lateral_control_enabled"));
		assertTrue(chinese.has("option.hook_and_reel.reeling_obstacle_bypass_multiplier"));
		assertTrue(chinese.has("option.hook_and_reel.wall_cling_duration_seconds"));
		assertTrue(english.has("option.hook_and_reel.wall_climb_down_speed"));
		assertTrue(chinese.has("option.hook_and_reel.wall_climb_down_speed"));
		assertTrue(english.has("message.hook_and_reel.anchor_cooldown"));
		assertTrue(chinese.has("message.hook_and_reel.anchor_cooldown"));
		assertTrue(english.has("category.hook_and_reel.ability_cooldowns"));
		assertTrue(chinese.has("category.hook_and_reel.lucky_three"));
		assertTrue(english.has("category.hook_and_reel.fishing_entities"));
		assertTrue(chinese.has("option.hook_and_reel.allow_fishing_entities.tooltip"));
		assertTrue(english.has("subcategory.hook_and_reel.fishing_entity_general"));
		assertTrue(chinese.has("subcategory.hook_and_reel.allowed_fishing_categories"));
		assertTrue(english.has("subcategory.hook_and_reel.fishing_category_weights"));
		assertTrue(english.has("option.hook_and_reel.allow_aquatic_entities.tooltip"));
		assertTrue(chinese.has("option.hook_and_reel.allow_land_animals.tooltip"));
		assertTrue(english.has("option.hook_and_reel.allow_land_monsters.tooltip"));
		assertTrue(chinese.has("option.hook_and_reel.allow_nether_entities.tooltip"));
		assertTrue(english.has("option.hook_and_reel.nether_entities_only_in_nether.tooltip"));
		assertTrue(chinese.has("option.hook_and_reel.allow_boss_entities.tooltip"));
		assertTrue(english.has("option.hook_and_reel.allow_ender_dragon_fishing.tooltip"));
		assertTrue(chinese.has("option.hook_and_reel.boss_entity_category_weight.tooltip"));
		assertTrue(english.has("option.hook_and_reel.grappling_hook_cooldown_seconds.tooltip"));
		assertTrue(chinese.has("option.hook_and_reel.anchor_hook_cooldown_seconds.tooltip"));
		assertEquals(
			"Grappling Hook Cooldown",
			english.get("option.hook_and_reel.grappling_hook_cooldown_seconds").getAsString()
		);
		assertEquals(
			"Anchor Hook Cooldown",
			english.get("option.hook_and_reel.anchor_hook_cooldown_seconds").getAsString()
		);
		assertEquals(
			"钩锁冷却时间",
			chinese.get("option.hook_and_reel.grappling_hook_cooldown_seconds").getAsString()
		);
		assertEquals(
			"挂钩冷却时间",
			chinese.get("option.hook_and_reel.anchor_hook_cooldown_seconds").getAsString()
		);
		assertEquals("Grappling Hook Cooldown: %ss", english.get("message.hook_and_reel.grapple_cooldown").getAsString());
		assertEquals("Anchor Hook Cooldown: %ss", english.get("message.hook_and_reel.anchor_cooldown").getAsString());
		assertTrue(english.has("option.hook_and_reel.lucky_three_instant_catch_delay_seconds.tooltip"));
		assertFalse(english.has("option.hook_and_reel.wall_slide_speed"));
		assertFalse(chinese.has("option.hook_and_reel.wall_slide_speed"));

		try (Stream<Path> files = Files.walk(Path.of("src/main"))) {
			assertFalse(files.filter(Files::isRegularFile).anyMatch(ResourceContractTest::containsClientImport));
		}
	}

	@Test
	void obsoleteTemplateNamespaceIsGone() throws IOException {
		try (Stream<Path> files = Files.walk(Path.of("src/main"))) {
			assertFalse(files.filter(Files::isRegularFile).anyMatch(ResourceContractTest::containsOldNamespace));
		}
		try (Stream<Path> files = Files.walk(Path.of("src/client"))) {
			assertFalse(files.filter(Files::isRegularFile).anyMatch(ResourceContractTest::containsOldNamespace));
		}
	}

	private static JsonObject json(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}

	private static void assertContainsAll(JsonObject tag, String... expectedValues) {
		String values = tag.getAsJsonArray("values").toString();
		for (String expected : expectedValues) {
			assertTrue(values.contains(expected), () -> "Missing tag value " + expected);
		}
	}

	private static boolean containsClientImport(Path path) {
		try {
			String content = Files.readString(path);
			return content.contains("import net.minecraft.client")
				|| content.contains("import com.terraformersmc.modmenu");
		} catch (IOException exception) {
			return false;
		}
	}

	private static boolean containsOldNamespace(Path path) {
		try {
			return Files.readString(path).contains("hook-" + "-reel");
		} catch (IOException exception) {
			return false;
		}
	}
}
