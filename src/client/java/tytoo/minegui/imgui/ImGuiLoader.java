package tytoo.minegui.imgui;

import imgui.*;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import tytoo.minegui.MineGuiClient;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.utils.McUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImGuiLoader {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private static long windowHandle;

    public static void onGlfwInit(long handle) {
        initializeImGui(handle);
        imGuiGlfw.init(handle, true);
        imGuiGl3.init();
        windowHandle = handle;
    }

    public static void onFrameRender() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDocking();
        // ImGui.showDemoWindow();
        UIManager.getInstance().render();

        finishDocking();

        ImGui.render();
        endFrame(windowHandle);
    }

    private static void setupDocking() {
        Window window = McUtils.getMc().map(MinecraftClient::getWindow).orElseThrow(() -> new IllegalStateException("Minecraft window not found"));
        ImGui.setNextWindowPos(0.0f, 0.0f, ImGuiCond.Always);
        ImGui.setNextWindowPos(window.getX(), window.getY(), ImGuiCond.Always);
        ImGui.setNextWindowSize(window.getWidth(), window.getHeight());
        final int windowFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoBackground |
                ImGuiWindowFlags.NoNavInputs;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
        ImGui.begin("imgui-mc docking host window", windowFlags);
        ImGui.popStyleVar(2);

        final int dockspaceId = ImGui.getID("MineGuiDockspace");
        ImGui.dockSpace(dockspaceId, 0.0f, 0.0f, ImGuiDockNodeFlags.PassthruCentralNode | ImGuiDockNodeFlags.NoDockingInCentralNode);
    }

    private static void finishDocking() {
        ImGui.end();
    }

    private static void initializeImGui(long glHandle) {
        ImGui.createContext();

        final ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigViewportsNoTaskBarIcon(true);

        final ImFontAtlas fontAtlas = io.getFonts();
        final ImFontConfig fontConfig = new ImFontConfig(); // Natively allocated object, should be explicitly destroyed

        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesCyrillic());

        fontAtlas.addFontDefault();

        fontConfig.setMergeMode(false);
        fontConfig.setPixelSnapH(true);

        fontConfig.destroy();

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
    }

    private static void endFrame(long windowPtr) {
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

    private static void initFont() {
        InputStream fontStream = MineGuiClient.class.getClassLoader().getResourceAsStream("assets/mt/proxima.ttf");
        if (fontStream == null) {
            return;
        }
        File tempFontFile;
        try {
            tempFontFile = File.createTempFile("proxima", ".ttf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tempFontFile.deleteOnExit();

        try (FileOutputStream outputStream = new FileOutputStream(tempFontFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fontStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String tempFontPath = tempFontFile.getAbsolutePath();
        ImGui.getIO().getFonts().addFontFromFileTTF(tempFontPath, 20.0f);
    }

}
