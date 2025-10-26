package tytoo.minegui.input;

import imgui.ImGui;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.util.InputHelper;

public final class InputRouter {
    private static final InputRouter INSTANCE = new InputRouter();

    private final IntSet pressedMouse = new IntOpenHashSet();
    private final IntSet pressedKeys = new IntOpenHashSet();

    private InputRouter() {
    }

    public static InputRouter getInstance() {
        return INSTANCE;
    }

    public boolean onMouseButton(int button, int action) {
        if (!wantsMouseInput()) {
            if (action == GLFW.GLFW_RELEASE) {
                pressedMouse.remove(button);
            }
            return false;
        }
        if (action == GLFW.GLFW_PRESS) {
            pressedMouse.add(button);
            return true;
        }
        if (action == GLFW.GLFW_RELEASE) {
            return pressedMouse.remove(button);
        }
        return !pressedMouse.isEmpty();
    }

    public boolean onMouseMove() {
        return wantsMouseInput();
    }

    public boolean onScroll(double horizontal, double vertical) {
        if (!wantsMouseInput()) {
            return false;
        }
        return horizontal != 0.0d || vertical != 0.0d;
    }

    public boolean onKey(int key, int action) {
        int normalizedKey = InputHelper.toQwerty(key);
        if (normalizedKey == GLFW.GLFW_KEY_UNKNOWN) {
            normalizedKey = key;
        }
        if (!wantsKeyboardInput()) {
            if (action == GLFW.GLFW_RELEASE) {
                pressedKeys.remove(normalizedKey);
            }
            return false;
        }
        if (action == GLFW.GLFW_PRESS) {
            pressedKeys.add(normalizedKey);
            return true;
        }
        if (action == GLFW.GLFW_RELEASE) {
            return pressedKeys.remove(normalizedKey);
        }
        return !pressedKeys.isEmpty();
    }

    public boolean onChar() {
        return wantsKeyboardInput();
    }

    private boolean wantsMouseInput() {
        return MineGuiNamespaces.anyVisible() && imguiWantsMouse();
    }

    private boolean wantsKeyboardInput() {
        return MineGuiNamespaces.anyVisible() && imguiWantsKeyboard();
    }

    private boolean imguiWantsMouse() {
        return ImGui.getCurrentContext() != null && ImGui.getIO().getWantCaptureMouse();
    }

    private boolean imguiWantsKeyboard() {
        if (ImGui.getCurrentContext() == null) {
            return false;
        }
        return ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantTextInput();
    }
}
