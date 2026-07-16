package com.ikunkk02afk.hookandreel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.ikunkk02afk.hookandreel.HookReel;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class HookReelConfigManager {
	public static final String FILE_NAME = "hook_and_reel.json";

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile HookReelConfig current = new HookReelConfig();
	private static Path configPath;

	private HookReelConfigManager() {
	}

	public static synchronized void load() {
		load(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME));
	}

	public static synchronized void load(Path path) {
		configPath = path;
		if (Files.notExists(path)) {
			current = new HookReelConfig();
			write(path, current);
			return;
		}

		try {
			HookReelConfig loaded;
			try (Reader reader = Files.newBufferedReader(path)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				loaded = GSON.fromJson(root, HookReelConfig.class);
				applyLegacyCooldownMigrations(root, loaded);
			}
			if (loaded == null) {
				throw new JsonParseException("Config root must be a JSON object");
			}
			current = loaded.validatedCopy();
			write(path, current);
		} catch (IOException | RuntimeException exception) {
			HookReel.LOGGER.warn("Could not read {}; restoring defaults", path, exception);
			backupBrokenFile(path);
			current = new HookReelConfig();
			write(path, current);
		}
	}

	private static void applyLegacyCooldownMigrations(JsonObject root, HookReelConfig loaded) {
		if (root.has("grapplingHookCooldownSeconds")) {
			loaded.grapplingHookCooldownSeconds = doubleOrDefault(
				root,
				"grapplingHookCooldownSeconds",
				HookReelConfig.DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS
			);
		} else if (root.has("grappleCooldownSeconds")) {
			loaded.grapplingHookCooldownSeconds = doubleOrDefault(
				root,
				"grappleCooldownSeconds",
				HookReelConfig.DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS
			);
		} else if (root.has("hookCooldownSeconds")) {
			loaded.grapplingHookCooldownSeconds = doubleOrDefault(
				root,
				"hookCooldownSeconds",
				HookReelConfig.DEFAULT_GRAPPLING_HOOK_COOLDOWN_SECONDS
			);
		}

		if (root.has("anchorHookCooldownSeconds")) {
			loaded.anchorHookCooldownSeconds = doubleOrDefault(
				root,
				"anchorHookCooldownSeconds",
				HookReelConfig.DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS
			);
		} else if (root.has("swingRecastDelaySeconds")) {
			loaded.anchorHookCooldownSeconds = doubleOrDefault(
				root,
				"swingRecastDelaySeconds",
				HookReelConfig.DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS
			);
		} else if (root.has("hookCooldownSeconds")) {
			loaded.anchorHookCooldownSeconds = doubleOrDefault(
				root,
				"hookCooldownSeconds",
				HookReelConfig.DEFAULT_ANCHOR_HOOK_COOLDOWN_SECONDS
			);
		}
	}

	private static double doubleOrDefault(JsonObject root, String key, double defaultValue) {
		try {
			return root.get(key).getAsDouble();
		} catch (RuntimeException exception) {
			return defaultValue;
		}
	}

	public static HookReelConfig get() {
		return current;
	}

	public static synchronized void save(HookReelConfig config) {
		if (configPath == null) {
			configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		}
		current = config.validatedCopy();
		write(configPath, current);
	}

	private static void backupBrokenFile(Path path) {
		if (Files.notExists(path)) {
			return;
		}
		Path backup = path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis());
		try {
			Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exception) {
			HookReel.LOGGER.warn("Could not preserve broken config as {}", backup, exception);
		}
	}

	private static void write(Path path, HookReelConfig config) {
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(config, writer);
			}
			try {
				Files.move(
					temporary,
					path,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE
				);
			} catch (IOException atomicMoveFailure) {
				try {
					Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException fallbackFailure) {
					atomicMoveFailure.addSuppressed(fallbackFailure);
					throw atomicMoveFailure;
				}
			}
		} catch (IOException exception) {
			HookReel.LOGGER.warn("Could not save Hook & Reel config to {}", path, exception);
			try {
				Files.deleteIfExists(temporary);
			} catch (IOException ignored) {
				// The original save error is the useful failure.
			}
		}
	}
}
