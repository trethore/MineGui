package tytoo.minegui;

import tytoo.minegui.config.*;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.persistence.ViewPersistenceAdapter;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;

import java.util.Set;

@SuppressWarnings("unused")
public record MineGuiInitializationOptions(
        boolean loadGlobalConfig,
        boolean ignoreGlobalConfig,
        String configNamespace,
        ConfigFeatureProfile featureProfile,
        ConfigPathStrategy configPathStrategy,
        ResourceId defaultCursorPolicyId,
        DockspaceCustomizer dockspaceCustomizer,
        ViewPersistenceAdapter viewPersistenceAdapter,
        NamespaceConfigStore configStore
) {
    public MineGuiInitializationOptions {
        configNamespace = normalizeNamespace(configNamespace);
        featureProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
        configPathStrategy = configPathStrategy != null ? configPathStrategy : ConfigPathStrategies.sandboxed();
        defaultCursorPolicyId = defaultCursorPolicyId != null ? defaultCursorPolicyId : CursorPolicies.clickToLockId();
        dockspaceCustomizer = dockspaceCustomizer != null ? dockspaceCustomizer : DockspaceCustomizer.noop();
        configStore = configStore != null ? configStore : new MemoryNamespaceConfigStore();
    }

    public static Builder builder(String namespace) {
        return new Builder(namespace);
    }

    public static MineGuiInitializationOptions defaults(String namespace) {
        return builder(namespace).build();
    }

    public static MineGuiInitializationOptions skipGlobalConfig(String namespace) {
        return builder(namespace).loadGlobalConfig(false).build();
    }

    public static MineGuiInitializationOptions ignoringGlobalConfig(String namespace) {
        return builder(namespace).loadGlobalConfig(false).ignoreGlobalConfig(true).build();
    }

    private static String normalizeNamespace(String namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("MineGui namespace cannot be null");
        }
        String trimmed = namespace.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("MineGui namespace cannot be blank");
        }
        return trimmed;
    }

    public MineGuiInitializationOptions withNamespace(String namespace) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, namespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withIgnoreGlobalConfig(boolean ignoreGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withFeatureProfile(ConfigFeatureProfile profile) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, profile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withLoadFeatures(Set<ConfigFeature> features) {
        return withFeatureProfile(featureProfile.withLoadFeatures(features));
    }

    public MineGuiInitializationOptions withSaveFeatures(Set<ConfigFeature> features) {
        return withFeatureProfile(featureProfile.withSaveFeatures(features));
    }

    public MineGuiInitializationOptions withFeature(ConfigFeature feature) {
        return withFeatureProfile(featureProfile.withFeature(feature));
    }

    public MineGuiInitializationOptions withoutFeature(ConfigFeature feature) {
        return withFeatureProfile(featureProfile.withoutFeature(feature));
    }

    public MineGuiInitializationOptions withDefaultCursorPolicy(ResourceId policyId) {
        ResourceId normalized = policyId != null ? policyId : CursorPolicies.clickToLockId();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, normalized, dockspaceCustomizer, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withDockspaceCustomizer(DockspaceCustomizer customizer) {
        DockspaceCustomizer normalized = customizer != null ? customizer : DockspaceCustomizer.noop();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, normalized, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withConfigPathStrategy(ConfigPathStrategy strategy) {
        ConfigPathStrategy normalized = strategy != null ? strategy : ConfigPathStrategies.sandboxed();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, normalized, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter, configStore);
    }

    public MineGuiInitializationOptions withViewPersistenceAdapter(ViewPersistenceAdapter adapter) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, adapter, configStore);
    }

    public MineGuiInitializationOptions withConfigStore(NamespaceConfigStore store) {
        NamespaceConfigStore normalized = store != null ? store : new MemoryNamespaceConfigStore();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter, normalized);
    }

    public static final class Builder {
        private final String namespace;
        private boolean loadGlobalConfig = true;
        private boolean ignoreGlobalConfig;
        private ConfigFeatureProfile featureProfile = ConfigFeatureProfile.all();
        private ConfigPathStrategy configPathStrategy = ConfigPathStrategies.sandboxed();
        private ResourceId defaultCursorPolicyId = CursorPolicies.clickToLockId();
        private DockspaceCustomizer dockspaceCustomizer = DockspaceCustomizer.noop();
        private ViewPersistenceAdapter viewPersistenceAdapter;
        private NamespaceConfigStore configStore = new MemoryNamespaceConfigStore();

        private Builder(String namespace) {
            this.namespace = normalizeNamespace(namespace);
        }

        public Builder loadGlobalConfig(boolean value) {
            this.loadGlobalConfig = value;
            return this;
        }

        public Builder ignoreGlobalConfig(boolean value) {
            this.ignoreGlobalConfig = value;
            return this;
        }

        public Builder featureProfile(ConfigFeatureProfile profile) {
            this.featureProfile = profile;
            return this;
        }

        public Builder loadFeatures(Set<ConfigFeature> features) {
            ConfigFeatureProfile baseProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
            this.featureProfile = baseProfile.withLoadFeatures(features);
            return this;
        }

        public Builder saveFeatures(Set<ConfigFeature> features) {
            ConfigFeatureProfile baseProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
            this.featureProfile = baseProfile.withSaveFeatures(features);
            return this;
        }

        public Builder enableFeature(ConfigFeature feature) {
            ConfigFeatureProfile baseProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
            this.featureProfile = baseProfile.withFeature(feature);
            return this;
        }

        public Builder disableFeature(ConfigFeature feature) {
            ConfigFeatureProfile baseProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
            this.featureProfile = baseProfile.withoutFeature(feature);
            return this;
        }

        public Builder configPathStrategy(ConfigPathStrategy strategy) {
            this.configPathStrategy = strategy;
            return this;
        }

        public Builder defaultCursorPolicyId(ResourceId policyId) {
            this.defaultCursorPolicyId = policyId;
            return this;
        }

        public Builder dockspaceCustomizer(DockspaceCustomizer customizer) {
            this.dockspaceCustomizer = customizer;
            return this;
        }

        public Builder viewPersistenceAdapter(ViewPersistenceAdapter adapter) {
            this.viewPersistenceAdapter = adapter;
            return this;
        }

        public Builder configStore(NamespaceConfigStore store) {
            this.configStore = store;
            return this;
        }

        public MineGuiInitializationOptions build() {
            return new MineGuiInitializationOptions(
                    loadGlobalConfig,
                    ignoreGlobalConfig,
                    namespace,
                    featureProfile,
                    configPathStrategy,
                    defaultCursorPolicyId,
                    dockspaceCustomizer,
                    viewPersistenceAdapter,
                    configStore
            );
        }
    }
}
