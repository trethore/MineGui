package tytoo.minegui.style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class StyleJsonSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private StyleJsonSerializer() {
    }

    public static String toJson(String namespace, String viewId, Identifier styleKey, MGStyleDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        JsonObject root = new JsonObject();
        if (namespace != null && !namespace.isBlank()) {
            root.addProperty("namespace", namespace);
        }
        if (viewId != null && !viewId.isBlank()) {
            root.addProperty("viewId", viewId);
        }
        if (styleKey != null) {
            root.addProperty("styleKey", styleKey.toString());
        }

        Identifier fontKey = descriptor.getFontKey();
        if (fontKey != null) {
            root.addProperty("fontKey", fontKey.toString());
        }
        Float fontSize = descriptor.getFontSize();
        if (fontSize != null) {
            root.addProperty("fontSize", fontSize);
        }

        root.addProperty("alpha", descriptor.getAlpha());
        root.addProperty("disabledAlpha", descriptor.getDisabledAlpha());
        root.add("windowPadding", vec(descriptor.getWindowPadding()));
        root.addProperty("windowRounding", descriptor.getWindowRounding());
        root.addProperty("windowBorderSize", descriptor.getWindowBorderSize());
        root.add("windowMinSize", vec(descriptor.getWindowMinSize()));
        root.add("windowTitleAlign", vec(descriptor.getWindowTitleAlign()));
        root.addProperty("windowMenuButtonPosition", descriptor.getWindowMenuButtonPosition());
        root.addProperty("childRounding", descriptor.getChildRounding());
        root.addProperty("childBorderSize", descriptor.getChildBorderSize());
        root.addProperty("popupRounding", descriptor.getPopupRounding());
        root.addProperty("popupBorderSize", descriptor.getPopupBorderSize());
        root.add("framePadding", vec(descriptor.getFramePadding()));
        root.addProperty("frameRounding", descriptor.getFrameRounding());
        root.addProperty("frameBorderSize", descriptor.getFrameBorderSize());
        root.add("itemSpacing", vec(descriptor.getItemSpacing()));
        root.add("itemInnerSpacing", vec(descriptor.getItemInnerSpacing()));
        root.add("cellPadding", vec(descriptor.getCellPadding()));
        root.add("touchExtraPadding", vec(descriptor.getTouchExtraPadding()));
        root.addProperty("indentSpacing", descriptor.getIndentSpacing());
        root.addProperty("columnsMinSpacing", descriptor.getColumnsMinSpacing());
        root.addProperty("scrollbarSize", descriptor.getScrollbarSize());
        root.addProperty("scrollbarRounding", descriptor.getScrollbarRounding());
        root.addProperty("grabMinSize", descriptor.getGrabMinSize());
        root.addProperty("grabRounding", descriptor.getGrabRounding());
        root.addProperty("logSliderDeadzone", descriptor.getLogSliderDeadzone());
        root.addProperty("tabRounding", descriptor.getTabRounding());
        root.addProperty("tabBorderSize", descriptor.getTabBorderSize());
        root.addProperty("tabMinWidthForCloseButton", descriptor.getTabMinWidthForCloseButton());
        root.addProperty("colorButtonPosition", descriptor.getColorButtonPosition());
        root.add("buttonTextAlign", vec(descriptor.getButtonTextAlign()));
        root.add("selectableTextAlign", vec(descriptor.getSelectableTextAlign()));
        root.add("displayWindowPadding", vec(descriptor.getDisplayWindowPadding()));
        root.add("displaySafeAreaPadding", vec(descriptor.getDisplaySafeAreaPadding()));
        root.addProperty("mouseCursorScale", descriptor.getMouseCursorScale());
        root.addProperty("antiAliasedLines", descriptor.isAntiAliasedLines());
        root.addProperty("antiAliasedLinesUseTex", descriptor.isAntiAliasedLinesUseTex());
        root.addProperty("antiAliasedFill", descriptor.isAntiAliasedFill());
        root.addProperty("curveTessellationTol", descriptor.getCurveTessellationTol());
        root.addProperty("circleTessellationMaxError", descriptor.getCircleTessellationMaxError());

        JsonObject colors = colors(descriptor.getColorPalette());
        if (colors != null) {
            root.add("colors", colors);
        }

        return GSON.toJson(root);
    }

    private static JsonObject vec(MGVec2 value) {
        JsonObject vec = new JsonObject();
        vec.addProperty("x", value.x());
        vec.addProperty("y", value.y());
        return vec;
    }

    private static JsonObject colors(MGColorPalette palette) {
        if (palette == null || palette.isEmpty()) {
            return null;
        }
        JsonObject colors = new JsonObject();
        for (Map.Entry<Integer, Integer> entry : palette.getColors().entrySet()) {
            colors.addProperty(String.valueOf(entry.getKey()), toHex(entry.getValue()));
        }
        return colors;
    }

    private static String toHex(int color) {
        return String.format("#%08X", color);
    }
}
