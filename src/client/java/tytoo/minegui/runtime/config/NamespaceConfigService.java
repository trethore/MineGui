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
        this.current = Objects.requireNonNull(store.load(namespace), "store returned null config");
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

    public synchronized void save() {
        store.save(current);
    }

    public ConfigFeatureProfile featureProfile() {
        return configService().featureProfile();
    }

    public void setFeatureProfile(ConfigFeatureProfile profile) {
        configService().setFeatureProfile(profile);
    }

    public boolean shouldLoad(ConfigFeature feature) {
        return configService().shouldLoadFeature(feature);
    }

    public boolean shouldSave(ConfigFeature feature) {
        return configService().shouldSaveFeature(feature);
    }

    public void enableFeature(ConfigFeature feature) {
        configService().enableFeature(feature);
    }

    public void disableFeature(ConfigFeature feature) {
        configService().disableFeature(feature);
    }

    public boolean isConfigIgnored() {
        return configService().isConfigIgnored();
    }

    private ConfigService configService() {
        return ConfigRegistry.get(namespace);
    }
}
