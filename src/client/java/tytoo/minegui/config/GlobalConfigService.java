package tytoo.minegui.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class GlobalConfigService {
    private final String namespace;
    private final ConfigState state;
    private final ConfigPathResolver pathResolver;
    private final ConfigSerializer serializer;

    GlobalConfigService(String namespace, Path configRoot, ConfigPathStrategy defaultStrategy) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.state = new ConfigState(namespace, configRoot, defaultStrategy);
        this.pathResolver = new ConfigPathResolver(configRoot, configRoot, defaultStrategy);
        this.serializer = new ConfigSerializer();
    }

    public String namespace() {
        return namespace;
    }

    public GlobalConfig config() {
        if (!state.isConfigIgnored() && state.isAutoLoadEnabled() && !state.isLoaded()) {
            load();
        }
        return state.config();
    }

    public boolean isAutoLoadEnabled() {
        if (state.isConfigIgnored()) {
            return false;
        }
        return state.isAutoLoadEnabled();
    }

    public void setAutoLoadEnabled(boolean enabled) {
        state.setAutoLoadPreference(enabled);
        if (state.isConfigIgnored()) {
            state.setAutoLoadEnabled(false);
            return;
        }
        state.setAutoLoadEnabled(enabled);
    }

    public boolean isConfigIgnored() {
        return state.isConfigIgnored();
    }

    public void setConfigIgnored(boolean ignored) {
        state.setConfigIgnored(ignored);
        if (ignored) {
            state.setAutoLoadEnabled(false);
            state.resetInMemory();
        } else {
            state.setAutoLoadEnabled(state.autoLoadPreference());
        }
        state.setLoaded(false);
    }

    public ConfigPathStrategy pathStrategy() {
        return state.strategy();
    }

    public void setPathStrategy(ConfigPathStrategy strategy) {
        state.setStrategy(strategy != null ? strategy : ConfigPathStrategies.sandboxed());
        state.setLoaded(false);
        state.setActiveConfigPath(state.defaultConfigFile());
        state.setActiveViewSavesPath(state.defaultViewSavesDir());
    }

    public ConfigFeatureProfile featureProfile() {
        return state.featureProfile();
    }

    public void setFeatureProfile(ConfigFeatureProfile profile) {
        state.setFeatureProfile(profile != null ? profile : ConfigFeatureProfile.all());
        state.setLoaded(false);
    }

    public void setLoadFeatures(Set<ConfigFeature> features) {
        ConfigFeatureProfile current = state.featureProfile();
        state.setFeatureProfile(current.withLoadFeatures(features != null ? features : Set.of()));
        state.setLoaded(false);
    }

    public void setSaveFeatures(Set<ConfigFeature> features) {
        ConfigFeatureProfile current = state.featureProfile();
        state.setFeatureProfile(current.withSaveFeatures(features != null ? features : Set.of()));
        state.setLoaded(false);
    }

    public void enableFeature(ConfigFeature feature) {
        state.setFeatureProfile(state.featureProfile().withFeature(feature));
        state.setLoaded(false);
    }

    public void disableFeature(ConfigFeature feature) {
        state.setFeatureProfile(state.featureProfile().withoutFeature(feature));
        state.setLoaded(false);
    }

    public boolean shouldLoadFeature(ConfigFeature feature) {
        return state.featureProfile().shouldLoad(feature);
    }

    public boolean shouldSaveFeature(ConfigFeature feature) {
        return state.featureProfile().shouldSave(feature);
    }

    public Path activeConfigPath() {
        if (state.isConfigIgnored()) {
            return state.defaultConfigFile();
        }
        if (state.isAutoLoadEnabled() && !state.isLoaded()) {
            load();
        }
        state.setActiveConfigPath(pathResolver.resolveConfigPath(state, state.config()));
        return state.activeConfigPath();
    }

    public Path viewSavesDirectory() {
        if (state.isConfigIgnored()) {
            return state.defaultViewSavesDir();
        }
        if (state.isAutoLoadEnabled() && !state.isLoaded()) {
            load();
        }
        state.setActiveViewSavesPath(pathResolver.resolveViewSavesPath(state, state.config()));
        return state.activeViewSavesPath();
    }

    public void load() {
        if (state.isConfigIgnored()) {
            applyIgnoredDefaults(true);
            return;
        }
        if (state.isLoaded()) {
            return;
        }

        ConfigSerializer.ensureDirectory(state.defaultConfigFile().getParent());
        GlobalConfig baseDocument = serializer.readConfig(state.defaultConfigFile());
        GlobalConfig snapshot = ConfigState.cloneConfig(baseDocument != null ? baseDocument : new GlobalConfig());
        ConfigPathResolution snapshotPaths = pathResolver.resolvePaths(state, snapshot);
        pathResolver.applyResolvedPaths(state, snapshot, snapshotPaths);
        Path snapshotConfigPath = snapshotPaths.configFile();

        if (state.featureProfile().shouldLoad(ConfigFeature.CORE)) {
            GlobalConfig overrideConfig = serializer.readConfig(snapshotConfigPath);
            if (overrideConfig != null) {
                snapshot = ConfigState.cloneConfig(overrideConfig);
                snapshotPaths = pathResolver.resolvePaths(state, snapshot);
                pathResolver.applyResolvedPaths(state, snapshot, snapshotPaths);
                snapshotConfigPath = snapshotPaths.configFile();
            }
        }

        Path snapshotViewPath = snapshotPaths.viewSavesDirectory();
        ConfigSerializer.ensureDirectory(snapshotConfigPath.getParent());

        GlobalConfig runtime = serializer.applyLoadProfile(snapshot, state.featureProfile());
        ConfigPathResolution runtimePaths = pathResolver.resolvePaths(state, runtime);
        pathResolver.applyResolvedPaths(state, runtime, runtimePaths);
        Path runtimeConfigPath = runtimePaths.configFile();
        Path runtimeViewPath = runtimePaths.viewSavesDirectory();
        ConfigSerializer.ensureDirectory(runtimeConfigPath.getParent());

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

    public void save() {
        if (state.isConfigIgnored()) {
            applyIgnoredDefaults(true);
            return;
        }
        if (state.isAutoLoadEnabled() && !state.isLoaded()) {
            load();
        }

        ConfigPathResolution runtimePaths = pathResolver.resolvePaths(state, state.config());
        pathResolver.applyResolvedPaths(state, state.config(), runtimePaths);
        Path runtimeConfigPath = runtimePaths.configFile();
        Path runtimeViewPath = runtimePaths.viewSavesDirectory();
        ConfigSerializer.ensureDirectory(runtimeConfigPath.getParent());

        GlobalConfig payload = serializer.mergeForSave(state.config(), state.snapshot(), state.featureProfile());
        serializer.writeConfig(runtimeConfigPath, payload, state, pathResolver);
        state.setSnapshot(ConfigState.cloneConfig(payload));
        state.setActiveConfigPath(runtimeConfigPath);
        state.setActiveViewSavesPath(runtimeViewPath);
        state.setLoaded(true);
    }

    public void reset() {
        if (state.isConfigIgnored()) {
            applyIgnoredDefaults(false);
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

    ConfigState state() {
        return state;
    }

    GlobalConfigService refreshWith(Path newConfigRoot, ConfigPathStrategy defaultStrategy) {
        GlobalConfigService refreshed = new GlobalConfigService(namespace, newConfigRoot, defaultStrategy);
        ConfigState refreshedState = refreshed.state;
        refreshedState.setFeatureProfile(state.featureProfile());
        refreshedState.setStrategy(state.strategy());
        refreshedState.setAutoLoadPreference(state.autoLoadPreference());
        refreshedState.setAutoLoadEnabled(!state.isConfigIgnored() && state.isAutoLoadEnabled());
        refreshedState.setConfigIgnored(state.isConfigIgnored());
        refreshedState.setLoaded(false);
        refreshedState.setConfig(ConfigState.cloneConfig(state.config()));
        refreshedState.setSnapshot(ConfigState.cloneConfig(state.snapshot()));
        return refreshed;
    }

    private void applyIgnoredDefaults(boolean markLoaded) {
        state.setConfig(new GlobalConfig());
        state.setSnapshot(ConfigState.cloneConfig(state.config()));
        state.setActiveConfigPath(state.defaultConfigFile());
        state.setActiveViewSavesPath(state.defaultViewSavesDir());
        state.setLoaded(markLoaded);
    }
}
