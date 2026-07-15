package com.ikunkk02afk.hookandreel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HookReelConfigManagerTest {
	@TempDir
	Path tempDir;

	@Test
	void missingFileCreatesDefaults() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");

		HookReelConfigManager.load(path);

		assertTrue(Files.isRegularFile(path));
		assertEquals(1.5D, HookReelConfigManager.get().luckyBonusPerLevel);
		assertTrue(Files.readString(path).contains("\"grappleCooldownSeconds\": 10"));
	}

	@Test
	void validFileLoadsAndValidatesValues() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");
		Files.writeString(path, """
			{
			  "luckyEnchantmentEnabled": false,
			  "luckyBonusPerLevel": 3.25,
			  "allowStackWithLuckOfTheSea": false,
			  "grappleCooldownSeconds": 500,
			  "maxChargeTimeSeconds": 2.5,
			  "allowPullBlockEntities": true,
			  "maximumBlockHardness": 5000.0,
			  "blockPullSpeedMultiplier": -4.0,
			  "maxBlockPullDurationSeconds": 90.0,
			  "blockPullStopDistance": 0.1,
			  "blockPullDurabilityCost": 100,
			  "swingEnabled": false,
			  "rappelEnabled": true
			}
			""");

		HookReelConfigManager.load(path);
		HookReelConfig config = HookReelConfigManager.get();

		assertEquals(false, config.luckyEnchantmentEnabled);
		assertEquals(3.25D, config.luckyBonusPerLevel);
		assertEquals(false, config.allowStackWithLuckOfTheSea);
		assertEquals(300, config.grappleCooldownSeconds);
		assertEquals(2.5D, config.maxChargeTimeSeconds);
		assertEquals(false, config.allowPullBlockEntities);
		assertEquals(1000.0D, config.maximumBlockHardness);
		assertEquals(0.0D, config.blockPullSpeedMultiplier);
		assertEquals(60.0D, config.maxBlockPullDurationSeconds);
		assertEquals(0.5D, config.blockPullStopDistance);
		assertEquals(64, config.blockPullDurabilityCost);
		assertTrue(Files.readString(path).contains("\"grappleCooldownSeconds\": 300"));
		assertTrue(Files.notExists(path.resolveSibling("hook_and_reel.json.tmp")));
	}

	@Test
	void brokenFileIsBackedUpAndReplacedWithDefaults() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");
		Files.writeString(path, "{ not valid json");

		HookReelConfigManager.load(path);

		assertEquals(1.5D, HookReelConfigManager.get().luckyBonusPerLevel);
		assertTrue(Files.readString(path).contains("\"luckyEnchantmentEnabled\": true"));
		try (Stream<Path> files = Files.list(tempDir)) {
			assertTrue(files.anyMatch(file -> file.getFileName().toString().startsWith(
				"hook_and_reel.json.broken-"
			)));
		}
	}
}
