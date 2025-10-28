package tytoo.minegui.runtime;

import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.runtime.config.NamespaceConfigAccess;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.view.cursor.MGCursorPolicies;
import tytoo.minegui.view.cursor.MGCursorPolicy;

import java.util.Objects;

public final class MineGuiNamespaceContext {
    private final String namespace;
    private final MineGuiInitializationOptions options;
    private final NamespaceConfigAccess config;
    private final UIManager uiManager;
    private final ViewSaveManager viewSaveManager;
    private final StyleManager styleManager;
    private Identifier defaultCursorPolicyId;
    private MGCursorPolicy defaultCursorPolicy;

    MineGuiNamespaceContext(String namespace, MineGuiInitializationOptions options) {
        this.namespace = namespace;
        this.options = options;
        this.config = new NamespaceConfigAccess(namespace);
        this.uiManager = UIManager.get(namespace);
        this.viewSaveManager = ViewSaveManager.get(namespace);
        this.styleManager = StyleManager.get(namespace);
        StyleManager defaultStyleManager = StyleManager.get(GlobalConfigManager.getDefaultNamespace());
        if (this.styleManager.getGlobalDescriptor().isEmpty()) {
            defaultStyleManager.getGlobalDescriptor()
                    .map(descriptor -> MGStyleDescriptor.builder().fromDescriptor(descriptor).build())
                    .ifPresent(this.styleManager::setGlobalDescriptor);
        }
        this.defaultCursorPolicyId = options.defaultCursorPolicyId();
        this.defaultCursorPolicy = CursorPolicyRegistry.resolvePolicyOrDefault(defaultCursorPolicyId, MGCursorPolicies.empty());
        this.uiManager.setDefaultCursorPolicy(defaultCursorPolicy);
    }

    public String namespace() {
        return namespace;
    }

    public MineGuiInitializationOptions options() {
        return options;
    }

    public NamespaceConfigAccess config() {
        return config;
    }

    public UIManager ui() {
        return uiManager;
    }

    public ViewSaveManager viewSaves() {
        return viewSaveManager;
    }

    public StyleManager style() {
        return styleManager;
    }

    public Identifier defaultCursorPolicyId() {
        return defaultCursorPolicyId;
    }

    public MGCursorPolicy defaultCursorPolicy() {
        return defaultCursorPolicy;
    }

    public void setDefaultCursorPolicy(Identifier policyId) {
        Identifier normalized = policyId != null ? policyId : MGCursorPolicies.emptyId();
        MGCursorPolicy resolved = CursorPolicyRegistry.resolvePolicyOrDefault(normalized, MGCursorPolicies.empty());
        if (Objects.equals(normalized, defaultCursorPolicyId) && resolved == defaultCursorPolicy) {
            return;
        }
        this.defaultCursorPolicyId = normalized;
        this.defaultCursorPolicy = resolved;
        this.uiManager.setDefaultCursorPolicy(resolved);
    }
}
