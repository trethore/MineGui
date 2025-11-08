package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicy;

@SuppressWarnings("unused")
public interface MineGuiContext {
    String namespace();

    MineGuiInitializationOptions options();

    NamespaceConfigService config();

    UIManager ui();

    ViewSaveManager viewSaves();

    StyleManager style();

    LayoutApi layout();

    ResourceId defaultCursorPolicyId();

    CursorPolicy defaultCursorPolicy();

    void setDefaultCursorPolicy(ResourceId policyId);

    DockspaceCustomizer dockspaceCustomizer();

    void setDockspaceCustomizer(DockspaceCustomizer customizer);
}
