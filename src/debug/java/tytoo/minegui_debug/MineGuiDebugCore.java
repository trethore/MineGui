package tytoo.minegui_debug;

import imgui.ImGui;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui_debug.view.StyleDebugView;
import tytoo.minegui_debug.view.TestView;

@SuppressWarnings("unused")
public final class MineGuiDebugCore {
    public static String ID = "minegui_debug";
    public static Logger LOGGER = LoggerFactory.getLogger(MineGuiDebugCore.class);

    private MineGuiDebugCore() {
    }

    public static void init() {
        test();
    }

    private static void test() {
        TestView testView = new TestView();
        StyleDebugView styleView = new StyleDebugView();
        MineGuiNamespaceContext context = MineGuiNamespaces.initialize(
                MineGuiInitializationOptions.defaults().withNamespace(ID)
        );
        MineGuiNamespaces.setDockspaceCustomizer(ID, state -> {
            state.addWindowFlags(ImGuiWindowFlags.MenuBar);
            state.removeDockspaceFlags(ImGuiDockNodeFlags.NoDockingInCentralNode);
            state.beforeDockspace(() -> {
                if (ImGui.beginMenuBar()) {
                    if (ImGui.beginMenu("MineGui")) {
                        if (ImGui.menuItem("Toggle Test View")) {
                            testView.toggleVisibility();
                        }
                        if (ImGui.menuItem("Toggle Style Inspector")) {
                            styleView.toggleVisibility();
                        }
                        ImGui.endMenu();
                    }
                    ImGui.endMenuBar();
                }
            });
        });
        context.ui().register(testView);
        context.ui().register(styleView);
        KeyBinding openTestViewKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.weave.test"
        ));
        KeyBinding styleInspectorKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.style_inspector",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.weave.test"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openTestViewKeybind != null && openTestViewKeybind.wasPressed()) {
                if (client != null && client.currentScreen == null) {
                    testView.toggleVisibility();
                }
                LOGGER.info("Toggled Test View: {}", testView.isVisible() ? "Open" : "Closed");
            }
            if (styleInspectorKeybind != null && styleInspectorKeybind.wasPressed()) {
                if (client != null && client.currentScreen == null) {
                    styleView.toggleVisibility();
                }
                LOGGER.info("Toggled Style Inspector: {}", styleView.isVisible() ? "Open" : "Closed");
            }
        });
    }
}
