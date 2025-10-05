package tytoo.minegui.input;

import imgui.ImGui;
import imgui.ImGuiIO;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.manager.UIManager;

public final class InputRouter {
    private static final InputRouter INSTANCE = new InputRouter();

    private final IntSet pressedMouse = new IntOpenHashSet();
    private final IntSet pressedKeys = new IntOpenHashSet();
    private boolean cursorLockedPrev;
    private int suppressImGuiFrames;
    private boolean forceUnlockUntilRelease;
    private double lastMouseX;
    private double lastMouseY;
    private boolean lastOverWindow;
    private boolean lastGameplayMask;

    private InputRouter() {
    }

    public static InputRouter getInstance() {
        return INSTANCE;
    }

    public boolean shouldPreventLock() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) return false;
        UIManager ui = UIManager.getInstance();
        if (!ui.isAnyWindowVisible()) return false;
        boolean overNow = lastOverWindow;
        return forceUnlockUntilRelease || overNow;
    }

    public boolean shouldMaskImGuiIO() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return false;
        boolean noScreen = mc.currentScreen == null;
        boolean cursorLocked = mc.mouse != null && mc.mouse.isCursorLocked();
        return noScreen && cursorLocked;
    }

    public void clearLatches() {
        pressedMouse.clear();
        pressedKeys.clear();
    }

    private void ensureUnlocked(MinecraftClient mc) {
        if (!mc.mouse.isCursorLocked()) return;
        mc.mouse.unlockCursor();
    }

    private void updateSuppression(MinecraftClient mc) {
        boolean locked = mc.mouse.isCursorLocked();
        if (locked && !cursorLockedPrev) {
            suppressImGuiFrames = 2;
            pressedMouse.clear();
            pressedKeys.clear();
        }
        cursorLockedPrev = locked;
        if (suppressImGuiFrames > 0) {
            suppressImGuiFrames--;
        }
        if (forceUnlockUntilRelease && mc.currentScreen == null && mc.mouse.isCursorLocked()) {
            ensureUnlocked(mc);
        }
    }

    private boolean isInteractionSuppressed(boolean screenOpen, MinecraftClient mc) {
        if (screenOpen || forceUnlockUntilRelease) return false;
        if (mc.mouse.isCursorLocked()) return true;
        return suppressImGuiFrames > 0;
    }

    public void onFrame() {
        MinecraftClient mc = MinecraftClient.getInstance();
        updateSuppression(mc);
        UIManager ui = UIManager.getInstance();
        if (!ui.isAnyWindowVisible()) return;
        boolean screenOpen = mc.currentScreen != null;
        if (screenOpen) return;
        if (forceUnlockUntilRelease) {
            if (mc.mouse.isCursorLocked()) {
                ensureUnlocked(mc);
            }
        }
    }

    public boolean onMouseButton(double mouseX, double mouseY, int button, int action) {
        MinecraftClient mc = MinecraftClient.getInstance();
        UIManager ui = UIManager.getInstance();
        boolean screenOpen = mc.currentScreen != null;
        updateSuppression(mc);
        boolean anyVisible = ui.isAnyWindowVisible();
        if (!anyVisible) {
            pressedMouse.remove(button);
            return false;
        }
        if (isInteractionSuppressed(screenOpen, mc)) {
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        boolean wantMouse = io.getWantCaptureMouse();
        boolean anyItemActiveOrFocused = ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
        boolean overWindow = ui.isPointOverWindow(mouseX, mouseY);
        lastOverWindow = overWindow;

        if (action == GLFW.GLFW_PRESS) {
            if (!screenOpen) {
                boolean capture = overWindow || anyItemActiveOrFocused || wantMouse;
                if (capture) {
                    if (mc.mouse.isCursorLocked()) ensureUnlocked(mc);
                    forceUnlockUntilRelease = true;
                    pressedMouse.add(button);
                    return true;
                }
                ImGui.setWindowFocus(null);
                return false;
            }
            if (wantMouse || anyItemActiveOrFocused) {
                pressedMouse.add(button);
                return true;
            }
            return false;
        } else if (action == GLFW.GLFW_RELEASE) {
            boolean consumed = pressedMouse.remove(button);
            if (pressedMouse.isEmpty() && forceUnlockUntilRelease) {
                forceUnlockUntilRelease = false;
                if (!lastOverWindow) {
                    ImGui.setWindowFocus(null);
                }
                return true;
            }
            return consumed;
        } else {
            return !pressedMouse.isEmpty() || wantMouse || anyItemActiveOrFocused;
        }
    }

    public boolean onMouseMove(double mouseX, double mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        updateSuppression(mc);
        if (isInteractionSuppressed(mc.currentScreen != null, mc)) {
            clearLatches();
            return false;
        }
        UIManager ui = UIManager.getInstance();
        boolean anyVisible = ui.isAnyWindowVisible();
        if (!anyVisible) {
            lastOverWindow = false;
            clearLatches();
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        boolean wantMouse = io.getWantCaptureMouse();
        boolean anyItemActiveOrFocused = ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
        boolean overWindow = ui.isPointOverWindow((int) mouseX, (int) mouseY);
        lastOverWindow = overWindow;
        boolean anyWindowFocused = ui.isAnyWindowFocused();
        boolean allowHover = (mc.currentScreen != null) || anyWindowFocused || overWindow;
        return allowHover && (!pressedMouse.isEmpty() || wantMouse || anyItemActiveOrFocused || overWindow);
    }

    public boolean onScroll(double mouseX, double mouseY, double horizontal, double vertical) {
        MinecraftClient mc = MinecraftClient.getInstance();
        updateSuppression(mc);
        if (isInteractionSuppressed(mc.currentScreen != null, mc)) {
            return false;
        }
        UIManager ui = UIManager.getInstance();
        boolean anyVisible = ui.isAnyWindowVisible();
        if (!anyVisible) return false;
        ImGuiIO io = ImGui.getIO();
        boolean wantMouse = io.getWantCaptureMouse();
        boolean anyItemActiveOrFocused = ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
        boolean overWindow = ui.isPointOverWindow((int) mouseX, (int) mouseY);
        boolean anyWindowFocused = ui.isAnyWindowFocused();
        boolean anyScroll = (horizontal != 0d) || (vertical != 0d);
        boolean allowHover = (mc.currentScreen != null) || anyWindowFocused;
        return allowHover && anyScroll && (anyItemActiveOrFocused || wantMouse || overWindow);
    }

    public boolean onKey(int key, int action) {
        MinecraftClient mc = MinecraftClient.getInstance();
        updateSuppression(mc);

        if (!UIManager.getInstance().isAnyWindowVisible()) {
            pressedKeys.remove(key);
            return false;
        }

        ImGuiIO io = ImGui.getIO();
        final boolean anyItemActiveOrFocused = ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
        final boolean wantText = io.getWantTextInput();

        final boolean wants = anyItemActiveOrFocused || wantText;

        if (action == GLFW.GLFW_PRESS) {
            if (!wants) return false;

            pressedKeys.add(key);
            return true;
        }

        if (action == GLFW.GLFW_RELEASE) {
            if (pressedKeys.remove(key)) return true;
            if (isInteractionSuppressed(mc.currentScreen != null, mc)) return false;

            return wants;
        }

        return !pressedKeys.isEmpty() || wants;
    }

    public boolean onChar() {
        MinecraftClient mc = MinecraftClient.getInstance();
        updateSuppression(mc);
        if (isInteractionSuppressed(mc.currentScreen != null, mc)) {
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        boolean anyVisible = UIManager.getInstance().isAnyWindowVisible();
        if (!anyVisible) return false;
        boolean wantText = io.getWantTextInput();
        boolean anyItemActiveOrFocused = ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
        return anyItemActiveOrFocused || wantText;
    }
}