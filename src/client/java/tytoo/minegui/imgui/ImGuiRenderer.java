package tytoo.minegui.imgui;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.imgui.dock.DockspaceRenderState;
import tytoo.minegui.runtime.MineGuiRuntimeContext;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.FontLibrary;
import tytoo.minegui.style.Fonts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ImGuiRenderer {
    private static float appliedGlobalScale = Float.NaN;
    private static int mcWindowWidth;
    private static int mcWindowHeight;
    private static int mcWindowX;
    private static int mcWindowY;

    private ImGuiRenderer() {
    }

    public static void onWindowResize(int width, int height) {
        mcWindowWidth = width;
        mcWindowHeight = height;
    }

    public static void onWindowMoved(int x, int y) {
        mcWindowX = x;
        mcWindowY = y;
    }

    static void resetAppliedGlobalScale() {
        appliedGlobalScale = Float.NaN;
    }

    public static void onFrameRender() {
        if (!MineGuiCore.isInitialized()) {
            return;
        }
        if (!ImGuiContextManager.tryInitialize()) {
            return;
        }
        ensureDefaultFont();
        ImGuiContextManager.glfw().newFrame();
        CursorPolicyRegistry.onFrameStart();
        ImGui.newFrame();
        NamespaceConfig defaultConfig = ImGuiContextManager.resolveDefaultConfig();
        applyGlobalScale(defaultConfig);
        renderDockSpace(defaultConfig);

        List<MineGuiRuntimeContext> contexts = new ArrayList<>(MineGuiCore.getAllContexts());
        contexts.sort(Comparator.comparing(ctx -> ctx.options().namespace()));

        for (MineGuiRuntimeContext context : contexts) {
            NamespaceConfig config = context.config().current();
            applyGlobalScale(config);
            context.style().apply();
            context.firePreRender();
            context.ui().render();
            context.firePostRender();
        }

        ImGui.render();
        endFrame();
    }

    public static void reapplyNamespaceStyles() {
        for (MineGuiRuntimeContext context : MineGuiCore.getAllContexts()) {
            context.style().apply();
        }
    }

    public static void refreshGlobalScale() {
        applyGlobalScale(ImGuiContextManager.resolveDefaultConfig());
    }

    static void applyGlobalScale(NamespaceConfig config) {
        if (config == null) {
            return;
        }
        if (!ContextGuard.hasValidContext()) {
            return;
        }
        float configuredScale = config.globalScale();
        if (!Float.isFinite(configuredScale) || configuredScale <= 0.0f) {
            configuredScale = 1.0f;
        }
        if (Float.compare(configuredScale, appliedGlobalScale) == 0) {
            return;
        }
        ImGui.getIO().setFontGlobalScale(configuredScale);
        appliedGlobalScale = configuredScale;
    }

    private static void ensureDefaultFont() {
        if (!ImGuiContextManager.isContextInitialized()) {
            return;
        }
        ImFont current = ImGui.getFont();
        if (current != null && current.isValidPtr()) {
            try {
                if (current.isLoaded()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        ImFont fallback = Fonts.ensure(FontLibrary.getInstance().getDefaultFontKey());
        if (fallback != null && fallback.isValidPtr()) {
            ImGui.getIO().setFontDefault(fallback);
        }
    }

    private static void renderDockSpace(NamespaceConfig config) {
        if (config == null || !config.dockspaceEnabled()) {
            return;
        }
        DockspaceRenderState state = DockspaceRenderState.createDefault(mcWindowX, mcWindowY, mcWindowWidth, mcWindowHeight);

        for (MineGuiRuntimeContext context : MineGuiCore.getAllContexts()) {
            context.dockspaceCustomizer().customize(state);
        }

        state.normalize();
        state.applyPlacement();
        int styleCount = state.applyStyleOverrides();
        for (Runnable task : state.beforeWindowTasks()) {
            task.run();
        }
        ImGui.begin(state.windowTitle(), state.windowFlags());
        if (styleCount > 0) {
            ImGui.popStyleVar(styleCount);
        }
        for (Runnable task : state.beforeDockspaceTasks()) {
            task.run();
        }
        if (state.isDockspaceEnabled()) {
            int dockspaceId = ImGui.getID(state.dockspaceId());
            ImGui.dockSpace(dockspaceId, state.dockspaceWidth(), state.dockspaceHeight(), state.dockspaceFlags());
        }
        for (Runnable task : state.afterDockspaceTasks()) {
            task.run();
        }
        ImGui.end();
    }

    private static void endFrame() {
        ImGuiContextManager.gl3().renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }
    }
}
