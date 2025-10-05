package tytoo.minegui_debug;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui_debug.windows.TestWindow;

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
        TestWindow testWindow = new TestWindow();
        KeyBinding openTestWindowKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.weave.test"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openTestWindowKeybind != null && openTestWindowKeybind.wasPressed()) {
                testWindow.toggleDependingOnScreen();
                LOGGER.info("Toggled Test Window: {}", testWindow.isVisible() ? "Open" : "Closed");
            }
        });

        UIManager.getInstance().registerWindow(testWindow);
    }
}
