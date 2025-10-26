package tytoo.minegui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
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
    private static final Path CONFIG_ROOT = determineConfigRoot();
    private static final Path NAMESPACE_ROOT = CONFIG_ROOT.resolve(MineGuiCore.ID).normalize();
    private static String defaultNamespace = DEFAULT_NAMESPACE;

    private GlobalConfigManager() {
    }

    public static synchronized void configureDefaultNamespace(String namespace) {
        defaultNamespace = sanitizeNamespace(namespace);
    }

    public static synchronized String getDefaultNamespace() {
        return defaultNamespace;
    }

    public static synchronized boolean isAutoLoadEnabled() {
        return isAutoLoadEnabled(defaultNamespace);
    }

    public static synchronized void setAutoLoadEnabled(boolean enabled) {
        setAutoLoadEnabled(defaultNamespace, enabled);
    }

    public static synchronized boolean isAutoLoadEnabled(String namespace) {
        ConfigState state = context(namespace);
        if (state.configIgnored) {
            return false;
        }
        return state.autoLoadEnabled;
    }

    public static synchronized void setAutoLoadEnabled(String namespace, boolean enabled) {
        ConfigState state = context(namespace);
        if (state.configIgnored) {
            state.autoLoadEnabled = false;
            return;
        }
        state.autoLoadEnabled = enabled;
    }

    public static synchronized boolean isConfigIgnored() {
        return isConfigIgnored(defaultNamespace);
    }

    public static synchronized void setConfigIgnored(boolean ignored) {
        setConfigIgnored(defaultNamespace, ignored);
    }

    public static synchronized boolean isConfigIgnored(String namespace) {
        ConfigState state = context(namespace);
        return state.configIgnored;
    }

    public static synchronized void setConfigIgnored(String namespace, boolean ignored) {
        ConfigState state = context(namespace);
        state.configIgnored = ignored;
        if (ignored) {
            state.autoLoadEnabled = false;
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
        if (!state.configIgnored && state.autoLoadEnabled && !state.loaded) {
            load(namespace);
        }
        return state.config;
    }

    public static synchronized void load() {
        load(defaultNamespace);
    }

    public static synchronized void load(String namespace) {
        ConfigState state = context(namespace);
        if (state.configIgnored) {
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
        if (state.configIgnored) {
            state.loaded = true;
            return;
        }
        if (state.autoLoadEnabled && !state.loaded) {
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
        if (state.configIgnored) {
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
        if (state.configIgnored) {
            return state.defaultConfigFile;
        }
        if (state.autoLoadEnabled && !state.loaded) {
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
        if (state.configIgnored) {
            return state.defaultViewSavesDir;
        }
        if (state.autoLoadEnabled && !state.loaded) {
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

    private static Path determineConfigRoot() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        if (configDir != null) {
            return configDir.toAbsolutePath().normalize();
        }
        return Path.of("config").toAbsolutePath().normalize();
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
            String storedConfigPath = sanitizeStoredPath(parsed.getConfigPath());
            if (storedConfigPath == null) {
                parsed.setConfigPath(relativizeToConfigRoot(path));
            } else {
                parsed.setConfigPath(storedConfigPath);
            }
            String storedViewPath = sanitizeStoredPath(parsed.getViewSavesPath());
            if (storedViewPath == null) {
                parsed.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
            } else {
                parsed.setViewSavesPath(storedViewPath);
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
        Path resolved = resolveWithinConfigRoot(configuredPath, state, state.defaultConfigFile, "config path");
        state.config.setConfigPath(relativizeToConfigRoot(resolved));
        return resolved;
    }

    private static void writeConfig(Path path, GlobalConfig value, ConfigState state) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            String relativeConfigPath = relativizeToConfigRoot(path);
            Path resolvedViewPath = resolveViewSavesPath(value.getViewSavesPath(), state);
            String relativeViewPath = relativizeToConfigRoot(resolvedViewPath);
            value.setConfigPath(relativeConfigPath);
            value.setViewSavesPath(relativeViewPath);
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
        Path resolved = resolveWithinConfigRoot(configuredPath, state, state.defaultViewSavesDir, "view saves path");
        state.config.setViewSavesPath(relativizeToConfigRoot(resolved));
        return resolved;
    }

    private static void ensureViewPath(ConfigState state) {
        if (state.config.getViewSavesPath() == null || state.config.getViewSavesPath().isBlank()) {
            state.config.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
        }
    }

    private static Path resolveWithinConfigRoot(String configuredPath, ConfigState state, Path defaultPath, String label) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return defaultPath;
        }
        String sanitizedInput = sanitizeStoredPath(configuredPath);
        if (sanitizedInput == null) {
            return defaultPath;
        }
        Path candidate;
        try {
            candidate = Path.of(sanitizedInput);
        } catch (InvalidPathException e) {
            MineGuiCore.LOGGER.warn("Invalid {} '{}', using default", label, sanitizedInput);
            return defaultPath;
        }
        Path resolved;
        if (candidate.isAbsolute()) {
            resolved = candidate.normalize();
        } else if (isSimpleName(candidate)) {
            resolved = state.baseDirectory.resolve(candidate).normalize();
        } else {
            resolved = CONFIG_ROOT.resolve(candidate).normalize();
        }
        if (!resolved.startsWith(NAMESPACE_ROOT)) {
            MineGuiCore.LOGGER.warn("{} '{}' resolves outside of the MineGui config directory; using default", label, sanitizedInput);
            return defaultPath;
        }
        return resolved;
    }

    private static boolean isSimpleName(Path path) {
        if (path.getNameCount() != 1) {
            return false;
        }
        String name = path.getFileName().toString();
        return !".".equals(name) && !"..".equals(name);
    }

    private static String relativizeToConfigRoot(Path path) {
        if (path == null) {
            return null;
        }
        Path normalized = path.normalize();
        if (normalized.startsWith(CONFIG_ROOT)) {
            return CONFIG_ROOT.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString().replace('\\', '/');
    }

    private static String sanitizeStoredPath(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replace('\\', '/');
    }

    private static final class ConfigState {
        private final Path baseDirectory;
        private final Path defaultConfigFile;
        private final Path defaultViewSavesDir;
        private GlobalConfig config;
        private Path activeConfigPath;
        private Path activeViewSavesPath;
        private boolean autoLoadEnabled;
        private boolean configIgnored;
        private boolean loaded;

        private ConfigState(String namespace) {
            this.baseDirectory = NAMESPACE_ROOT.resolve(namespace).normalize();
            this.defaultConfigFile = baseDirectory.resolve("global_config.json");
            this.defaultViewSavesDir = baseDirectory.resolve(GlobalConfig.getDefaultViewSavesPath());
            this.config = new GlobalConfig();
            this.activeConfigPath = defaultConfigFile;
            this.activeViewSavesPath = defaultViewSavesDir;
            this.autoLoadEnabled = true;
            this.configIgnored = false;
            this.loaded = false;
        }
    }
}
