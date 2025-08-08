package tytoo.minegui.imgui;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.MineGuiClient;
import tytoo.minegui.input.InputRouter;
import tytoo.minegui.manager.UIManager;

import java.io.IOException;
import java.io.InputStream;

public class ImGuiLoader {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final String GLSL_VERSION = "#version 150";

    private static int mcWindowWidth;
    private static int mcWindowHeight;
    private static int mcWindowX;
    private static int mcWindowY;

    public static void onGlfwInit(long handle) {
        initializeImGui();
        imGuiGlfw.init(handle, false);
        imGuiGl3.init(GLSL_VERSION);
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
        InputRouter.getInstance().onFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderDockSpace();
        // ImGui.showDemoWindow();
        UIManager.getInstance().render();

        ImGui.render();
        endFrame();
    }

    private static void renderDockSpace() {
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

        final ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigViewportsNoTaskBarIcon(true);

        // Load default font with specific ranges
        final ImFontConfig fontConfig = new ImFontConfig();
        try {
            fontConfig.setGlyphRanges(io.getFonts().getGlyphRangesCyrillic());
            fontConfig.setPixelSnapH(true);
            io.getFonts().addFontDefault(fontConfig);
        } finally {
            fontConfig.destroy();
        }

        // Load custom fonts
        initFont("proxima.ttf", 20.0f);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
    }

    private static void endFrame() {
        // After Dear ImGui prepared a draw data, we use it in the LWJGL3 renderer.
        // At that moment ImGui will be rendered to the current OpenGL context.
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }

        //glfwSwapBuffers(windowPtr);
        //glfwPollEvents();
    }

    private static void initFont(String fontName, float fontSize) {
        final ImGuiIO io = ImGui.getIO();
        final String fontPath = String.format("assets/%s/fonts/%s", MineGuiClient.MOD_ID, fontName);

        try (InputStream fontStream = MineGuiClient.class.getClassLoader().getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                MineGuiClient.LOGGER.warn("Font not found: {}", fontPath);
                return;
            }

            final byte[] fontBytes = fontStream.readAllBytes();

            final ImFontConfig fontConfig = new ImFontConfig();
            try {
                fontConfig.setPixelSnapH(true);
                io.getFonts().addFontFromMemoryTTF(fontBytes, fontSize, fontConfig);
            } finally {
                fontConfig.destroy();
            }
        } catch (IOException e) {
            MineGuiClient.LOGGER.error("Failed to load font: {}", fontPath, e);
        }
    }
}