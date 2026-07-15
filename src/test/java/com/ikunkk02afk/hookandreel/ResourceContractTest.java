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
