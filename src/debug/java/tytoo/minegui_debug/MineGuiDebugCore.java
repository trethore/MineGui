package tytoo.minegui_debug;

import imgui.flag.ImGuiDockNodeFlags;
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
import tytoo.minegui_debug.view.FeaturesView;
import tytoo.minegui_debug.view.StyleDebugView;
import tytoo.minegui_debug.view.TestView;

@SuppressWarnings("unused")
public final class MineGuiDebugCore {
    public static final String ID = "minegui_debug";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiDebugCore.class);

    private MineGuiDebugCore() {
    }

    public static void init() {
        test();
    }

    private static void test() {
        TestView testView = new TestView();
        FeaturesView featuresView = new FeaturesView();
        StyleDebugView styleView = new StyleDebugView();
        MineGuiNamespaceContext context = MineGuiNamespaces.initialize(
                MineGuiInitializationOptions.defaults(ID)
        );
        MineGuiNamespaces.setDockspaceCustomizer(ID, state -> state.removeDockspaceFlags(ImGuiDockNodeFlags.NoDockingInCentralNode));
        context.ui().register(testView);
        context.ui().register(featuresView);
        context.ui().register(styleView);
        KeyBinding openTestViewKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.weave.test"
        ));
        KeyBinding featuresKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.feature_tour",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.weave.test"
        ));
        KeyBinding styleInspectorKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.style_inspector",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.weave.test"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isValid = client != null && client.currentScreen == null;
            if (!isValid) {
                return;
            }

            if (openTestViewKeybind != null && openTestViewKeybind.wasPressed()) {
                testView.toggleVisibility();
            }
            if (featuresKeybind != null && featuresKeybind.wasPressed()) {
                featuresView.toggleVisibility();
            }
            if (styleInspectorKeybind != null && styleInspectorKeybind.wasPressed()) {
                styleView.toggleVisibility();
            }
        });
    }
}
