package tytoo.mineguidebug.view;

import tytoo.minegui.helper.LayoutHelper;

public final class DebugLayout {

    public static final float SECTION_GAP = LayoutHelper.SECTION_GAP;
    public static final float SMALL_GAP = LayoutHelper.SMALL_GAP;
    public static final float TINY_GAP = LayoutHelper.TINY_GAP;
    public static final float TABLE_LABEL_WIDTH = LayoutHelper.TABLE_LABEL_WIDTH;
    public static final float TABLE_LABEL_WIDTH_SMALL = LayoutHelper.TABLE_LABEL_WIDTH_SMALL;

    private DebugLayout() {
    }

    public static void sectionGap() {
        LayoutHelper.sectionGap();
    }

    public static void smallGap() {
        LayoutHelper.smallGap();
    }

    public static void tinyGap() {
        LayoutHelper.tinyGap();
    }
}
