package tytoo.minegui.config;

import net.fabricmc.loader.api.FabricLoader;
import tytoo.minegui.MineGuiCore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class GlobalConfigManager {
    private static final String DEFAULT_NAMESPACE = MineGuiCore.ID;
    private static final Pattern VALID_NAMESPACE = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Map<String, ConfigState> CONTEXTS = new HashMap<>();
    private static final ConfigPathStrategy DEFAULT_STRATEGY = ConfigPathStrategies.sandboxed();
    private static Path configRoot = determineConfigRoot();
    private static Path namespaceRoot = configRoot;
    private static String defaultNamespace = DEFAULT_NAMESPACE;
    private static ConfigPathResolver pathResolver = new ConfigPathResolver(configRoot, namespaceRoot, DEFAULT_STRATEGY);
    private static ConfigSerializer serializer = new ConfigSerializer(configRoot);

    private GlobalConfigManager() {
    }

    public static synchronized void configure(Path rootPath) {
        if (rootPath == null) {
            return;
        }
        configRoot = rootPath.toAbsolutePath().normalize();
        namespaceRoot = configRoot;
        pathResolver = new ConfigPathResolver(configRoot, namespaceRoot, DEFAULT_STRATEGY);
        serializer = new ConfigSerializer(configRoot);
        rebindContexts();
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
        if (state.isConfigIgnored()) {
            return false;
        }
        return state.isAutoLoadEnabled();
    }

    public static synchronized void setAutoLoadEnabled(String namespace, boolean enabled) {
        ConfigState state = context(namespace);
        state.setAutoLoadPreference(enabled);
        if (state.isConfigIgnored()) {
            state.setAutoLoadEnabled(false);
            return;
        }
        state.setAutoLoadEnabled(enabled);
    }

    public static synchronized boolean isConfigIgnored() {
        return isConfigIgnored(defaultNamespace);
    }

    public static synchronized void setConfigIgnored(boolean ignored) {
        setConfigIgnored(defaultNamespace, ignored);
    }

    public static synchronized boolean isConfigIgnored(String namespace) {
        ConfigState state = context(namespace);
        return state.isConfigIgnored();
    }

    public static synchronized void setConfigIgnored(String namespace, boolean ignored) {
        ConfigState state = context(namespace);
        state.setConfigIgnored(ignored);
        if (ignored) {
            state.setAutoLoadEnabled(false);
            state.resetInMemory();
        } else {
            state.setAutoLoadEnabled(state.autoLoadPreference());
        }
        state.setLoaded(false);
    }

    public static synchronized void ensureContext(String namespace) {
        context(namespace);
    }

    public static synchronized void setConfigPathStrategy(String namespace, ConfigPathStrategy strategy) {
        ConfigState state = context(namespace);
        state.setStrategy(strategy != null ? strategy : DEFAULT_STRATEGY);
        state.setLoaded(false);
        state.setActiveConfigPath(state.defaultConfigFile());
        state.setActiveViewSavesPath(state.defaultViewSavesDir());
    }

    public static synchronized ConfigPathStrategy getConfigPathStrategy() {
        return getConfigPathStrategy(defaultNamespace);
    }

    public static synchronized void setConfigPathStrategy(ConfigPathStrategy strategy) {
        setConfigPathStrategy(defaultNamespace, strategy);
    }

    public static synchronized ConfigPathStrategy getConfigPathStrategy(String namespace) {
        ConfigState state = context(namespace);
        return state.strategy();
    }

    public static synchronized GlobalConfig getConfig() {
        return getConfig(defaultNamespace);
    }

    public static synchronized GlobalConfig getConfig(String namespace) {
        ConfigState state = context(namespace);
        if (!state.isConfigIgnored() && state.isAutoLoadEnabled() && !state.isLoaded()) {
            load(namespace);
        }
        return state.config();
    }

    public static synchronized void load() {
        load(defaultNamespace);
    }

    public static synchronized void load(String namespace) {
        ConfigState state = context(namespace);
        if (state.isConfigIgnored()) {
            applyIgnoredDefaults(state, true);
            return;
        }
        if (state.isLoaded()) {
            return;
        }

        ConfigSerializer.ensureDirectory(state.defaultConfigFile().getParent());
        GlobalConfig baseDocument = serializer.readConfig(state.defaultConfigFile());
        GlobalConfig snapshot = ConfigState.cloneConfig(baseDocument != null ? baseDocument : new GlobalConfig());
        ensureViewPath(snapshot);
        ConfigPathResolution snapshotPaths = pathResolver.resolvePaths(state, snapshot);
        pathResolver.applyResolvedPaths(state, snapshot, snapshotPaths);
        Path snapshotConfigPath = snapshotPaths.configFile();

        if (state.featureProfile().shouldLoad(ConfigFeature.CORE)) {
            GlobalConfig overrideConfig = serializer.readConfig(snapshotConfigPath);
            if (overrideConfig != null) {
                snapshot = ConfigState.cloneConfig(overrideConfig);
                ensureViewPath(snapshot);
                snapshotPaths = pathResolver.resolvePaths(state, snapshot);
                pathResolver.applyResolvedPaths(state, snapshot, snapshotPaths);
                snapshotConfigPath = snapshotPaths.configFile();
            }
        }

        Path snapshotViewPath = snapshotPaths.viewSavesDirectory();
        ConfigSerializer.ensureDirectory(snapshotConfigPath.getParent());
        ConfigSerializer.ensureDirectory(snapshotViewPath);

        GlobalConfig runtime = serializer.applyLoadProfile(snapshot, state.featureProfile());
        ensureViewPath(runtime);
        ConfigPathResolution runtimePaths = pathResolver.resolvePaths(state, runtime);
        pathResolver.applyResolvedPaths(state, runtime, runtimePaths);
        Path runtimeConfigPath = runtimePaths.configFile();
        Path runtimeViewPath = runtimePaths.viewSavesDirectory();
        ConfigSerializer.ensureDirectory(runtimeConfigPath.getParent());
        ConfigSerializer.ensureDirectory(runtimeViewPath);

        if (!Files.exists(runtimeConfigPath)) {
            GlobalConfig initialPayload = serializer.mergeForSave(runtime, snapshot, state.featureProfile());
            serializer.writeConfig(runtimeConfigPath, initialPayload, state, pathResolver);
            snapshot = ConfigState.cloneConfig(initialPayload);
        }

        state.setSnapshot(snapshot);
        state.setConfig(runtime);
        state.setActiveConfigPath(runtimeConfigPath);
        state.setActiveViewSavesPath(runtimeViewPath);
        state.setLoaded(true);
    }

    public static synchronized void save() {
        save(defaultNamespace);
    }

    public static synchronized void save(String namespace) {
        ConfigState state = context(namespace);
        if (state.isConfigIgnored()) {
            applyIgnoredDefaults(state, true);
            return;
        }
        if (state.isAutoLoadEnabled() && !state.isLoaded()) {
            load(namespace);
        }

        ensureViewPath(state.config());
        ConfigPathResolution runtimePaths = pathResolver.resolvePaths(state, state.config());
        pathResolver.applyResolvedPaths(state, state.config(), runtimePaths);
        Path runtimeConfigPath = runtimePaths.configFile();
        Path runtimeViewPath = runtimePaths.viewSavesDirectory();
        ConfigSerializer.ensureDirectory(runtimeConfigPath.getParent());
        ConfigSerializer.ensureDirectory(runtimeViewPath);

        GlobalConfig payload = serializer.mergeForSave(state.config(), state.snapshot(), state.featureProfile());
        serializer.writeConfig(runtimeConfigPath, payload, state, pathResolver);
        state.setSnapshot(ConfigState.cloneConfig(payload));
        state.setActiveConfigPath(runtimeConfigPath);
        state.setActiveViewSavesPath(runtimeViewPath);
        state.setLoaded(true);
    }

    public static synchronized void reset() {
        reset(defaultNamespace);
    }

    public static synchronized void reset(String namespace) {
        ConfigState state = context(namespace);
        if (state.isConfigIgnored()) {
            applyIgnoredDefaults(state, false);
            return;
        }

        Path currentPath = pathResolver.resolveConfigPath(state, state.config());
        ConfigSerializer.deleteIfExists(currentPath);
        if (!Objects.equals(currentPath, state.defaultConfigFile())) {
            ConfigSerializer.deleteIfExists(state.defaultConfigFile());
        }
        state.setConfig(new GlobalConfig());
        state.setSnapshot(ConfigState.cloneConfig(state.config()));
        state.setActiveConfigPath(state.defaultConfigFile());
        state.setActiveViewSavesPath(state.defaultViewSavesDir());
        state.setLoaded(false);
    }

    public static synchronized Path getActiveConfigPath() {
        return getActiveConfigPath(defaultNamespace);
    }

    public static synchronized Path getActiveConfigPath(String namespace) {
        ConfigState state = context(namespace);
        if (state.isConfigIgnored()) {
            return state.defaultConfigFile();
        }
        if (state.isAutoLoadEnabled() && !state.isLoaded()) {
            load(namespace);
        }
        state.setActiveConfigPath(pathResolver.resolveConfigPath(state, state.config()));
        return state.activeConfigPath();
    }

    public static synchronized Path getViewSavesDirectory() {
        return getViewSavesDirectory(defaultNamespace);
    }

    public static synchronized Path getViewSavesDirectory(String namespace) {
        ConfigState state = context(namespace);
        if (state.isConfigIgnored()) {
            return state.defaultViewSavesDir();
        }
        if (state.isAutoLoadEnabled() && !state.isLoaded()) {
            load(namespace);
        }
        state.setActiveViewSavesPath(pathResolver.resolveViewSavesPath(state, state.config()));
        return state.activeViewSavesPath();
    }

    public static synchronized ConfigFeatureProfile getFeatureProfile() {
        return getFeatureProfile(defaultNamespace);
    }

    public static synchronized void setFeatureProfile(ConfigFeatureProfile profile) {
        setFeatureProfile(defaultNamespace, profile);
    }

    public static synchronized ConfigFeatureProfile getFeatureProfile(String namespace) {
        ConfigState state = context(namespace);
        return state.featureProfile();
    }

    public static synchronized void setFeatureProfile(String namespace, ConfigFeatureProfile profile) {
        ConfigState state = context(namespace);
        state.setFeatureProfile(profile != null ? profile : ConfigFeatureProfile.all());
        state.setLoaded(false);
    }

    public static synchronized void setLoadFeatures(String namespace, Set<ConfigFeature> features) {
        ConfigState state = context(namespace);
        ConfigFeatureProfile current = state.featureProfile();
        state.setFeatureProfile(current.withLoadFeatures(features != null ? features : Set.of()));
        state.setLoaded(false);
    }

    public static synchronized void setSaveFeatures(String namespace, Set<ConfigFeature> features) {
        ConfigState state = context(namespace);
        ConfigFeatureProfile current = state.featureProfile();
        state.setFeatureProfile(current.withSaveFeatures(features != null ? features : Set.of()));
        state.setLoaded(false);
    }

    public static synchronized void enableFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        state.setFeatureProfile(state.featureProfile().withFeature(feature));
        state.setLoaded(false);
    }

    public static synchronized void disableFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        state.setFeatureProfile(state.featureProfile().withoutFeature(feature));
        state.setLoaded(false);
    }

    public static synchronized boolean shouldLoadFeature(ConfigFeature feature) {
        return shouldLoadFeature(defaultNamespace, feature);
    }

    public static synchronized boolean shouldLoadFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        return state.featureProfile().shouldLoad(feature);
    }

    public static synchronized boolean shouldSaveFeature(ConfigFeature feature) {
        return shouldSaveFeature(defaultNamespace, feature);
    }

    public static synchronized boolean shouldSaveFeature(String namespace, ConfigFeature feature) {
        ConfigState state = context(namespace);
        return state.featureProfile().shouldSave(feature);
    }

    private static ConfigState context(String namespace) {
        String sanitized = sanitizeNamespace(namespace);
        return CONTEXTS.computeIfAbsent(sanitized, ns -> new ConfigState(ns, configRoot, DEFAULT_STRATEGY));
    }

    private static String sanitizeNamespace(String namespace) {
        if (namespace == null) {
            return DEFAULT_NAMESPACE;
        }
        String trimmed = namespace.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_NAMESPACE;
        }
        if (!VALID_NAMESPACE.matcher(trimmed).matches()) {
            MineGuiCore.LOGGER.warn("Invalid namespace '{}'; using default '{}'", namespace, DEFAULT_NAMESPACE);
            return DEFAULT_NAMESPACE;
        }
        Path resolved = namespaceRoot.resolve(trimmed).normalize();
        if (!resolved.startsWith(namespaceRoot)) {
            MineGuiCore.LOGGER.warn("Namespace '{}' resolves outside of MineGui config root; using default '{}'", namespace, DEFAULT_NAMESPACE);
            return DEFAULT_NAMESPACE;
        }
        return trimmed;
    }

    private static Path determineConfigRoot() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return Objects.requireNonNullElseGet(configDir, () -> Path.of("config")).toAbsolutePath().normalize();
    }

    private static void ensureViewPath(GlobalConfig config) {
        if (config.getViewSavesPath() == null || config.getViewSavesPath().isBlank()) {
            config.setViewSavesPath(GlobalConfig.getDefaultViewSavesPath());
        }
    }

    private static void rebindContexts() {
        if (CONTEXTS.isEmpty()) {
            return;
        }
        Map<String, ConfigState> previous = new HashMap<>(CONTEXTS);
        CONTEXTS.clear();
        for (Map.Entry<String, ConfigState> entry : previous.entrySet()) {
            ConfigState oldState = entry.getValue();
            ConfigState refreshed = oldState.refreshWith(configRoot, DEFAULT_STRATEGY);
            CONTEXTS.put(entry.getKey(), refreshed);
        }
    }

    private static void applyIgnoredDefaults(ConfigState state, boolean markLoaded) {
        state.setConfig(new GlobalConfig());
        state.setSnapshot(ConfigState.cloneConfig(state.config()));
        state.setActiveConfigPath(state.defaultConfigFile());
        state.setActiveViewSavesPath(state.defaultViewSavesDir());
        state.setLoaded(markLoaded);
    }
}
