package tytoo.minegui.input;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiHoveredFlags;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.manager.UIManager;
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

    public void onFrame() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.mouse == null) {
            return;
        }
        if (!CursorLockUtils.clientWantsLockCursor()) {
            return;
        }
        if (shouldPreventLock() && mc.mouse.isCursorLocked()) {
            CursorLockUtils.applyCursorLock(mc.mouse, false);
        }
    }

    public boolean shouldPreventLock() {
        if (!CursorLockUtils.clientWantsLockCursor()) {
            return false;
        }
        return wantsMouseInput();
    }

    public boolean onMouseButton(int button, int action) {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean wantsMouse = wantsMouseInput();
        if (!wantsMouse) {
            if (action == GLFW.GLFW_RELEASE) {
                pressedMouse.remove(button);
            }
            return false;
        }
        if (action == GLFW.GLFW_PRESS) {
            pressedMouse.add(button);
            if (mc != null && mc.mouse != null && mc.mouse.isCursorLocked()) {
                CursorLockUtils.applyCursorLock(mc.mouse, false);
            }
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
        boolean wantsMouse = wantsMouseInput();
        if (!wantsMouse) {
            return false;
        }
        return horizontal != 0.0d || vertical != 0.0d;
    }

    public boolean onKey(int key, int action) {
        int normalizedKey = InputHelper.toQwerty(key);
        if (normalizedKey == GLFW.GLFW_KEY_UNKNOWN) {
            normalizedKey = key;
        }
        boolean wantsKeyboard = wantsKeyboardInput();
        if (!wantsKeyboard) {
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
        if (!UIManager.getInstance().hasViews()) {
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        return io.getWantCaptureMouse()
                || ImGui.isAnyItemActive()
                || ImGui.isAnyItemFocused()
                || ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow);
    }

    private boolean wantsKeyboardInput() {
        if (!UIManager.getInstance().hasViews()) {
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        return io.getWantCaptureKeyboard()
                || io.getWantTextInput()
                || ImGui.isAnyItemActive()
                || ImGui.isAnyItemFocused();
    }
}
