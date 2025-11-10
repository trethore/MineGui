package tytoo.minegui_debug;

import imgui.flag.ImGuiDockNodeFlags;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui_debug.view.PlaygroundView;

import java.util.Objects;

@SuppressWarnings("unused")
public final class MineGuiDebugCore {
    public static final String ID = "minegui_debug";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiDebugCore.class);

    private MineGuiDebugCore() {
    }

    public static void init(MineGuiContext context) {
        Objects.requireNonNull(context, "context");
        test(context);
    }

    private static void test(MineGuiContext context) {
        PlaygroundView playgroundView = new PlaygroundView();
        context.setDockspaceCustomizer(state -> state.removeDockspaceFlags(ImGuiDockNodeFlags.NoDockingInCentralNode));
        context.ui().register(playgroundView);
        KeyBinding playgroundKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minegui.playground",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.minegui.playground"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isValid = client != null && client.currentScreen == null;
            if (!isValid) {
                return;
            }

            if (playgroundKeybind != null && playgroundKeybind.wasPressed()) {
                playgroundView.toggleVisibility();
            }
        });
    }
}
