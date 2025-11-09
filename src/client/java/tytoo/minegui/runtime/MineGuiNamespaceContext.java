package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.config.NamespaceConfigStore;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.layout.LayoutService;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.Objects;

public final class MineGuiNamespaceContext implements MineGuiContext {
    private final String namespace;
    private final MineGuiInitializationOptions options;
    private final NamespaceConfigService config;
    private final UIManager uiManager;
    private final ViewSaveManager viewSaveManager;
    private final StyleManager styleManager;
    private final LayoutApi layout;
    private ResourceId defaultCursorPolicyId;
    private CursorPolicy defaultCursorPolicy;
    private volatile DockspaceCustomizer dockspaceCustomizer;

    MineGuiNamespaceContext(String namespace, MineGuiInitializationOptions options) {
        this.namespace = namespace;
        this.options = options;
        NamespaceConfigStore store = options.configStore();
        this.config = new NamespaceConfigService(namespace, store);
        this.uiManager = UIManager.get(namespace);
        this.viewSaveManager = ViewSaveManager.get(namespace);
        this.styleManager = StyleManager.get(namespace);
        this.layout = new LayoutService();
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
    public String namespace() {
        return namespace;
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
    public ViewSaveManager viewSaves() {
        return viewSaveManager;
    }

    @Override
    public StyleManager style() {
        return styleManager;
    }

    @Override
    public LayoutApi layout() {
        return layout;
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
