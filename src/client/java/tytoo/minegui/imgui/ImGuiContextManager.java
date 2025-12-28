package tytoo.minegui.imgui;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;
import lombok.Getter;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.ConfigRegistry;
import tytoo.minegui.config.GlobalConfigNamespaceConfigStore;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.config.NamespaceConfigStore;
import tytoo.minegui.runtime.MineGuiRuntimeContext;
import tytoo.minegui.style.*;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.util.ResourceId;

public final class ImGuiContextManager {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final NamespaceConfigStore DEFAULT_CONFIG_STORE = new GlobalConfigNamespaceConfigStore();
    private static final String GLSL_VERSION = "#version 150";

    @Getter
    private static volatile boolean contextInitialized;
    private static volatile boolean clientStarted;
    private static volatile boolean initializationInProgress;
    private static volatile boolean initializationFailed;
    private static long windowHandle;

    private ImGuiContextManager() {
    }

    public static void onGlfwInit(long handle) {
        windowHandle = handle;
    }

    public static void onClientStarted() {
        clientStarted = true;
    }

    public static boolean tryInitialize() {
        if (!MineGuiCore.isInitialized()) {
            return false;
        }
        if (contextInitialized || initializationInProgress || initializationFailed) {
            return contextInitialized;
        }
        if (!clientStarted || windowHandle == 0L) {
            return false;
        }
        initializeContext();
        return contextInitialized;
    }

    public static void requestReload() {
        if (!MineGuiCore.isInitialized()) {
            return;
        }
        if (contextInitialized) {
            MineGuiCore.LOGGER.warn("MineGui reload requested after initialization; restart the client to refresh fonts.");
            return;
        }
        initializationFailed = false;
        tryInitialize();
    }

    static ImGuiImplGlfw glfw() {
        return imGuiGlfw;
    }

    static ImGuiImplGl3 gl3() {
        return imGuiGl3;
    }

    static long windowHandle() {
        return windowHandle;
    }

    private static void initializeContext() {
        initializationInProgress = true;
        try {
            MineGuiCore.LOGGER.info("Initializing MineGui context");
            FontLibrary fontLibrary = FontLibrary.getInstance();
            fontLibrary.resetRuntime();
            StyleManager.resetAllActiveFonts();
            initializeImGui();
            fontLibrary.preloadRegisteredFonts();
            imGuiGlfw.init(windowHandle, false);
            imGuiGl3.init(GLSL_VERSION);
            if (!rebuildFontAtlasTexture()) {
                MineGuiCore.LOGGER.error("Failed to initialize MineGui font atlas. Ensure fonts register before MineGui starts.");
                teardown();
                initializationFailed = true;
                return;
            }
            ImGuiImageUtils.invalidateAll();
            ImGuiRenderer.reapplyNamespaceStyles();
            fontLibrary.lockRegistration();
            contextInitialized = true;
            initializationFailed = false;
            MineGuiCore.fireContextReadyListeners();
        } finally {
            initializationInProgress = false;
        }
    }

    private static void teardown() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGuiContext context = ImGui.getCurrentContext();
        if (context != null && !context.isNotValidPtr()) {
            ImGui.destroyContext(context);
            ImGui.setCurrentContext(null);
        }
    }

    private static void initializeImGui() {
        ImGuiContext context = ImGui.createContext();
        ImGui.setCurrentContext(context);
        ImGuiRenderer.resetAppliedGlobalScale();

        ImGuiIO io = ImGui.getIO();
        NamespaceConfig config = resolveDefaultConfig();

        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        if (config.dockspaceEnabled()) {
            io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        }
        if (config.viewportEnabled()) {
            io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
            io.setConfigViewportsNoTaskBarIcon(true);
        } else {
            io.setConfigViewportsNoTaskBarIcon(false);
        }

        ImFont defaultFont = configureDefaultFonts(io);
        ImGuiRenderer.applyGlobalScale(config);
        runFontRegistrars(io);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
        finalizeInitialStyle(defaultFont);
    }

    private static ImFont configureDefaultFonts(ImGuiIO io) {
        ImFontConfig defaultConfig = new ImFontConfig();
        ImFont baseDefaultFont;
        try {
            defaultConfig.setGlyphRanges(io.getFonts().getGlyphRangesCyrillic());
            defaultConfig.setPixelSnapH(true);
            baseDefaultFont = io.getFonts().addFontDefault(defaultConfig);
        } finally {
            defaultConfig.destroy();
        }

        ImFont defaultFont = baseDefaultFont;
        if (shouldRegisterDefaultFonts()) {
            Fonts.registerDefaults(io);
            FontLibrary fontLibrary = FontLibrary.getInstance();
            ImFont registeredDefault = Fonts.ensure(fontLibrary.getDefaultFontKey());
            if (registeredDefault != null) {
                io.setFontDefault(registeredDefault);
                defaultFont = registeredDefault;
            }
        }
        return defaultFont;
    }

    private static boolean shouldRegisterDefaultFonts() {
        for (MineGuiRuntimeContext context : MineGuiCore.getAllContexts()) {
            if (context.options().registerDefaultFonts()) {
                return true;
            }
        }
        return false;
    }

    private static void runFontRegistrars(ImGuiIO io) {
        for (MineGuiRuntimeContext context : MineGuiCore.getAllContexts()) {
            var registrar = context.options().fontRegistrar();
            if (registrar == null) {
                continue;
            }
            try {
                registrar.accept(io);
            } catch (RuntimeException exception) {
                MineGuiCore.LOGGER.error("Font registrar failed for namespace '{}'", context.options().namespace(), exception);
            }
        }
    }

    private static void finalizeInitialStyle(ImFont defaultFont) {
        ImGuiStyle style = ImGui.getStyle();
        FontLibrary fontLibrary = FontLibrary.getInstance();
        Float fontSize = defaultFont != null ? defaultFont.getFontSize() : null;
        StyleDescriptor descriptor = StyleDescriptor.capture(
                style,
                ColorPalette.fromStyle(style),
                fontLibrary.getDefaultFontKey(),
                fontSize
        );
        StyleManager.getInstance().setGlobalDescriptor(descriptor);
        StyleManager.backfillGlobalDescriptors(descriptor);
        NamedStyleRegistry.getInstance().registerBasePresets(descriptor);
        NamespaceConfig config = resolveDefaultConfig();
        ResourceId configuredStyleKey = config.globalStyleKey();
        if (configuredStyleKey != null) {
            StyleManager.getInstance().setGlobalStyleKey(configuredStyleKey);
        }
        StyleManager.getInstance().apply();
        StyleManager.publishGlobalDescriptor(descriptor);
    }

    public static boolean rebuildFontAtlasTexture() {
        if (!ContextGuard.hasValidContext()) {
            MineGuiCore.LOGGER.warn("ImGui context unavailable while rebuilding MineGui font atlas");
            return false;
        }
        ImGuiIO io = ImGui.getIO();
        imgui.ImFontAtlas atlas = io != null ? io.getFonts() : null;
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

    static NamespaceConfig resolveDefaultConfig() {
        var context = MineGuiCore.getContext();
        if (context == null) {
            var all = MineGuiCore.getAllContexts();
            if (!all.isEmpty()) {
                context = all.iterator().next();
            }
        }
        if (context == null) {
            return DEFAULT_CONFIG_STORE.load(ConfigRegistry.defaultNamespace());
        }
        return context.config().current();
    }
}
