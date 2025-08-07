package tytoo.minegui.utils;

import imgui.ImGui;

public final class ImGuiUtils {
    private ImGuiUtils() {
    }

    public static boolean shouldCancelGameInputs() {
        return ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
    }
}
