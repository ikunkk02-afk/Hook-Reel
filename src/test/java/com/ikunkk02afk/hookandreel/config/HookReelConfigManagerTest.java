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
		assertTrue(Files.readString(path).contains("\"grapplingHookCooldownSeconds\": 10.0"));
		assertTrue(Files.readString(path).contains("\"anchorHookCooldownSeconds\": 1.5"));
		assertTrue(Files.readString(path).contains("\"allowAquaticEntities\": true"));
		assertTrue(Files.readString(path).contains("\"bossEntityCategoryWeight\": 0.5"));
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
			  "swingRecastDelaySeconds": 0.4,
			  "luckyThreeInstantCatchDelaySeconds": 0.1,
			  "fishingEntityBaseChance": 2.0,
			  "allowBossEntities": true,
			  "allowEnderDragonFishing": true,
			  "bossEntityCategoryWeight": 2001.0,
			  "maxChargeTimeSeconds": 2.5,
			  "allowPullBlockEntities": true,
			  "maximumBlockHardness": 5000.0,
			  "blockPullSpeedMultiplier": -4.0,
			  "maxBlockPullDurationSeconds": 90.0,
			  "blockPullStopDistance": 0.1,
			  "blockPullDurabilityCost": 100,
			  "anchorHookEnabled": false,
			  "anchorArrivalDistance": 7.5,
			  "showClimbTimerHud": false,
			  "wallSlideSpeed": -0.03,
			  "rappelSpeed": 22.0,
			  "rappelEnabled": true
			}
			""");

		HookReelConfigManager.load(path);
		HookReelConfig config = HookReelConfigManager.get();

		assertEquals(false, config.luckyEnchantmentEnabled);
		assertEquals(3.25D, config.luckyBonusPerLevel);
		assertEquals(false, config.allowStackWithLuckOfTheSea);
		assertEquals(120.0D, config.grapplingHookCooldownSeconds);
		assertEquals(0.4D, config.anchorHookCooldownSeconds);
		assertEquals(0.4D, config.anchorHookFailedCastDelaySeconds);
		assertEquals(0.25D, config.luckyThreeInstantCatchDelaySeconds);
		assertEquals(1.0D, config.fishingEntityBaseChance);
		assertEquals(true, config.allowBossEntities);
		assertEquals(true, config.allowEnderDragonFishing);
		assertEquals(1000.0D, config.bossEntityCategoryWeight);
		assertEquals(2.5D, config.maxChargeTimeSeconds);
		assertEquals(false, config.allowPullBlockEntities);
		assertEquals(1000.0D, config.maximumBlockHardness);
		assertEquals(0.0D, config.blockPullSpeedMultiplier);
		assertEquals(60.0D, config.maxBlockPullDurationSeconds);
		assertEquals(0.5D, config.blockPullStopDistance);
		assertEquals(64, config.blockPullDurabilityCost);
		assertEquals(false, config.anchorHookEnabled);
		assertEquals(false, config.showWallClingTimerHud);
		assertEquals(0.75D, config.reelArrivalDistance);
		assertEquals(0.15D, config.wallClimbDownSpeed);
		assertEquals(20.0D, config.rappelSpeed);
		String rewritten = Files.readString(path);
		assertTrue(rewritten.contains("\"grapplingHookCooldownSeconds\": 120.0"));
		assertTrue(rewritten.contains("\"anchorHookCooldownSeconds\": 0.4"));
		assertTrue(rewritten.contains("\"anchorHookFailedCastDelaySeconds\": 0.4"));
		assertTrue(!rewritten.contains("\"grappleCooldownSeconds\""));
		assertTrue(!rewritten.contains("\"swingRecastDelaySeconds\""));
		assertTrue(rewritten.contains("\"showWallClingTimerHud\": false"));
		assertTrue(rewritten.contains("\"wallClimbDownSpeed\": 0.15"));
		assertTrue(!rewritten.contains("anchorArrivalDistance"));
		assertTrue(!rewritten.contains("wallSlideSpeed"));
		assertTrue(rewritten.contains("\"bossEntityCategoryWeight\": 1000.0"));
		assertTrue(Files.notExists(path.resolveSibling("hook_and_reel.json.tmp")));
	}

	@Test
	void newCooldownFieldsOverrideLegacyAliasesAndMissingFieldsUseDefaults() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");
		Files.writeString(path, """
			{
			  "grapplingHookCooldownSeconds": 2.0,
			  "grappleCooldownSeconds": 70.0,
			  "anchorHookCooldownSeconds": 0.5,
			  "swingRecastDelaySeconds": 8.0,
			  "hookCooldownSeconds": 25.0
			}
			""");

		HookReelConfigManager.load(path);

		assertEquals(2.0D, HookReelConfigManager.get().grapplingHookCooldownSeconds);
		assertEquals(0.5D, HookReelConfigManager.get().anchorHookCooldownSeconds);

		Files.writeString(path, "{}");
		HookReelConfigManager.load(path);

		assertEquals(10.0D, HookReelConfigManager.get().grapplingHookCooldownSeconds);
		assertEquals(1.5D, HookReelConfigManager.get().anchorHookCooldownSeconds);
	}

	@Test
	void saveMakesCategoryChangesVisibleImmediately() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");
		HookReelConfigManager.load(path);
		HookReelConfig edited = HookReelConfigManager.get().copy();
		edited.allowAquaticEntities = false;
		edited.allowLandAnimals = true;
		edited.landAnimalCategoryWeight = 321.0D;

		HookReelConfigManager.save(edited);

		assertEquals(false, HookReelConfigManager.get().allowAquaticEntities);
		assertEquals(true, HookReelConfigManager.get().allowLandAnimals);
		assertEquals(321.0D, HookReelConfigManager.get().landAnimalCategoryWeight);
		assertTrue(Files.readString(path).contains("\"landAnimalCategoryWeight\": 321.0"));
	}

	@Test
	void saveMakesCooldownChangesVisibleToTheNextServerAction() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");
		HookReelConfigManager.load(path);
		HookReelConfig edited = HookReelConfigManager.get().copy();
		edited.grapplingHookCooldownSeconds = 2.0D;
		edited.anchorHookCooldownSeconds = 0.5D;

		HookReelConfigManager.save(edited);

		assertEquals(2.0D, HookReelConfigManager.get().grapplingHookCooldownSeconds);
		assertEquals(0.5D, HookReelConfigManager.get().anchorHookCooldownSeconds);
		String saved = Files.readString(path);
		assertTrue(saved.contains("\"grapplingHookCooldownSeconds\": 2.0"));
		assertTrue(saved.contains("\"anchorHookCooldownSeconds\": 0.5"));
	}

	@Test
	void saveMakesReelingLateralChangesVisibleToTheNextUse() throws IOException {
		Path path = tempDir.resolve("hook_and_reel.json");
		HookReelConfigManager.load(path);
		HookReelConfig edited = HookReelConfigManager.get().copy();
		edited.reelingLateralControlEnabled = false;
		edited.reelingLateralControlStrength = 0.22D;
		edited.maximumReelingLateralSpeed = 0.70D;

		HookReelConfigManager.save(edited);

		assertEquals(false, HookReelConfigManager.get().reelingLateralControlEnabled);
		assertEquals(0.22D, HookReelConfigManager.get().reelingLateralControlStrength);
		assertEquals(0.70D, HookReelConfigManager.get().maximumReelingLateralSpeed);
		String saved = Files.readString(path);
		assertTrue(saved.contains("\"reelingLateralControlEnabled\": false"));
		assertTrue(saved.contains("\"reelingLateralControlStrength\": 0.22"));
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
