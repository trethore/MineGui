package tytoo.minegui.config;

import java.nio.file.Path;

final class ConfigState {
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
    private boolean autoLoadPreference;
    private boolean configIgnored;
    private boolean loaded;

    ConfigState(String namespace, Path configRoot, ConfigPathStrategy defaultStrategy) {
        this.namespace = namespace;
        Path fileName = configRoot.getFileName();
        if (fileName != null && fileName.toString().equals(namespace)) {
            this.baseDirectory = configRoot.normalize();
        } else {
            this.baseDirectory = configRoot.resolve(namespace).normalize();
        }
        this.defaultConfigFile = baseDirectory.resolve("global_config.json");
        this.defaultViewSavesDir = baseDirectory.resolve("views");
        this.config = new GlobalConfig();
        this.snapshot = cloneConfig(this.config);
        this.activeConfigPath = defaultConfigFile;
        this.activeViewSavesPath = defaultViewSavesDir;
        this.featureProfile = ConfigFeatureProfile.all();
        this.strategy = defaultStrategy;
        this.autoLoadEnabled = true;
        this.autoLoadPreference = true;
        this.configIgnored = false;
        this.loaded = false;
    }

    String namespace() {
        return namespace;
    }

    Path baseDirectory() {
        return baseDirectory;
    }

    Path defaultConfigFile() {
        return defaultConfigFile;
    }

    Path defaultViewSavesDir() {
        return defaultViewSavesDir;
    }

    GlobalConfig config() {
        return config;
    }

    void setConfig(GlobalConfig config) {
        this.config = config;
    }

    GlobalConfig snapshot() {
        return snapshot;
    }

    void setSnapshot(GlobalConfig snapshot) {
        this.snapshot = snapshot;
    }

    Path activeConfigPath() {
        return activeConfigPath;
    }

    void setActiveConfigPath(Path path) {
        this.activeConfigPath = path;
    }

    Path activeViewSavesPath() {
        return activeViewSavesPath;
    }

    void setActiveViewSavesPath(Path path) {
        this.activeViewSavesPath = path;
    }

    ConfigFeatureProfile featureProfile() {
        return featureProfile;
    }

    void setFeatureProfile(ConfigFeatureProfile profile) {
        this.featureProfile = profile;
    }

    ConfigPathStrategy strategy() {
        return strategy;
    }

    void setStrategy(ConfigPathStrategy strategy) {
        this.strategy = strategy;
    }

    boolean isAutoLoadEnabled() {
        return autoLoadEnabled;
    }

    void setAutoLoadEnabled(boolean enabled) {
        this.autoLoadEnabled = enabled;
    }

    boolean autoLoadPreference() {
        return autoLoadPreference;
    }

    void setAutoLoadPreference(boolean preference) {
        this.autoLoadPreference = preference;
    }

    boolean isConfigIgnored() {
        return configIgnored;
    }

    void setConfigIgnored(boolean ignored) {
        this.configIgnored = ignored;
    }

    boolean isLoaded() {
        return loaded;
    }

    void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    void resetInMemory() {
        this.config = new GlobalConfig();
        this.snapshot = cloneConfig(this.config);
        this.activeConfigPath = defaultConfigFile;
        this.activeViewSavesPath = defaultViewSavesDir;
    }

    ConfigState refreshWith(Path newConfigRoot, ConfigPathStrategy defaultStrategy) {
        ConfigState refreshed = new ConfigState(namespace, newConfigRoot, defaultStrategy);
        refreshed.featureProfile = this.featureProfile;
        refreshed.strategy = this.strategy;
        refreshed.autoLoadPreference = this.autoLoadPreference;
        refreshed.autoLoadEnabled = !this.configIgnored && this.autoLoadEnabled;
        refreshed.configIgnored = this.configIgnored;
        refreshed.loaded = false;
        refreshed.config = cloneConfig(this.config);
        refreshed.snapshot = cloneConfig(this.snapshot);
        return refreshed;
    }

    static GlobalConfig cloneConfig(GlobalConfig config) {
        if (config == null) {
            return new GlobalConfig();
        }
        GlobalConfig clone = new GlobalConfig();
        clone.setViewport(config.isViewportEnabled());
        clone.setDockspace(config.isDockspaceEnabled());
        clone.setGlobalScale(config.getGlobalScale());
        clone.setGlobalStyleKey(config.getGlobalStyleKey());
        return clone;
    }
}
