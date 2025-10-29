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
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GlobalConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_NAMESPACE = MineGuiCore.ID;
    private static final Map<String, ConfigState> CONTEXTS = new HashMap<>();
    private static final Path CONFIG_ROOT = determineConfigRoot();
    private static final Path NAMESPACE_ROOT = CONFIG_ROOT.resolve(MineGuiCore.ID).normalize();
    private static final ConfigPathStrategy DEFAULT_STRATEGY = ConfigPathStrategies.sandboxed();
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
        state.loaded = false;
    }

    public static synchronized void ensureContext(String namespace) {
        context(namespace);
    }

    public static synchronized void setConfigPathStrategy(String namespace, ConfigPathStrategy strategy) {
        ConfigState state = context(namespace);
        state.strategy = strategy != null ? strategy : DEFAULT_STRATEGY;
        state.loaded = false;
        state.activeConfigPath = state.defaultConfigFile;
        state.activeViewSavesPath = state.defaultViewSavesDir;
    }

    public static synchronized ConfigPathStrategy getConfigPathStrategy() {
        return getConfigPathStrategy(defaultNamespace);
    }

    public static synchronized void setConfigPathStrategy(ConfigPathStrategy strategy) {
        setConfigPathStrategy(defaultNamespace, strategy);
    }

    public static synchronized ConfigPathStrategy getConfigPathStrategy(String namespace) {
        ConfigState state = context(namespace);
        return state.strategy;
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
            state.config = new GlobalConfig();
            state.snapshot = cloneConfig(state.config);
            state.activeConfigPath = state.defaultConfigFile;
            state.activeViewSavesPath = state.defaultViewSavesDir;
            state.loaded = true;
            return;
        }
        if (state.loaded) {
            return;
        }

        ensureDirectory(state.defaultConfigFile.getParent());
        GlobalConfig baseDocument = readConfig(state.defaultConfigFile);
        GlobalConfig snapshot = cloneConfig(baseDocument != null ? baseDocument : new GlobalConfig());
        ensureViewPath(snapshot);
        Path snapshotConfigPath = resolveConfigPath(state, snapshot);

        if (state.featureProfile.shouldLoad(ConfigFeature.CORE)) {
            GlobalConfig overrideConfig = readConfig(snapshotConfigPath);
            if (overrideConfig != null) {
                snapshot = cloneConfig(overrideConfig);
                ensureViewPath(snapshot);
                snapshotConfigPath = resolveConfigPath(state, snapshot);
            }
        }

        Path snapshotViewPath = resolveViewSavesPath(state, snapshot);
        ensureDirectory(snapshotConfigPath.getParent());
        ensureDirectory(snapshotViewPath);

        GlobalConfig runtime = applyLoadProfile(snapshot, state.featureProfile);
        ensureViewPath(runtime);
        Path runtimeConfigPath = resolveConfigPath(state, runtime);
        Path runtimeViewPath = resolveViewSavesPath(state, runtime);
        ensureDirectory(runtimeConfigPath.getParent());
        ensureDirectory(runtimeViewPath);

        if (!Files.exists(runtimeConfigPath)) {
            GlobalConfig initialPayload = mergeForSave(runtime, snapshot, state.featureProfile);
            writeConfig(runtimeConfigPath, initialPayload, state);
            snapshot = cloneConfig(initialPayload);
        }

        state.snapshot = snapshot;
        state.config = runtime;
        state.activeConfigPath = runtimeConfigPath;
        state.activeViewSavesPath = runtimeViewPath;
        state.loaded = true;
    }

    public static synchronized void save() {
        save(defaultNamespace);
    }

    public static synchronized void save(String namespace) {
        ConfigState state = context(namespace);
        if (state.configIgnored) {
            state.config = new GlobalConfig();
            state.snapshot = cloneConfig(state.config);
            state.activeConfigPath = state.defaultConfigFile;
            state.activeViewSavesPath = state.defaultViewSavesDir;
            state.loaded = true;
            return;
        }
        if (state.autoLoadEnabled && !state.loaded) {
            load(namespace);
        }

        ensureViewPath(state.config);
        Path runtimeConfigPath = resolveConfigPath(state, state.config);
        Path runtimeViewPath = resolveViewSavesPath(state, state.config);
        ensureDirectory(runtimeConfigPath.getParent());
        ensureDirectory(runtimeViewPath);

        GlobalConfig payload = mergeForSave(state.config, state.snapshot, state.featureProfile);
        writeConfig(runtimeConfigPath, payload, state);
        state.snapshot = cloneConfig(payload);
        state.activeConfigPath = runtimeConfigPath;
        state.activeViewSavesPath = runtimeViewPath;
        state.loaded = true;
    }

    public static synchronized void reset() {
        reset(defaultNamespace);
    }

    public static synchronized void reset(String namespace) {
        ConfigState state = context(namespace);
        if (state.configIgnored) {
            state.config = new GlobalConfig();
            state.snapshot = cloneConfig(state.config);
            state.activeConfigPath = state.defaultConfigFile;
            state.activeViewSavesPath = state.defaultViewSavesDir;
            state.loaded = false;
            return;
        }

        Path currentPath = resolveConfigPath(state, state.config);
        deleteIfExists(currentPath);
        if (!Objects.equals(currentPath, state.defaultConfigFile)) {
            deleteIfExists(state.defaultConfigFile);
        }
        state.config = new GlobalConfig();
        state.snapshot = cloneConfig(state.config);
        state.activeConfigPath = state.defaultConfigFile;
        state.activeViewSavesPath = state.defaultViewSavesDir;
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
        state.activeConfigPath = resolveConfigPath(state, state.config);
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
        state.activeViewSavesPath = resolveViewSavesPath(state, state.config);
        return state.activeViewSavesPath;
    }

    public static synchronized ConfigFeatureProfile getFeatureProfile() {
        return getFeatureProfile(defaultNamespace);
    }

    public static synchronized void setFeatureProfile(ConfigFeatureProfile profile) {
        setFeatureProfile(defaultNamespace, profile);
    }

    public static synchronized ConfigFeatureProfile getFeatureProfile(String namespace) {
        ConfigState state = context(namespace);
        return state.featureProfile;
    }

    public static synchronized void setFeatureProfile(String namespace, ConfigFeatureProfile profile) {
        ConfigState state = context(namespace);
        state.featureProfile = profile != null ? profile : ConfigFeatureProfile.all();
        state.loaded = false;
    }

    public static synchronized void setLoadFeatures(String namespace, Set<ConfigFeature> features) {
        ConfigState state = context(namespace);
        ConfigFeatureProfile current = state.featureProfile;
        state.featureProfile = current.withLoadFeatures(features != null ? features : Set.of());
        state.loaded = false;
    }

    public static synchronized void setSaveFeatures(String namespace, Set<ConfigFeature> features) {
        ConfigState state = context(namespace);
        ConfigFeatureProfile current = state.featureProfile;
        state.featureProfile = current.withSaveFeatures(features != null ? features : Set.of());
        state.loaded = false;
    }

    public static synchronized void enableFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        state.featureProfile = state.featureProfile.withFeature(feature);
        state.loaded = false;
    }

    public static synchronized void disableFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        state.featureProfile = state.featureProfile.withoutFeature(feature);
        state.loaded = false;
    }

    public static synchronized boolean shouldLoadFeature(ConfigFeature feature) {
        return shouldLoadFeature(defaultNamespace, feature);
    }

    public static synchronized boolean shouldLoadFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        return state.featureProfile.shouldLoad(feature);
    }

    public static synchronized boolean shouldSaveFeature(ConfigFeature feature) {
        return shouldSaveFeature(defaultNamespace, feature);
    }

    public static synchronized boolean shouldSaveFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        return state.featureProfile.shouldSave(feature);
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
        return Objects.requireNonNullElseGet(configDir, () -> Path.of("config")).toAbsolutePath().normalize();
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
            parsed.setViewSavesPath(Objects.requireNonNullElseGet(storedViewPath, GlobalConfig::getDefaultViewSavesPath));
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

    private static GlobalConfig applyLoadProfile(GlobalConfig source, ConfigFeatureProfile profile) {
        GlobalConfig runtime = new GlobalConfig();
        if (profile.shouldLoad(ConfigFeature.CORE)) {
            runtime.setViewport(source.isViewportEnabled());
            runtime.setDockspace(source.isDockspaceEnabled());
            runtime.setGlobalScale(source.getGlobalScale());
            runtime.setConfigPath(source.getConfigPath());
            runtime.setViewSavesPath(source.getViewSavesPath());
        }
        if (profile.shouldLoad(ConfigFeature.STYLE_REFERENCES)) {
            runtime.setGlobalStyleKey(source.getGlobalStyleKey());
            runtime.setViewStyles(source.getViewStyles());
        } else {
            runtime.setGlobalStyleKey(null);
            runtime.setViewStyles(new HashMap<>());
        }
        return runtime;
    }

    private static GlobalConfig mergeForSave(GlobalConfig runtime, GlobalConfig snapshot, ConfigFeatureProfile profile) {
        GlobalConfig target = snapshot != null ? cloneConfig(snapshot) : new GlobalConfig();
        if (profile.shouldSave(ConfigFeature.CORE)) {
            target.setViewport(runtime.isViewportEnabled());
            target.setDockspace(runtime.isDockspaceEnabled());
            target.setGlobalScale(runtime.getGlobalScale());
            target.setConfigPath(runtime.getConfigPath());
            target.setViewSavesPath(runtime.getViewSavesPath());
        }
        if (profile.shouldSave(ConfigFeature.STYLE_REFERENCES)) {
            target.setGlobalStyleKey(runtime.getGlobalStyleKey());
            target.setViewStyles(runtime.getViewStyles());
        }
        return target;
    }

    private static void writeConfig(Path path, GlobalConfig value, ConfigState state) {
        GlobalConfig payload = cloneConfig(value);
        payload.setConfigPath(relativizeToConfigRoot(path));
        Path resolvedViewDir = resolveViewSavesPath(state, payload);
        ensureDirectory(resolvedViewDir);
        payload.setViewSavesPath(relativizeToConfigRoot(resolvedViewDir));
        payload.setGlobalScale(payload.getGlobalScale());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            GSON.toJson(payload, writer);
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

    private static Path resolveConfigPath(ConfigState state, GlobalConfig target) {
        ConfigPathResolution resolution = resolvePaths(state, target);
        Path resolved = resolution.configFile();
        if (target != null) {
            target.setConfigPath(relativizeToConfigRoot(resolved));
        }
        return resolved;
    }

    private static Path resolveViewSavesPath(ConfigState state, GlobalConfig target) {
        ConfigPathResolution resolution = resolvePaths(state, target);
        Path resolved = resolution.viewSavesDirectory();
        if (target != null) {
            target.setViewSavesPath(relativizeToConfigRoot(resolved));
        }
        return resolved;
    }

    private static void ensureViewPath(GlobalConfig config) {
        if (config.getViewSavesPath() == null || config.getViewSavesPath().isBlank()) {
            config.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
        }
    }

    private static ConfigPathResolution resolvePaths(ConfigState state, GlobalConfig target) {
        GlobalConfig source = target != null ? target : new GlobalConfig();
        String sanitizedConfig = sanitizeStoredPath(source.getConfigPath());
        String sanitizedViews = sanitizeStoredPath(source.getViewSavesPath());
        ConfigPathRequest request = buildRequest(state, sanitizedConfig, sanitizedViews);
        Path configPath = resolvePathWithFallback(state, request, ConfigPathKind.CONFIG_FILE);
        Path viewPath = resolvePathWithFallback(state, request, ConfigPathKind.VIEW_SAVES_DIRECTORY);
        return new ConfigPathResolution(configPath, viewPath);
    }

    private static ConfigPathRequest buildRequest(ConfigState state, String configPath, String viewPath) {
        return new ConfigPathRequest(
                state.namespace,
                configPath,
                viewPath,
                CONFIG_ROOT,
                NAMESPACE_ROOT,
                state.baseDirectory,
                state.defaultConfigFile,
                state.defaultViewSavesDir
        );
    }

    private static Path resolvePathWithFallback(ConfigState state, ConfigPathRequest request, ConfigPathKind kind) {
        ConfigPathStrategy strategy = state.strategy != null ? state.strategy : DEFAULT_STRATEGY;
        Path resolved = resolveWithStrategy(strategy, request, kind, state.namespace);
        if (resolved == null && strategy != DEFAULT_STRATEGY) {
            MineGuiCore.LOGGER.warn("Config path strategy for namespace '{}' returned an invalid {}; using sandboxed defaults.", state.namespace, describe(kind));
            resolved = resolveWithStrategy(DEFAULT_STRATEGY, request, kind, state.namespace);
        }
        if (resolved == null) {
            resolved = kind == ConfigPathKind.CONFIG_FILE ? state.defaultConfigFile : state.defaultViewSavesDir;
        }
        return resolved.normalize();
    }

    private static Path resolveWithStrategy(ConfigPathStrategy strategy, ConfigPathRequest request, ConfigPathKind kind, String namespace) {
        if (strategy == null) {
            return null;
        }
        try {
            Path candidate = kind == ConfigPathKind.CONFIG_FILE
                    ? strategy.resolveConfigFile(request)
                    : strategy.resolveViewSavesDirectory(request);
            if (candidate == null) {
                return null;
            }
            Path normalized = candidate.normalize();
            ConfigPathValidationResult validation = kind == ConfigPathKind.CONFIG_FILE
                    ? strategy.validateConfigFile(request, normalized)
                    : strategy.validateViewSavesDirectory(request, normalized);
            String message = validation != null ? validation.message() : null;
            boolean hasMessage = message != null && !message.isBlank();
            if (validation == null) {
                return normalized;
            }
            if (!validation.allowed()) {
                if (hasMessage) {
                    MineGuiCore.LOGGER.warn(message);
                }
                return null;
            }
            Path resultPath = validation.path() != null ? validation.path().normalize() : null;
            if (resultPath == null) {
                return null;
            }
            if (hasMessage) {
                MineGuiCore.LOGGER.info(message);
            }
            return resultPath;
        } catch (RuntimeException e) {
            MineGuiCore.LOGGER.error("Config path strategy for namespace '{}' failed to resolve {}.", namespace, describe(kind), e);
            return null;
        }
    }

    private static String describe(ConfigPathKind kind) {
        if (kind == ConfigPathKind.CONFIG_FILE) {
            return "config file";
        }
        return "view saves directory";
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

    private static GlobalConfig cloneConfig(GlobalConfig config) {
        if (config == null) {
            return new GlobalConfig();
        }
        GlobalConfig clone = new GlobalConfig();
        clone.setViewport(config.isViewportEnabled());
        clone.setDockspace(config.isDockspaceEnabled());
        clone.setGlobalScale(config.getGlobalScale());
        clone.setConfigPath(config.getConfigPath());
        clone.setViewSavesPath(config.getViewSavesPath());
        clone.setGlobalStyleKey(config.getGlobalStyleKey());
        clone.setViewStyles(config.getViewStyles());
        return clone;
    }

    private enum ConfigPathKind {
        CONFIG_FILE,
        VIEW_SAVES_DIRECTORY
    }

    private static final class ConfigState {
        private final String namespace;
        private final Path baseDirectory;
        private final Path defaultConfigFile;
        private final Path defaultViewSavesDir;
        private GlobalConfig config;
        private GlobalConfig snapshot;
        private Path activeConfigPath;
        private Path activeViewSavesPath;
        private ConfigFeatureProfile featureProfile;
        private ConfigPathStrategy strategy;
        private boolean autoLoadEnabled;
        private boolean configIgnored;
        private boolean loaded;

        private ConfigState(String namespace) {
            this.namespace = namespace;
            this.baseDirectory = NAMESPACE_ROOT.resolve(namespace).normalize();
            this.defaultConfigFile = baseDirectory.resolve("global_config.json");
            this.defaultViewSavesDir = baseDirectory.resolve(GlobalConfig.getDefaultViewSavesPath());
            this.config = new GlobalConfig();
            this.snapshot = cloneConfig(this.config);
            this.activeConfigPath = defaultConfigFile;
            this.activeViewSavesPath = defaultViewSavesDir;
            this.featureProfile = ConfigFeatureProfile.all();
            this.strategy = DEFAULT_STRATEGY;
            this.autoLoadEnabled = true;
            this.configIgnored = false;
            this.loaded = false;
        }
    }
}
