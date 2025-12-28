package tytoo.minegui.helper;

import imgui.ImGui;

public final class Tooltip {

    private Tooltip() {
    }

    public static void quick(String text) {
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(text);
        }
    }

    public static void quick(String fmt, Object... args) {
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(fmt.formatted(args));
        }
    }
}
