package tytoo.minegui.input;

import imgui.ImGui;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.runtime.viewport.ViewportInteractionTracker;
import tytoo.minegui.util.CursorLockUtils;
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
            if (action == GLFW.GLFW_PRESS && CursorLockUtils.clientWantsLockCursor()) {
                CursorPolicyRegistry.releaseClickReleaseForWorldInteraction();
            }
            return false;
        }
        if (action == GLFW.GLFW_PRESS) {
            pressedMouse.add(button);
            ViewportInteractionTracker.notifyInteraction();
            return true;
        }
        if (action == GLFW.GLFW_RELEASE) {
            boolean consumed = pressedMouse.remove(button);
            if (consumed) {
                ViewportInteractionTracker.notifyInteraction();
            }
            return consumed;
        }
        boolean captured = !pressedMouse.isEmpty();
        if (captured) {
            ViewportInteractionTracker.notifyInteraction();
        }
        return captured;
    }

    public boolean onMouseMove() {
        boolean captured = wantsMouseInput();
        if (captured) {
            ViewportInteractionTracker.notifyInteraction();
        }
        return captured;
    }

    public boolean onScroll(double horizontal, double vertical) {
        if (!wantsMouseInput()) {
            return false;
        }
        boolean captured = horizontal != 0.0d || vertical != 0.0d;
        if (captured) {
            ViewportInteractionTracker.notifyInteraction();
        }
        return captured;
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
            ViewportInteractionTracker.notifyInteraction();
            return true;
        }
        if (action == GLFW.GLFW_RELEASE) {
            boolean consumed = pressedKeys.remove(normalizedKey);
            if (consumed) {
                ViewportInteractionTracker.notifyInteraction();
            }
            return consumed;
        }
        boolean captured = !pressedKeys.isEmpty();
        if (captured) {
            ViewportInteractionTracker.notifyInteraction();
        }
        return captured;
    }

    public boolean onChar() {
        boolean captured = wantsKeyboardInput();
        if (captured) {
            ViewportInteractionTracker.notifyInteraction();
        }
        return captured;
    }

    private boolean wantsMouseInput() {
        if (!CursorPolicyRegistry.wantsImGuiInput()) {
            return false;
        }
        return MineGuiNamespaces.anyVisible() && imguiWantsMouse();
    }

    private boolean wantsKeyboardInput() {
        if (!CursorPolicyRegistry.wantsImGuiInput()) {
            return false;
        }
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
