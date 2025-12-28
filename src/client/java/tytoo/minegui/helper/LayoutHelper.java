package tytoo.minegui.helper;

import imgui.ImGui;

public final class LayoutHelper {

    public static final float SECTION_GAP = 6f;
    public static final float SMALL_GAP = 4f;
    public static final float TINY_GAP = 2f;
    public static final float TABLE_LABEL_WIDTH = 140f;
    public static final float TABLE_LABEL_WIDTH_SMALL = 120f;

    private LayoutHelper() {
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

    public static void verticalSpace(float height) {
        ImGui.dummy(0f, height);
    }

    public static void horizontalSpace(float width) {
        ImGui.dummy(width, 0f);
    }
}
