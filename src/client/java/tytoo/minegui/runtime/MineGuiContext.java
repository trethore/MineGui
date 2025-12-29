package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiOptions;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.config.PersistenceFlags;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public interface MineGuiContext {

    String namespace();

    MineGuiOptions options();

    NamespaceConfig config();

    void updateConfig(UnaryOperator<NamespaceConfig> updater);

    PersistenceFlags persistence();

    void save();

    void load();

    UIManager ui();

    StyleManager style();

    ResourceId cursorPolicyId();

    CursorPolicy cursorPolicy();

    void setCursorPolicy(ResourceId policyId);

    DockspaceCustomizer dockspaceCustomizer();

    void setDockspaceCustomizer(DockspaceCustomizer customizer);

    void addLifecycleListener(MineGuiLifecycleListener listener);

    void removeLifecycleListener(MineGuiLifecycleListener listener);
}
