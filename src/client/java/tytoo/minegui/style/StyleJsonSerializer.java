package tytoo.minegui.style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tytoo.minegui.util.ResourceId;

import java.util.Map;
import java.util.Optional;

public final class StyleJsonSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private StyleJsonSerializer() {
    }

    public static String toJson(ResourceId styleKey, StyleDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        JsonObject root = new JsonObject();
        if (styleKey != null) {
            root.addProperty("styleKey", styleKey.toString());
        }

        ResourceId fontKey = descriptor.getFontKey();
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

    public static Optional<StyleDescriptor> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        JsonObject root = GSON.fromJson(json, JsonObject.class);
        if (root == null) {
            return Optional.empty();
        }
        StyleDescriptor.Builder builder = StyleDescriptor.builder();
        builder.alpha(floatVal(root, "alpha", 1.0f));
        builder.disabledAlpha(floatVal(root, "disabledAlpha", 1.0f));
        builder.windowPadding(vec(root, "windowPadding", Vec2.of(0.0f, 0.0f)));
        builder.windowRounding(floatVal(root, "windowRounding", 0.0f));
        builder.windowBorderSize(floatVal(root, "windowBorderSize", 0.0f));
        builder.windowMinSize(vec(root, "windowMinSize", Vec2.of(0.0f, 0.0f)));
        builder.windowTitleAlign(vec(root, "windowTitleAlign", Vec2.of(0.5f, 0.5f)));
        builder.windowMenuButtonPosition(intVal(root, "windowMenuButtonPosition", 0));
        builder.childRounding(floatVal(root, "childRounding", 0.0f));
        builder.childBorderSize(floatVal(root, "childBorderSize", 0.0f));
        builder.popupRounding(floatVal(root, "popupRounding", 0.0f));
        builder.popupBorderSize(floatVal(root, "popupBorderSize", 0.0f));
        builder.framePadding(vec(root, "framePadding", Vec2.of(0.0f, 0.0f)));
        builder.frameRounding(floatVal(root, "frameRounding", 0.0f));
        builder.frameBorderSize(floatVal(root, "frameBorderSize", 0.0f));
        builder.itemSpacing(vec(root, "itemSpacing", Vec2.of(0.0f, 0.0f)));
        builder.itemInnerSpacing(vec(root, "itemInnerSpacing", Vec2.of(0.0f, 0.0f)));
        builder.cellPadding(vec(root, "cellPadding", Vec2.of(0.0f, 0.0f)));
        builder.touchExtraPadding(vec(root, "touchExtraPadding", Vec2.of(0.0f, 0.0f)));
        builder.indentSpacing(floatVal(root, "indentSpacing", 0.0f));
        builder.columnsMinSpacing(floatVal(root, "columnsMinSpacing", 0.0f));
        builder.scrollbarSize(floatVal(root, "scrollbarSize", 0.0f));
        builder.scrollbarRounding(floatVal(root, "scrollbarRounding", 0.0f));
        builder.grabMinSize(floatVal(root, "grabMinSize", 0.0f));
        builder.grabRounding(floatVal(root, "grabRounding", 0.0f));
        builder.logSliderDeadzone(floatVal(root, "logSliderDeadzone", 0.0f));
        builder.tabRounding(floatVal(root, "tabRounding", 0.0f));
        builder.tabBorderSize(floatVal(root, "tabBorderSize", 0.0f));
        builder.tabMinWidthForCloseButton(floatVal(root, "tabMinWidthForCloseButton", 0.0f));
        builder.colorButtonPosition(intVal(root, "colorButtonPosition", 0));
        builder.buttonTextAlign(vec(root, "buttonTextAlign", Vec2.of(0.5f, 0.5f)));
        builder.selectableTextAlign(vec(root, "selectableTextAlign", Vec2.of(0.0f, 0.0f)));
        builder.displayWindowPadding(vec(root, "displayWindowPadding", Vec2.of(0.0f, 0.0f)));
        builder.displaySafeAreaPadding(vec(root, "displaySafeAreaPadding", Vec2.of(0.0f, 0.0f)));
        builder.mouseCursorScale(floatVal(root, "mouseCursorScale", 1.0f));
        builder.antiAliasedLines(boolVal(root, "antiAliasedLines", true));
        builder.antiAliasedLinesUseTex(boolVal(root, "antiAliasedLinesUseTex", true));
        builder.antiAliasedFill(boolVal(root, "antiAliasedFill", true));
        builder.curveTessellationTol(floatVal(root, "curveTessellationTol", 1.25f));
        builder.circleTessellationMaxError(floatVal(root, "circleTessellationMaxError", 0.3f));

        ResourceId fontKey = resourceId(root, "fontKey");
        if (fontKey != null) {
            builder.fontKey(fontKey);
        }
        if (root.has("fontSize")) {
            builder.fontSize(floatValNullable(root, "fontSize", null));
        }

        JsonObject colors = root.has("colors") && root.get("colors").isJsonObject() ? root.getAsJsonObject("colors") : null;
        if (colors != null) {
            ColorPalette.Builder paletteBuilder = ColorPalette.builder();
            for (Map.Entry<String, JsonElement> entry : colors.entrySet()) {
                int key = parseInt(entry.getKey(), -1);
                if (key < 0) continue;
                Integer color = parseColor(entry.getValue().getAsString());
                if (color != null) {
                    paletteBuilder.set(key, color);
                }
            }
            builder.colorPalette(paletteBuilder.build());
        }

        return Optional.of(builder.build());
    }

    private static JsonObject vec(Vec2 value) {
        JsonObject vec = new JsonObject();
        vec.addProperty("x", value.x());
        vec.addProperty("y", value.y());
        return vec;
    }

    private static Vec2 vec(JsonObject root, String key, Vec2 fallback) {
        if (root == null || key == null || !root.has(key) || !root.get(key).isJsonObject()) {
            return fallback;
        }
        JsonObject obj = root.getAsJsonObject(key);
        float x = floatVal(obj, "x", fallback.x());
        float y = floatVal(obj, "y", fallback.y());
        return Vec2.of(x, y);
    }

    private static JsonObject colors(ColorPalette palette) {
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

    private static float floatVal(JsonObject root, String key, Float fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback != null ? fallback : 0.0f;
        }
        try {
            return root.get(key).getAsFloat();
        } catch (NumberFormatException | IllegalStateException | UnsupportedOperationException ignored) {
            return fallback != null ? fallback : 0.0f;
        }
    }

    private static Float floatValNullable(JsonObject root, String key, Float fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsFloat();
        } catch (NumberFormatException | IllegalStateException | UnsupportedOperationException ignored) {
            return fallback;
        }
    }

    private static int intVal(JsonObject root, String key, int fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsInt();
        } catch (NumberFormatException | IllegalStateException | UnsupportedOperationException ignored) {
            return fallback;
        }
    }

    private static boolean boolVal(JsonObject root, String key, boolean fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsBoolean();
        } catch (IllegalStateException | UnsupportedOperationException ignored) {
            return fallback;
        }
    }

    private static ResourceId resourceId(JsonObject root, String key) {
        if (root == null || key == null || !root.has(key)) {
            return null;
        }
        String value = root.get(key).getAsString();
        if (value == null || value.isBlank()) {
            return null;
        }
        return ResourceId.tryParse(value);
    }

    private static Integer parseColor(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        try {
            if (normalized.startsWith("#")) {
                normalized = normalized.substring(1);
            }
            long parsed = Long.parseUnsignedLong(normalized, 16);
            if (normalized.length() == 6) {
                // Assume opaque when alpha omitted.
                parsed |= 0xFF000000L;
            } else if (normalized.length() != 8) {
                return null;
            }
            return (int) parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
