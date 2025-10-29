package tytoo.minegui;

import net.minecraft.util.Identifier;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.ConfigFeatureProfile;
import tytoo.minegui.config.ConfigPathStrategies;
import tytoo.minegui.config.ConfigPathStrategy;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.persistence.ViewPersistenceAdapter;
import tytoo.minegui.view.cursor.MGCursorPolicies;

import java.util.Set;

public record MineGuiInitializationOptions(
        boolean loadGlobalConfig,
        boolean ignoreGlobalConfig,
        String configNamespace,
        ConfigFeatureProfile featureProfile,
        ConfigPathStrategy configPathStrategy,
        Identifier defaultCursorPolicyId,
        DockspaceCustomizer dockspaceCustomizer,
        ViewPersistenceAdapter viewPersistenceAdapter
) {
    public MineGuiInitializationOptions {
        configNamespace = normalizeNamespace(configNamespace);
        featureProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
        configPathStrategy = configPathStrategy != null ? configPathStrategy : ConfigPathStrategies.sandboxed();
        defaultCursorPolicyId = defaultCursorPolicyId != null ? defaultCursorPolicyId : MGCursorPolicies.emptyId();
        dockspaceCustomizer = dockspaceCustomizer != null ? dockspaceCustomizer : DockspaceCustomizer.noop();
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
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, namespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withIgnoreGlobalConfig(boolean ignoreGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withFeatureProfile(ConfigFeatureProfile profile) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, profile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter);
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

    public MineGuiInitializationOptions withDefaultCursorPolicy(Identifier policyId) {
        Identifier normalized = policyId != null ? policyId : MGCursorPolicies.emptyId();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, normalized, dockspaceCustomizer, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withDockspaceCustomizer(DockspaceCustomizer customizer) {
        DockspaceCustomizer normalized = customizer != null ? customizer : DockspaceCustomizer.noop();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, normalized, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withConfigPathStrategy(ConfigPathStrategy strategy) {
        ConfigPathStrategy normalized = strategy != null ? strategy : ConfigPathStrategies.sandboxed();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, normalized, defaultCursorPolicyId, dockspaceCustomizer, viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions withViewPersistenceAdapter(ViewPersistenceAdapter adapter) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, adapter);
    }

    public static final class Builder {
        private final String namespace;
        private boolean loadGlobalConfig = true;
        private boolean ignoreGlobalConfig;
        private ConfigFeatureProfile featureProfile = ConfigFeatureProfile.all();
        private ConfigPathStrategy configPathStrategy = ConfigPathStrategies.sandboxed();
        private Identifier defaultCursorPolicyId = MGCursorPolicies.emptyId();
        private DockspaceCustomizer dockspaceCustomizer = DockspaceCustomizer.noop();
        private ViewPersistenceAdapter viewPersistenceAdapter;

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

        public Builder defaultCursorPolicyId(Identifier policyId) {
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

        public MineGuiInitializationOptions build() {
            return new MineGuiInitializationOptions(
                    loadGlobalConfig,
                    ignoreGlobalConfig,
                    namespace,
                    featureProfile,
                    configPathStrategy,
                    defaultCursorPolicyId,
                    dockspaceCustomizer,
                    viewPersistenceAdapter
            );
        }
    }
}
