package tytoo.minegui.runtime.config;

import tytoo.minegui.config.*;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class NamespaceConfigService {
    private final String namespace;
    private final NamespaceConfigStore store;
    private volatile NamespaceConfig current;

    public NamespaceConfigService(String namespace, NamespaceConfigStore store) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.store = Objects.requireNonNull(store, "store");
        this.current = store.load(namespace);
    }

    public String namespace() {
        return namespace;
    }

    public NamespaceConfig current() {
        return current;
    }

    public synchronized NamespaceConfig reload() {
        current = store.load(namespace);
        return current;
    }

    public synchronized NamespaceConfig update(UnaryOperator<NamespaceConfig> updater) {
        Objects.requireNonNull(updater, "updater");
        NamespaceConfig next = Objects.requireNonNull(updater.apply(current), "updater returned null");
        if (!namespace.equals(next.namespace())) {
            throw new IllegalArgumentException("Cannot set config for namespace '" + next.namespace() + "' on service for '" + namespace + "'");
        }
        current = next;
        store.save(next);
        return current;
    }

    public void save() {
        store.save(current);
    }

    public ConfigFeatureProfile featureProfile() {
        return GlobalConfigManager.getFeatureProfile(namespace);
    }

    public void setFeatureProfile(ConfigFeatureProfile profile) {
        GlobalConfigManager.setFeatureProfile(namespace, profile);
    }

    public boolean shouldLoad(ConfigFeature feature) {
        return GlobalConfigManager.shouldLoadFeature(namespace, feature);
    }

    public boolean shouldSave(ConfigFeature feature) {
        return GlobalConfigManager.shouldSaveFeature(namespace, feature);
    }

    public void enableFeature(ConfigFeature feature) {
        GlobalConfigManager.enableFeature(namespace, feature);
    }

    public void disableFeature(ConfigFeature feature) {
        GlobalConfigManager.disableFeature(namespace, feature);
    }

    public boolean isConfigIgnored() {
        return GlobalConfigManager.isConfigIgnored(namespace);
    }
}
