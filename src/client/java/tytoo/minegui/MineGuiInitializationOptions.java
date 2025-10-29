package tytoo.minegui;

import net.minecraft.util.Identifier;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.ConfigFeatureProfile;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.view.cursor.MGCursorPolicies;

import java.util.Set;

public record MineGuiInitializationOptions(
        boolean loadGlobalConfig,
        boolean ignoreGlobalConfig,
        String configNamespace,
        ConfigFeatureProfile featureProfile,
        Identifier defaultCursorPolicyId,
        DockspaceCustomizer dockspaceCustomizer
) {
    public MineGuiInitializationOptions {
        configNamespace = normalizeNamespace(configNamespace);
        featureProfile = featureProfile != null ? featureProfile : ConfigFeatureProfile.all();
        defaultCursorPolicyId = defaultCursorPolicyId != null ? defaultCursorPolicyId : MGCursorPolicies.emptyId();
        dockspaceCustomizer = dockspaceCustomizer != null ? dockspaceCustomizer : DockspaceCustomizer.noop();
    }

    public MineGuiInitializationOptions(
            boolean loadGlobalConfig,
            boolean ignoreGlobalConfig,
            String configNamespace,
            ConfigFeatureProfile featureProfile,
            Identifier defaultCursorPolicyId
    ) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, defaultCursorPolicyId, DockspaceCustomizer.noop());
    }

    public MineGuiInitializationOptions(boolean loadGlobalConfig, String configNamespace) {
        this(loadGlobalConfig, false, configNamespace, ConfigFeatureProfile.all(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop());
    }

    public MineGuiInitializationOptions(boolean loadGlobalConfig, boolean ignoreGlobalConfig, String configNamespace) {
        this(loadGlobalConfig, ignoreGlobalConfig, configNamespace, ConfigFeatureProfile.all(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop());
    }

    public static MineGuiInitializationOptions defaults() {
        return new MineGuiInitializationOptions(true, false, MineGuiCore.ID, ConfigFeatureProfile.all(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop());
    }

    public static MineGuiInitializationOptions skipGlobalConfig() {
        return new MineGuiInitializationOptions(false, false, MineGuiCore.ID, ConfigFeatureProfile.all(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop());
    }

    public static MineGuiInitializationOptions ignoringGlobalConfig() {
        return new MineGuiInitializationOptions(false, true, MineGuiCore.ID, ConfigFeatureProfile.all(), MGCursorPolicies.emptyId(), DockspaceCustomizer.noop());
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
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, namespace, featureProfile, defaultCursorPolicyId, dockspaceCustomizer);
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, defaultCursorPolicyId, dockspaceCustomizer);
    }

    public MineGuiInitializationOptions withIgnoreGlobalConfig(boolean ignoreGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, defaultCursorPolicyId, dockspaceCustomizer);
    }

    public MineGuiInitializationOptions withFeatureProfile(ConfigFeatureProfile profile) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, profile, defaultCursorPolicyId, dockspaceCustomizer);
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
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, normalized, dockspaceCustomizer);
    }

    public MineGuiInitializationOptions withDockspaceCustomizer(DockspaceCustomizer customizer) {
        DockspaceCustomizer normalized = customizer != null ? customizer : DockspaceCustomizer.noop();
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace, featureProfile, defaultCursorPolicyId, normalized);
    }
}
