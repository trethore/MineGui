package tytoo.minegui.runtime.config;

import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.ConfigFeatureProfile;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;

import java.nio.file.Path;
import java.util.Set;

public record NamespaceConfigAccess(String namespace) {

    public GlobalConfig get() {
        return GlobalConfigManager.getConfig(namespace);
    }

    public void load() {
        GlobalConfigManager.load(namespace);
    }

    public void save() {
        GlobalConfigManager.save(namespace);
    }

    public void reset() {
        GlobalConfigManager.reset(namespace);
    }

    public Path configPath() {
        return GlobalConfigManager.getActiveConfigPath(namespace);
    }

    public Path viewSavesPath() {
        return GlobalConfigManager.getViewSavesDirectory(namespace);
    }

    public ConfigFeatureProfile featureProfile() {
        return GlobalConfigManager.getFeatureProfile(namespace);
    }

    public void setFeatureProfile(ConfigFeatureProfile profile) {
        GlobalConfigManager.setFeatureProfile(namespace, profile);
    }

    public void setLoadFeatures(Set<ConfigFeature> features) {
        GlobalConfigManager.setLoadFeatures(namespace, features);
    }

    public void setSaveFeatures(Set<ConfigFeature> features) {
        GlobalConfigManager.setSaveFeatures(namespace, features);
    }

    public void enableFeature(ConfigFeature feature) {
        GlobalConfigManager.enableFeature(namespace, feature);
    }

    public void disableFeature(ConfigFeature feature) {
        GlobalConfigManager.disableFeature(namespace, feature);
    }

    public boolean shouldLoad(ConfigFeature feature) {
        return GlobalConfigManager.shouldLoadFeature(namespace, feature);
    }

    public boolean shouldSave(ConfigFeature feature) {
        return GlobalConfigManager.shouldSaveFeature(namespace, feature);
    }
}
