package tytoo.minegui.imgui;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.imgui.dock.DockspaceRenderState;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.*;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.util.InputHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImGuiLoader {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final String GLSL_VERSION = "#version 150";
    private static float appliedGlobalScale = Float.NaN;
    private static volatile boolean contextInitialized;
    private static volatile boolean clientStarted;
    private static volatile boolean initializationInProgress;
    private static volatile boolean initializationFailed;

    private static long windowHandle;
    private static int mcWindowWidth;
    private static int mcWindowHeight;
    private static int mcWindowX;
    private static int mcWindowY;

    public static void onGlfwInit(long handle) {
        MineGuiCore.loadConfig();
        windowHandle = handle;
        tryInitializeContext();
    }

    public static void onWindowResize(int width, int height) {
        mcWindowWidth = width;
        mcWindowHeight = height;
    }

    public static void onWindowMoved(int x, int y) {
        mcWindowX = x;
        mcWindowY = y;
    }

    public static void onClientStarted() {
        clientStarted = true;
        tryInitializeContext();
    }

    public static void onFrameRender() {
        tryInitializeContext();
        if (!contextInitialized) {
            return;
        }
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
        if (contextInitialized) {
            MineGuiCore.LOGGER.warn("MineGui reload requested after initialization; restart the client to refresh fonts.");
            return;
        }
        initializationFailed = false;
        tryInitializeContext();
    }

    private static void tryInitializeContext() {
        if (contextInitialized || initializationInProgress || initializationFailed) {
            return;
        }
        if (!clientStarted || windowHandle == 0L) {
            return;
        }
        initializeContext();
    }

    private static void initializeContext() {
        initializationInProgress = true;
        try {
            MineGuiCore.LOGGER.info("Initializing MineGui context");
            MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
            fontLibrary.resetRuntime();
            StyleManager.resetAllActiveFonts();
            initializeImGui();
            fontLibrary.preloadRegisteredFonts();
            imGuiGlfw.init(windowHandle, false);
            imGuiGl3.init(GLSL_VERSION);
            if (!rebuildFontAtlasTexture()) {
                MineGuiCore.LOGGER.error("Failed to initialize MineGui font atlas. Ensure fonts register before MineGui starts.");
                teardownContext();
                initializationFailed = true;
                return;
            }
            ImGuiImageUtils.invalidateAll();
            for (MineGuiNamespaceContext contextHandle : MineGuiNamespaces.all()) {
                contextHandle.style().apply();
            }
            fontLibrary.lockRegistration();
            contextInitialized = true;
            initializationFailed = false;
        } finally {
            initializationInProgress = false;
        }
    }

    private static void teardownContext() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGuiContext context = ImGui.getCurrentContext();
        if (context != null && !context.isNotValidPtr()) {
            ImGui.destroyContext(context);
            ImGui.setCurrentContext(null);
        }
    }

    private static void renderDockSpace(GlobalConfig config) {
        if (config == null || !config.isDockspaceEnabled()) {
            return;
        }
        DockspaceRenderState state = DockspaceRenderState.createDefault(mcWindowX, mcWindowY, mcWindowWidth, mcWindowHeight);
        List<MineGuiNamespaceContext> contexts = new ArrayList<>(MineGuiNamespaces.all());
        contexts.sort(Comparator.comparing(MineGuiNamespaceContext::namespace));
        for (MineGuiNamespaceContext context : contexts) {
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

    private static void initializeImGui() {
        ImGuiContext context = ImGui.createContext();
        ImGui.setCurrentContext(context);
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

        MGFonts.registerDefaults(io);
        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        ImFont defaultFont = MGFonts.ensure(fontLibrary.getDefaultFontKey());
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
        StyleManager.backfillGlobalDescriptors(descriptor);
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

    private static boolean rebuildFontAtlasTexture() {
        ImGuiContext context = ImGui.getCurrentContext();
        if (context == null || context.isNotValidPtr()) {
            MineGuiCore.LOGGER.warn("ImGui context unavailable while rebuilding MineGui font atlas");
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        ImFontAtlas atlas = io != null ? io.getFonts() : null;
        if (atlas == null) {
            MineGuiCore.LOGGER.warn("ImGui font atlas is not available during MineGui reload");
            return false;
        }
        atlas.setTexID(0);
        if (!atlas.build()) {
            MineGuiCore.LOGGER.warn("Failed to rebuild font atlas after MineGui reload");
            return false;
        }
        imGuiGl3.updateFontsTexture();
        if (atlas.getTexID() == 0) {
            MineGuiCore.LOGGER.warn("Font atlas texture upload resulted in texId=0");
            return false;
        }
        return true;
    }
}
