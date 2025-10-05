package tytoo.minegui.utils;

import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.client.util.Window;

@SuppressWarnings("unused")
public final class ImGuiUtils {
    private ImGuiUtils() {
    }

    public static double mouseX() {
        return ImGui.getIO().getMousePosX();
    }

    public static double mouseY() {
        return ImGui.getIO().getMousePosY();
    }

    public static double toImGuiX(Window window, double mcX) {
        ImGuiIO io = ImGui.getIO();
        return mcX / Math.max(1e-6f, io.getDisplayFramebufferScaleX());
    }

    public static double toImGuiY(Window window, double mcY) {
        ImGuiIO io = ImGui.getIO();
        return mcY / Math.max(1e-6f, io.getDisplayFramebufferScaleY());
    }
}
