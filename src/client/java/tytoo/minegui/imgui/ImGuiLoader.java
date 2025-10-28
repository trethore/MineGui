package tytoo.minegui.imgui;

import imgui.*;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.*;
import tytoo.minegui.util.InputHelper;

public class ImGuiLoader {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final String GLSL_VERSION = "#version 150";
    private static float appliedGlobalScale = Float.NaN;
    private static volatile boolean reloadRequested;

    private static long windowHandle;
    private static int mcWindowWidth;
    private static int mcWindowHeight;
    private static int mcWindowX;
    private static int mcWindowY;

    public static void onGlfwInit(long handle) {
        MineGuiCore.loadConfig();
        initializeImGui();
        imGuiGlfw.init(handle, false);
        imGuiGl3.init(GLSL_VERSION);
        windowHandle = handle;
    }

    public static void onWindowResize(int width, int height) {
        mcWindowWidth = width;
        mcWindowHeight = height;
    }

    public static void onWindowMoved(int x, int y) {
        mcWindowX = x;
        mcWindowY = y;
    }

    public static void onFrameRender() {
        handlePendingReload();
        imGuiGlfw.newFrame();
        CursorPolicyRegistry.onFrameStart();
        ImGui.newFrame();
        GlobalConfig defaultConfig = GlobalConfigManager.getConfig(GlobalConfigManager.getDefaultNamespace());
        applyGlobalScale(defaultConfig);
        renderDockSpace(defaultConfig);
        for (MineGuiNamespaceContext context : MineGuiNamespaces.all()) {
            GlobalConfig config = context.config().get();
            applyGlobalScale(config);
            context.style().apply();
            context.ui().render();
        }

        ImGui.render();
        endFrame();
    }

    public static void requestReload() {
        reloadRequested = true;
    }

    private static void handlePendingReload() {
        if (!reloadRequested) {
            return;
        }
        performReload();
    }

    private static void performReload() {
        reloadRequested = false;
        if (windowHandle == 0L) {
            return;
        }
        MineGuiCore.LOGGER.info("Reloading MineGui context");
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGuiContext context = ImGui.getCurrentContext();
        if (context != null && !context.isNotValidPtr()) {
            ImGui.destroyContext();
        }
        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        fontLibrary.resetRuntime();
        initializeImGui();
        fontLibrary.preloadRegisteredFonts();
        imGuiGlfw.init(windowHandle, false);
        imGuiGl3.init(GLSL_VERSION);
        for (MineGuiNamespaceContext contextHandle : MineGuiNamespaces.all()) {
            contextHandle.style().apply();
        }
    }

    private static void renderDockSpace(GlobalConfig config) {
        if (config == null || !config.isDockspaceEnabled()) {
            return;
        }
        ImGui.setNextWindowPos(mcWindowX, mcWindowY);
        ImGui.setNextWindowSize(mcWindowWidth, mcWindowHeight);
        final int windowFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoBackground |
                ImGuiWindowFlags.NoNavInputs | ImGuiWindowFlags.NoDocking;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
        ImGui.begin("Dockspace Host", windowFlags);
        ImGui.popStyleVar(2);

        final int dockspaceId = ImGui.getID("MineGuiDockspace");
        ImGui.dockSpace(dockspaceId, 0.0f, 0.0f, ImGuiDockNodeFlags.PassthruCentralNode | ImGuiDockNodeFlags.NoDockingInCentralNode);

        ImGui.end();
    }

    private static void initializeImGui() {
        ImGui.createContext();
        appliedGlobalScale = Float.NaN;

        final ImGuiIO io = ImGui.getIO();
        final GlobalConfig config = GlobalConfigManager.getConfig(MineGuiCore.getConfigNamespace());

        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        if (config.isDockspaceEnabled()) {
            io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        }
        if (config.isViewportEnabled()) {
            io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
            io.setConfigViewportsNoTaskBarIcon(true);
        } else {
            io.setConfigViewportsNoTaskBarIcon(false);
        }

        ImFont defaultFont = configureDefaultFonts(io);
        applyGlobalScale(config);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
        finalizeInitialStyle(defaultFont);
    }

    private static void endFrame() {
        for (MineGuiNamespaceContext context : MineGuiNamespaces.all()) {
            context.viewSaves().onFrameRendered();
        }
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    private static ImFont configureDefaultFonts(ImGuiIO io) {
        final ImFontConfig defaultConfig = new ImFontConfig();
        try {
            defaultConfig.setGlyphRanges(io.getFonts().getGlyphRangesCyrillic());
            defaultConfig.setPixelSnapH(true);
            io.getFonts().addFontDefault(defaultConfig);
        } finally {
            defaultConfig.destroy();
        }

        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        fontLibrary.registerFont(
                fontLibrary.getDefaultFontKey(),
                new MGFontLibrary.FontDescriptor(
                        MGFontLibrary.FontSource.asset("proxima.ttf"),
                        20.0f,
                        config -> {
                            config.setPixelSnapH(true);
                            config.setGlyphRanges(io.getFonts().getGlyphRangesCyrillic());
                        }
                )
        );
        ImFont defaultFont = fontLibrary.ensureFont(fontLibrary.getDefaultFontKey(), null);
        if (defaultFont != null) {
            io.setFontDefault(defaultFont);
        }
        return defaultFont;
    }

    private static void finalizeInitialStyle(ImFont defaultFont) {
        ImGuiStyle style = ImGui.getStyle();
        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        Float fontSize = defaultFont != null ? defaultFont.getFontSize() : null;
        MGStyleDescriptor descriptor = MGStyleDescriptor.capture(
                style,
                MGColorPalette.fromStyle(style),
                fontLibrary.getDefaultFontKey(),
                fontSize
        );
        StyleManager.getInstance().setGlobalDescriptor(descriptor);
        NamedStyleRegistry.getInstance().registerBasePresets(descriptor);
        GlobalConfig config = GlobalConfigManager.getConfig(MineGuiCore.getConfigNamespace());
        String configuredStyleKey = config.getGlobalStyleKey();
        if (configuredStyleKey != null && !configuredStyleKey.isBlank()) {
            Identifier styleKey = Identifier.tryParse(configuredStyleKey);
            if (styleKey != null) {
                StyleManager.getInstance().setGlobalStyleKey(styleKey);
            }
        }
        StyleManager.getInstance().apply();
    }

    public static void onMouseScroll(long window, double horizontal, double vertical) {
        if (windowHandle == 0L || window != windowHandle) {
            return;
        }
        imGuiGlfw.scrollCallback(window, horizontal, vertical);
    }

    public static void onKeyEvent(long window, int key, int scancode, int action, int modifiers) {
        if (windowHandle == 0L || window != windowHandle) {
            return;
        }
        int normalizedKey = InputHelper.toQwerty(key);
        imGuiGlfw.keyCallback(window, normalizedKey, scancode, action, modifiers);
    }

    public static void onCharTyped(long window, int codePoint) {
        if (windowHandle == 0L || window != windowHandle) {
            return;
        }
        imGuiGlfw.charCallback(window, codePoint);
    }

    public static void refreshGlobalScale() {
        applyGlobalScale(GlobalConfigManager.getConfig(MineGuiCore.getConfigNamespace()));
    }

    private static void applyGlobalScale(GlobalConfig config) {
        if (config == null) {
            return;
        }
        ImGuiContext context = ImGui.getCurrentContext();
        if (context == null || context.isNotValidPtr()) {
            return;
        }
        float configuredScale = config.getGlobalScale();
        if (!Float.isFinite(configuredScale) || configuredScale <= 0.0f) {
            configuredScale = 1.0f;
        }
        if (Float.compare(configuredScale, appliedGlobalScale) == 0) {
            return;
        }
        ImGui.getIO().setFontGlobalScale(configuredScale);
        appliedGlobalScale = configuredScale;
    }
}
