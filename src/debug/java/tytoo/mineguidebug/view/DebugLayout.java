package tytoo.mineguidebug.view;

import imgui.ImGui;

public final class DebugLayout {

    public static final float SECTION_GAP = 6f;
    public static final float SMALL_GAP = 4f;
    public static final float TINY_GAP = 2f;
    public static final float TABLE_LABEL_WIDTH = 140f;
    public static final float TABLE_LABEL_WIDTH_SMALL = 120f;

    private DebugLayout() {
    }

    public static void sectionGap() {
        ImGui.dummy(0f, SECTION_GAP);
    }

    public static void smallGap() {
        ImGui.dummy(0f, SMALL_GAP);
    }

    public static void tinyGap() {
        ImGui.dummy(0f, TINY_GAP);
    }
}
