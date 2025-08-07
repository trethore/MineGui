package tytoo.minegui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.windows.TestWindow;

public class MineGuiClient implements ClientModInitializer {
    public static final String MOD_ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            test();
        }
        LOGGER.info("MineGui Client Initialized");

    }

    private void test() {
        TestWindow testWindow = new TestWindow();
        KeyBinding openTestWindowKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.weave.test"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openTestWindowKeybind != null && openTestWindowKeybind.wasPressed()) {
                testWindow.toggle();
                LOGGER.info("Toggled Test Window: {}", testWindow.isVisible() ? "Open" : "Closed");
            }
        });

        UIManager.getInstance().registerWindow(testWindow);
    }
}