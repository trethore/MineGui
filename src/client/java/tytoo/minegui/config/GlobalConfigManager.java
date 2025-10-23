package tytoo.minegui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import tytoo.minegui.MineGuiCore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class GlobalConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DEFAULT_CONFIG_FILE = MineGuiCore.CONFIG_DIR.resolve("global_config.json");
    private static final Path DEFAULT_VIEW_SAVES_DIR = MineGuiCore.CONFIG_DIR.resolve(GlobalConfig.getDefaultViewSavesPath());

    private static GlobalConfig config = new GlobalConfig();
    private static Path activeConfigPath = DEFAULT_CONFIG_FILE;
    private static Path activeViewSavesPath = DEFAULT_VIEW_SAVES_DIR;
    private static boolean loaded;

    private GlobalConfigManager() {
    }

    public static synchronized GlobalConfig getConfig() {
        if (!loaded) {
            load();
        }
        return config;
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        ensureDirectory(DEFAULT_CONFIG_FILE.getParent());
        GlobalConfig baseConfig = readConfig(DEFAULT_CONFIG_FILE);
        config = Objects.requireNonNullElseGet(baseConfig, GlobalConfig::new);
        ensureViewPath(config);
        activeConfigPath = resolveConfigPath(config.getConfigPath());
        GlobalConfig overrideConfig = readConfig(activeConfigPath);
        if (overrideConfig != null) {
            config = overrideConfig;
            activeConfigPath = resolveConfigPath(config.getConfigPath());
            ensureViewPath(config);
        }
        ensureDirectory(activeConfigPath.getParent());
        activeViewSavesPath = resolveViewSavesPath(config.getViewSavesPath());
        ensureDirectory(activeViewSavesPath);
        if (!Files.exists(activeConfigPath)) {
            writeConfig(activeConfigPath, config);
        }
        loaded = true;
    }

    public static synchronized void save() {
        if (!loaded) {
            load();
        }
        activeConfigPath = resolveConfigPath(config.getConfigPath());
        ensureViewPath(config);
        ensureDirectory(activeConfigPath.getParent());
        activeViewSavesPath = resolveViewSavesPath(config.getViewSavesPath());
        ensureDirectory(activeViewSavesPath);
        writeConfig(activeConfigPath, config);
    }

    public static synchronized void reset() {
        if (!loaded) {
            load();
        }
        Path currentPath = resolveConfigPath(config.getConfigPath());
        deleteIfExists(currentPath);
        if (!Objects.equals(currentPath, DEFAULT_CONFIG_FILE)) {
            deleteIfExists(DEFAULT_CONFIG_FILE);
        }
        config = new GlobalConfig();
        activeConfigPath = resolveConfigPath(config.getConfigPath());
        ensureViewPath(config);
        activeViewSavesPath = resolveViewSavesPath(config.getViewSavesPath());
        loaded = true;
    }

    public static Path getActiveConfigPath() {
        return activeConfigPath;
    }

    public static Path getViewSavesDirectory() {
        if (!loaded) {
            load();
        }
        return activeViewSavesPath;
    }

    private static void ensureDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to create config directory {}", directory, e);
        }
    }

    private static GlobalConfig readConfig(Path path) {
        if (path == null) {
            return null;
        }
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GlobalConfig parsed = GSON.fromJson(reader, GlobalConfig.class);
            if (parsed == null) {
                return null;
            }
            if (parsed.getConfigPath() == null || parsed.getConfigPath().isBlank()) {
                parsed.setConfigPath(path.toString());
            }
            ensureViewPath(parsed);
            return parsed;
        } catch (IOException | JsonParseException e) {
            MineGuiCore.LOGGER.error("Failed to read global config from {}", path, e);
            return null;
        }
    }

    private static Path resolveConfigPath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return DEFAULT_CONFIG_FILE;
        }
        try {
            Path path = Path.of(configuredPath);
            if (!path.isAbsolute()) {
                path = MineGuiCore.CONFIG_DIR.resolve(path);
            }
            return path.normalize();
        } catch (InvalidPathException e) {
            MineGuiCore.LOGGER.warn("Invalid config path '{}', using default", configuredPath);
            return DEFAULT_CONFIG_FILE;
        }
    }

    private static void writeConfig(Path path, GlobalConfig value) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            value.setConfigPath(path.toString());
            Path resolvedViewPath = resolveViewSavesPath(value.getViewSavesPath());
            value.setViewSavesPath(resolvedViewPath.toString());
            GSON.toJson(value, writer);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to write global config to {}", path, e);
        }
    }

    private static void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to delete config file {}", path, e);
        }
    }

    private static Path resolveViewSavesPath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return DEFAULT_VIEW_SAVES_DIR;
        }
        try {
            Path path = Path.of(configuredPath);
            if (!path.isAbsolute()) {
                path = MineGuiCore.CONFIG_DIR.resolve(path);
            }
            return path.normalize();
        } catch (InvalidPathException e) {
            MineGuiCore.LOGGER.warn("Invalid view saves path '{}', using default", configuredPath);
            return DEFAULT_VIEW_SAVES_DIR;
        }
    }

    private static void ensureViewPath(GlobalConfig value) {
        if (value.getViewSavesPath() == null || value.getViewSavesPath().isBlank()) {
            value.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
        }
    }
}
