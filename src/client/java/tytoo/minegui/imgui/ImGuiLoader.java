package tytoo.minegui.imgui;

import lombok.Getter;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.InputHelper;

public class ImGuiLoader {
    @Getter
    private static volatile boolean contextInitialized;
    private static long windowHandle;

    public static void onGlfwInit(long handle) {
        MineGuiCore.loadConfig();
        windowHandle = handle;
        ImGuiContextManager.onGlfwInit(handle);
        ImGuiContextManager.tryInitialize();
        contextInitialized = ImGuiContextManager.isContextInitialized();
    }

    public static void onWindowResize(int width, int height) {
        ImGuiRenderer.onWindowResize(width, height);
    }

    public static void onWindowMoved(int x, int y) {
        ImGuiRenderer.onWindowMoved(x, y);
    }

    public static void onClientStarted() {
        ImGuiContextManager.onClientStarted();
        ImGuiContextManager.tryInitialize();
        contextInitialized = ImGuiContextManager.isContextInitialized();
    }

    public static void onFrameRender() {
        ImGuiRenderer.onFrameRender();
        contextInitialized = ImGuiContextManager.isContextInitialized();
    }

    public static void requestReload() {
        ImGuiContextManager.requestReload();
        contextInitialized = ImGuiContextManager.isContextInitialized();
    }

    public static void reapplyNamespaceStyles() {
        ImGuiRenderer.reapplyNamespaceStyles();
    }

    public static void onMouseScroll(long window, double horizontal, double vertical) {
        if (windowHandle == 0L || window != windowHandle) {
            return;
        }
        ImGuiContextManager.glfw().scrollCallback(window, horizontal, vertical);
    }

    public static void onKeyEvent(long window, int key, int scancode, int action, int modifiers) {
        if (windowHandle == 0L || window != windowHandle) {
            return;
        }
        int normalizedKey = InputHelper.toQwerty(key);
        ImGuiContextManager.glfw().keyCallback(window, normalizedKey, scancode, action, modifiers);
    }

    public static void onCharTyped(long window, int codePoint) {
        if (windowHandle == 0L || window != windowHandle) {
            return;
        }
        ImGuiContextManager.glfw().charCallback(window, codePoint);
    }

    public static void refreshGlobalScale() {
        ImGuiRenderer.refreshGlobalScale();
    }

    public static boolean rebuildFontAtlasTexture() {
        return ImGuiContextManager.rebuildFontAtlasTexture();
    }
}
