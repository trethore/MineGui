package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.runtime.config.NamespaceConfigAccess;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.style.StyleManager;

public final class MineGuiNamespaceContext {
    private final String namespace;
    private final MineGuiInitializationOptions options;
    private final NamespaceConfigAccess config;
    private final UIManager uiManager;
    private final ViewSaveManager viewSaveManager;
    private final StyleManager styleManager;

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
}
