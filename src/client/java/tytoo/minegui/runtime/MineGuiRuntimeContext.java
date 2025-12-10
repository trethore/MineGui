package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.config.NamespaceConfigStore;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.Objects;

public final class MineGuiRuntimeContext implements MineGuiContext {
    private final MineGuiInitializationOptions options;
    private final NamespaceConfigService config;
    private final UIManager uiManager;
    private final StyleManager styleManager;
    private ResourceId defaultCursorPolicyId;
    private CursorPolicy defaultCursorPolicy;
    private volatile DockspaceCustomizer dockspaceCustomizer;

    public MineGuiRuntimeContext(MineGuiInitializationOptions options) {
        this.options = options;
        NamespaceConfigStore store = options.configStore();
        // Using "main" as default namespace for now
        this.config = new NamespaceConfigService("main", store);
        this.uiManager = UIManager.get("main");
        this.styleManager = StyleManager.get("main");
        StyleManager defaultStyleManager = StyleManager.get(GlobalConfigManager.getDefaultNamespace());
        if (this.styleManager.getGlobalDescriptor().isEmpty()) {
            defaultStyleManager.getGlobalDescriptor()
                    .map(descriptor -> StyleDescriptor.builder().fromDescriptor(descriptor).build())
                    .ifPresent(this.styleManager::setGlobalDescriptor);
        }
        this.defaultCursorPolicyId = options.defaultCursorPolicyId();
        this.defaultCursorPolicy = CursorPolicyRegistry.resolvePolicyOrDefault(defaultCursorPolicyId, CursorPolicies.empty());
        this.uiManager.setDefaultCursorPolicy(defaultCursorPolicy);
        this.dockspaceCustomizer = options.dockspaceCustomizer();
    }

    @Override
    public MineGuiInitializationOptions options() {
        return options;
    }

    @Override
    public NamespaceConfigService config() {
        return config;
    }

    @Override
    public UIManager ui() {
        return uiManager;
    }

    @Override
    public StyleManager style() {
        return styleManager;
    }

    @Override
    public ResourceId defaultCursorPolicyId() {
        return defaultCursorPolicyId;
    }

    @Override
    public CursorPolicy defaultCursorPolicy() {
        return defaultCursorPolicy;
    }

    @Override
    public void setDefaultCursorPolicy(ResourceId policyId) {
        ResourceId normalized = policyId != null ? policyId : CursorPolicies.emptyId();
        CursorPolicy resolved = CursorPolicyRegistry.resolvePolicyOrDefault(normalized, CursorPolicies.empty());
        if (Objects.equals(normalized, defaultCursorPolicyId) && resolved == defaultCursorPolicy) {
            return;
        }
        this.defaultCursorPolicyId = normalized;
        this.defaultCursorPolicy = resolved;
        this.uiManager.setDefaultCursorPolicy(resolved);
    }

    @Override
    public DockspaceCustomizer dockspaceCustomizer() {
        return dockspaceCustomizer;
    }

    @Override
    public void setDockspaceCustomizer(DockspaceCustomizer customizer) {
        dockspaceCustomizer = customizer != null ? customizer : DockspaceCustomizer.noop();
    }
}
