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

    public MineGuiInitializationOptions(
            boolean loadGlobalConfig,
            boolean ignoreGlobalConfig,
            String configNamespace,
            ConfigFeatureProfile featureProfile,
            ConfigPathStrategy configPathStrategy,
            Identifier defaultCursorPolicyId,
            DockspaceCustomizer dockspaceCustomizer
    ) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, dockspaceCustomizer, null);
    }

    public MineGuiInitializationOptions(
            boolean loadGlobalConfig,
            boolean ignoreGlobalConfig,
            String configNamespace,
            ConfigFeatureProfile featureProfile,
            ConfigPathStrategy configPathStrategy,
            Identifier defaultCursorPolicyId
    ) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, DockspaceCustomizer.noop(), null);
    }

    public MineGuiInitializationOptions(
            boolean loadGlobalConfig,
            boolean ignoreGlobalConfig,
            String configNamespace,
            ConfigFeatureProfile featureProfile,
            Identifier defaultCursorPolicyId
    ) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, ConfigPathStrategies.sandboxed(), defaultCursorPolicyId, DockspaceCustomizer.noop(), null);
    }

    public MineGuiInitializationOptions(
            boolean loadGlobalConfig,
            boolean ignoreGlobalConfig,
            String configNamespace,
            ConfigFeatureProfile featureProfile,
            ConfigPathStrategy configPathStrategy,
            Identifier defaultCursorPolicyId,
            ViewPersistenceAdapter viewPersistenceAdapter
    ) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, configPathStrategy, defaultCursorPolicyId, DockspaceCustomizer.noop(), viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions(
            boolean loadGlobalConfig,
            boolean ignoreGlobalConfig,
            String configNamespace,
            ConfigFeatureProfile featureProfile,
            Identifier defaultCursorPolicyId,
            ViewPersistenceAdapter viewPersistenceAdapter
    ) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, ConfigPathStrategies.sandboxed(), defaultCursorPolicyId, DockspaceCustomizer.noop(), viewPersistenceAdapter);
    }

    public MineGuiInitializationOptions(boolean loadGlobalConfig, String configNamespace) {
        this(loadGlobalConfig, false, configNamespace, ConfigFeatureProfile.all(), ConfigPathStrategies.sandboxed(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop(), null);
    }

    public MineGuiInitializationOptions(boolean loadGlobalConfig, boolean ignoreGlobalConfig, String configNamespace) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, ConfigFeatureProfile.all(), ConfigPathStrategies.sandboxed(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop(), null);
    }

    public static MineGuiInitializationOptions defaults() {
        return new MineGuiInitializationOptions(true, false, MineGuiCore.ID, ConfigFeatureProfile.all(), ConfigPathStrategies.sandboxed(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop(), null);
    }

    public static MineGuiInitializationOptions skipGlobalConfig() {
        return new MineGuiInitializationOptions(false, false, MineGuiCore.ID, ConfigFeatureProfile.all(), ConfigPathStrategies.sandboxed(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop(), null);
    }

    public static MineGuiInitializationOptions ignoringGlobalConfig() {
        return new MineGuiInitializationOptions(false, true, MineGuiCore.ID, ConfigFeatureProfile.all(), ConfigPathStrategies.sandboxed(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop(), null);
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
}
