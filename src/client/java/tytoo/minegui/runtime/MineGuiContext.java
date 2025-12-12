package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicy;
import tytoo.minegui.view.persistence.ViewPersistenceManager;

@SuppressWarnings("unused")
public interface MineGuiContext {
    MineGuiInitializationOptions options();

    NamespaceConfigService config();

    UIManager ui();

    StyleManager style();

    ViewPersistenceManager persistence();

    ResourceId defaultCursorPolicyId();

    CursorPolicy defaultCursorPolicy();

    void setDefaultCursorPolicy(ResourceId policyId);

    DockspaceCustomizer dockspaceCustomizer();

    void setDockspaceCustomizer(DockspaceCustomizer customizer);
}
