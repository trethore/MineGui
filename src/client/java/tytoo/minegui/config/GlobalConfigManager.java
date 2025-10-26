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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class GlobalConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_NAMESPACE = MineGuiCore.ID;
    private static final Map<String, ConfigState> CONTEXTS = new HashMap<>();
    private static String defaultNamespace = DEFAULT_NAMESPACE;
    private static boolean autoLoadEnabled = true;
    private static boolean configIgnored;

    private GlobalConfigManager() {
    }

    public static synchronized void configureDefaultNamespace(String namespace) {
        defaultNamespace = sanitizeNamespace(namespace);
    }

    public static synchronized boolean isAutoLoadEnabled() {
        if (configIgnored) {
            return false;
        }
        return autoLoadEnabled;
    }

    public static synchronized void setAutoLoadEnabled(boolean enabled) {
        if (configIgnored) {
            autoLoadEnabled = false;
            return;
        }
        autoLoadEnabled = enabled;
    }

    public static synchronized boolean isConfigIgnored() {
        return configIgnored;
    }

    public static synchronized void setConfigIgnored(boolean ignored) {
        configIgnored = ignored;
        if (ignored) {
            autoLoadEnabled = false;
        }
    }

    public static synchronized void ensureContext(String namespace) {
        context(namespace);
    }

    public static synchronized GlobalConfig getConfig() {
        return getConfig(defaultNamespace);
    }

    public static synchronized GlobalConfig getConfig(String namespace) {
        ConfigState state = context(namespace);
        if (!configIgnored && autoLoadEnabled && !state.loaded) {
            load(namespace);
        }
        return state.config;
    }

    public static synchronized void load() {
        load(defaultNamespace);
    }

    public static synchronized void load(String namespace) {
        ConfigState state = context(namespace);
        if (configIgnored) {
            state.loaded = true;
            state.activeConfigPath = state.defaultConfigFile;
            state.activeViewSavesPath = state.defaultViewSavesDir;
            return;
        }
        if (state.loaded) {
            return;
        }
        ensureDirectory(state.defaultConfigFile.getParent());
        GlobalConfig baseConfig = readConfig(state.defaultConfigFile);
        state.config = Objects.requireNonNullElseGet(baseConfig, GlobalConfig::new);
        ensureViewPath(state);
        state.activeConfigPath = resolveConfigPath(state.config.getConfigPath(), state);
        GlobalConfig overrideConfig = readConfig(state.activeConfigPath);
        if (overrideConfig != null) {
            state.config = overrideConfig;
            ensureViewPath(state);
            state.activeConfigPath = resolveConfigPath(state.config.getConfigPath(), state);
        }
        ensureDirectory(state.activeConfigPath.getParent());
        state.activeViewSavesPath = resolveViewSavesPath(state.config.getViewSavesPath(), state);
        ensureDirectory(state.activeViewSavesPath);
        if (!Files.exists(state.activeConfigPath)) {
            writeConfig(state.activeConfigPath, state.config, state);
        }
        state.loaded = true;
    }

    public static synchronized void save() {
        save(defaultNamespace);
    }

    public static synchronized void save(String namespace) {
        ConfigState state = context(namespace);
        if (configIgnored) {
            state.loaded = true;
            return;
        }
        if (autoLoadEnabled && !state.loaded) {
            load(namespace);
        }
        state.activeConfigPath = resolveConfigPath(state.config.getConfigPath(), state);
        ensureViewPath(state);
        ensureDirectory(state.activeConfigPath.getParent());
        state.activeViewSavesPath = resolveViewSavesPath(state.config.getViewSavesPath(), state);
        ensureDirectory(state.activeViewSavesPath);
        writeConfig(state.activeConfigPath, state.config, state);
        state.loaded = true;
    }

    public static synchronized void reset() {
        reset(defaultNamespace);
    }

    public static synchronized void reset(String namespace) {
        ConfigState state = context(namespace);
        if (configIgnored) {
            state.config = new GlobalConfig();
            state.activeConfigPath = state.defaultConfigFile;
            state.activeViewSavesPath = state.defaultViewSavesDir;
            state.loaded = false;
            return;
        }
        Path currentPath = resolveConfigPath(state.config.getConfigPath(), state);
        deleteIfExists(currentPath);
        if (!Objects.equals(currentPath, state.defaultConfigFile)) {
            deleteIfExists(state.defaultConfigFile);
        }
        state.config = new GlobalConfig();
        ensureViewPath(state);
        state.activeConfigPath = resolveConfigPath(state.config.getConfigPath(), state);
        state.activeViewSavesPath = resolveViewSavesPath(state.config.getViewSavesPath(), state);
        state.loaded = false;
    }

    public static synchronized Path getActiveConfigPath() {
        return getActiveConfigPath(defaultNamespace);
    }

    public static synchronized Path getActiveConfigPath(String namespace) {
        ConfigState state = context(namespace);
        if (configIgnored) {
            return state.defaultConfigFile;
        }
        if (autoLoadEnabled && !state.loaded) {
            load(namespace);
        }
        state.activeConfigPath = resolveConfigPath(state.config.getConfigPath(), state);
        return state.activeConfigPath;
    }

    public static synchronized Path getViewSavesDirectory() {
        return getViewSavesDirectory(defaultNamespace);
    }

    public static synchronized Path getViewSavesDirectory(String namespace) {
        ConfigState state = context(namespace);
        if (configIgnored) {
            return state.defaultViewSavesDir;
        }
        if (autoLoadEnabled && !state.loaded) {
            load(namespace);
        }
        state.activeViewSavesPath = resolveViewSavesPath(state.config.getViewSavesPath(), state);
        return state.activeViewSavesPath;
    }

    private static ConfigState context(String namespace) {
        String sanitized = sanitizeNamespace(namespace);
        return CONTEXTS.computeIfAbsent(sanitized, ConfigState::new);
    }

    private static String sanitizeNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return DEFAULT_NAMESPACE;
        }
        return namespace;
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
            if (parsed.getViewSavesPath() == null || parsed.getViewSavesPath().isBlank()) {
                parsed.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
            }
            if (parsed.getViewStyles() == null) {
                parsed.setViewStyles(new HashMap<>());
            }
            parsed.setGlobalScale(parsed.getGlobalScale());
            return parsed;
        } catch (IOException | JsonParseException e) {
            MineGuiCore.LOGGER.error("Failed to read global config from {}", path, e);
            return null;
        }
    }

    private static Path resolveConfigPath(String configuredPath, ConfigState state) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return state.defaultConfigFile;
        }
        try {
            Path path = Path.of(configuredPath);
            if (!path.isAbsolute()) {
                path = state.baseDirectory.resolve(path);
            }
            return path.normalize();
        } catch (InvalidPathException e) {
            MineGuiCore.LOGGER.warn("Invalid config path '{}', using default", configuredPath);
            return state.defaultConfigFile;
        }
    }

    private static void writeConfig(Path path, GlobalConfig value, ConfigState state) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            value.setConfigPath(path.toString());
            Path resolvedViewPath = resolveViewSavesPath(value.getViewSavesPath(), state);
            value.setViewSavesPath(resolvedViewPath.toString());
            value.setGlobalScale(value.getGlobalScale());
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

    private static Path resolveViewSavesPath(String configuredPath, ConfigState state) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return state.defaultViewSavesDir;
        }
        try {
            Path path = Path.of(configuredPath);
            if (!path.isAbsolute()) {
                path = state.baseDirectory.resolve(path);
            }
            return path.normalize();
        } catch (InvalidPathException e) {
            MineGuiCore.LOGGER.warn("Invalid view saves path '{}', using default", configuredPath);
            return state.defaultViewSavesDir;
        }
    }

    private static void ensureViewPath(ConfigState state) {
        if (state.config.getViewSavesPath() == null || state.config.getViewSavesPath().isBlank()) {
            state.config.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
        }
    }

    private static final class ConfigState {
        private final Path baseDirectory;
        private final Path defaultConfigFile;
        private final Path defaultViewSavesDir;
        private GlobalConfig config;
        private Path activeConfigPath;
        private Path activeViewSavesPath;
        private boolean loaded;

        private ConfigState(String namespace) {
            if (DEFAULT_NAMESPACE.equals(namespace)) {
                this.baseDirectory = MineGuiCore.CONFIG_DIR;
            } else {
                this.baseDirectory = MineGuiCore.CONFIG_DIR.resolve(namespace);
            }
            this.defaultConfigFile = baseDirectory.resolve("global_config.json");
            this.defaultViewSavesDir = baseDirectory.resolve(GlobalConfig.getDefaultViewSavesPath());
            this.config = new GlobalConfig();
            this.activeConfigPath = defaultConfigFile;
            this.activeViewSavesPath = defaultViewSavesDir;
            this.loaded = false;
        }
    }
}
