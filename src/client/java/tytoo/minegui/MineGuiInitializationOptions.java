package tytoo.minegui;

import tytoo.minegui.config.*;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;

import java.util.Set;

@SuppressWarnings("unused")
public record MineGuiInitializationOptions(
        boolean loadGlobalConfig,
        boolean ignoreGlobalConfig,
        ConfigFeatureProfile featureProfile,
        ConfigPathStrategy configPathStrategy,
        ResourceId defaultCursorPolicyId,
        DockspaceCustomizer dockspaceCustomizer,
        NamespaceConfigStore configStore
) {
    public MineGuiInitializationOptions {
        featureProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
        configPathStrategy = configPathStrategy != null ? configPathStrategy : ConfigPathStrategies.sandboxed();
        defaultCursorPolicyId = defaultCursorPolicyId != null ? defaultCursorPolicyId : CursorPolicies.clickToLockId();
        dockspaceCustomizer = dockspaceCustomizer != null ? dockspaceCustomizer : DockspaceCustomizer.noop();
        configStore = configStore != null ? configStore : new GlobalConfigNamespaceConfigStore();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MineGuiInitializationOptions defaults() {
        return builder().build();
    }

    public static MineGuiInitializationOptions skipGlobalConfig() {
        return builder().loadGlobalConfig(false).build();
    }

    public static MineGuiInitializationOptions ignoringGlobalConfig() {
        return builder().loadGlobalConfig(false).ignoreGlobalConfig(true).build();
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore);
    }

    public MineGuiInitializationOptions withIgnoreGlobalConfig(boolean ignoreGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore);
    }

    public MineGuiInitializationOptions withFeatureProfile(ConfigFeatureProfile profile) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, profile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, configStore);
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
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, normalized, dockspaceCustomizer, configStore);
    }

    public MineGuiInitializationOptions withDockspaceCustomizer(DockspaceCustomizer customizer) {
        DockspaceCustomizer normalized = customizer != null ? customizer : DockspaceCustomizer.noop();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, normalized, configStore);
    }

    public MineGuiInitializationOptions withConfigPathStrategy(ConfigPathStrategy strategy) {
        ConfigPathStrategy normalized = strategy != null ? strategy : ConfigPathStrategies.sandboxed();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, featureProfile, normalized, defaultCursorPolicyId, dockspaceCustomizer, configStore);
    }

    public MineGuiInitializationOptions withConfigStore(NamespaceConfigStore store) {
        NamespaceConfigStore normalized = store != null ? store : new GlobalConfigNamespaceConfigStore();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, normalized);
    }

    public static final class Builder {
        private boolean loadGlobalConfig = true;
        private boolean ignoreGlobalConfig;
        private ConfigFeatureProfile featureProfile = ConfigFeatureProfile.all();
        private ConfigPathStrategy configPathStrategy = ConfigPathStrategies.sandboxed();
        private ResourceId defaultCursorPolicyId = CursorPolicies.clickToLockId();
        private DockspaceCustomizer dockspaceCustomizer = DockspaceCustomizer.noop();
        private NamespaceConfigStore configStore = new GlobalConfigNamespaceConfigStore();

        private Builder() {
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

        public MineGuiInitializationOptions build() {
            return new MineGuiInitializationOptions(
                    loadGlobalConfig,
                    ignoreGlobalConfig,
                    featureProfile,
                    configPathStrategy,
                    defaultCursorPolicyId,
                    dockspaceCustomizer,
                    configStore
            );
        }
    }
}
