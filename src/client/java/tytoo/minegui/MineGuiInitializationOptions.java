package tytoo.minegui;

import tytoo.minegui.config.*;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.persistence.DefaultViewPersistenceAdapter;
import tytoo.minegui.view.persistence.ViewPersistenceAdapter;

import java.nio.file.Path;
import java.util.Set;

@SuppressWarnings("unused")
public record MineGuiInitializationOptions(
        String namespace,
        Path configRoot,
        boolean loadGlobalConfig,
        boolean ignoreGlobalConfig,
        ConfigFeatureProfile featureProfile,
        ConfigPathStrategy configPathStrategy,
        ResourceId defaultCursorPolicyId,
        DockspaceCustomizer dockspaceCustomizer,
        NamespaceConfigStore configStore,
        ViewPersistenceAdapter viewPersistenceAdapter
) {
    public MineGuiInitializationOptions {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Namespace must be non-blank");
        }
        if (MineGuiCore.ID.equals(namespace)) {
            throw new IllegalArgumentException("Namespace '" + MineGuiCore.ID + "' is reserved for MineGui internals");
        }
        featureProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
        configPathStrategy = configPathStrategy != null ? configPathStrategy : ConfigPathStrategies.sandboxed();
        defaultCursorPolicyId = defaultCursorPolicyId != null ? defaultCursorPolicyId : CursorPolicies.clickToLockId();
        dockspaceCustomizer = dockspaceCustomizer != null ? dockspaceCustomizer : DockspaceCustomizer.noop();
        configStore = configStore != null ? configStore : new GlobalConfigNamespaceConfigStore();
        if (viewPersistenceAdapter == null) {
            viewPersistenceAdapter = new DefaultViewPersistenceAdapter(GlobalConfigManager.getViewSavesDirectory(namespace));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MineGuiInitializationOptions defaults(String namespace) {
        return builder().namespace(namespace).build();
    }

    public static MineGuiInitializationOptions skipGlobalConfig(String namespace) {
        return builder().namespace(namespace).loadGlobalConfig(false).build();
    }

    public static MineGuiInitializationOptions ignoringGlobalConfig(String namespace) {
        return builder().namespace(namespace).loadGlobalConfig(false).ignoreGlobalConfig(true).build();
    }

    public MineGuiInitializationOptions withNamespace(String namespace) {
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withConfigRoot(Path configRoot) {
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withIgnoreGlobalConfig(boolean ignoreGlobalConfig) {
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withFeatureProfile(ConfigFeatureProfile profile) {
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, profile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore, viewPersistenceAdapter);
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
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, normalized, dockspaceCustomizer, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withDockspaceCustomizer(DockspaceCustomizer customizer) {
        DockspaceCustomizer normalized = customizer != null ? customizer : DockspaceCustomizer.noop();
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, normalized, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withConfigPathStrategy(ConfigPathStrategy strategy) {
        ConfigPathStrategy normalized = strategy != null ? strategy : ConfigPathStrategies.sandboxed();
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, normalized, defaultCursorPolicyId, dockspaceCustomizer, configStore, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withConfigStore(NamespaceConfigStore store) {
        NamespaceConfigStore normalized = store != null ? store : new GlobalConfigNamespaceConfigStore();
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, normalized, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withViewPersistenceAdapter(ViewPersistenceAdapter adapter) {
        ViewPersistenceAdapter normalized = adapter != null ? adapter : new DefaultViewPersistenceAdapter(GlobalConfigManager.getViewSavesDirectory(namespace));
        return new MineGuiInitializationOptions(namespace, configRoot, loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore, normalized);
    }

    public static final class Builder {
        private String namespace;
        private Path configRoot;
        private boolean loadGlobalConfig = true;
        private boolean ignoreGlobalConfig;
        private ConfigFeatureProfile featureProfile = ConfigFeatureProfile.all();
        private ConfigPathStrategy configPathStrategy = ConfigPathStrategies.sandboxed();
        private ResourceId defaultCursorPolicyId = CursorPolicies.clickToLockId();
        private DockspaceCustomizer dockspaceCustomizer = DockspaceCustomizer.noop();
        private NamespaceConfigStore configStore = new GlobalConfigNamespaceConfigStore();
        private ViewPersistenceAdapter viewPersistenceAdapter;

        private Builder() {
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder configRoot(Path configRoot) {
            this.configRoot = configRoot;
            return this;
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

        public Builder configStore(NamespaceConfigStore store) {
            this.configStore = store != null ? store : new GlobalConfigNamespaceConfigStore();
            return this;
        }

        public Builder viewPersistenceAdapter(ViewPersistenceAdapter adapter) {
            this.viewPersistenceAdapter = adapter;
            return this;
        }

        public MineGuiInitializationOptions build() {
            return new MineGuiInitializationOptions(
                    namespace,
                    configRoot,
                    loadGlobalConfig,
                    ignoreGlobalConfig,
                    featureProfile,
                    configPathStrategy,
                    defaultCursorPolicyId,
                    dockspaceCustomizer,
                    configStore,
                    viewPersistenceAdapter
            );
        }
    }
}
