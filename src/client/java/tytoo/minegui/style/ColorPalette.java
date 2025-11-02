package tytoo.minegui.style;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ColorPalette {
    private static final ColorPalette EMPTY = new ColorPalette(Collections.emptyMap());

    @Getter
    private final Map<Integer, Integer> colors;

    private ColorPalette(Map<Integer, Integer> colors) {
        this.colors = Map.copyOf(colors);
    }

    public static ColorPalette empty() {
        return EMPTY;
    }

    public static ColorPalette of(Map<Integer, Integer> colors) {
        if (colors == null || colors.isEmpty()) {
            return empty();
        }
        return new ColorPalette(colors);
    }

    public static ColorPalette fromStyle(ImGuiStyle style) {
        if (style == null) {
            return empty();
        }
        Map<Integer, Integer> captured = new HashMap<>();
        ImVec4 buffer = new ImVec4();
        for (int index = 0; index < ImGuiCol.COUNT; index++) {
            style.getColor(index, buffer);
            int packed = ImGui.getColorU32(buffer.x, buffer.y, buffer.z, buffer.w);
            captured.put(index, packed);
        }
        return new ColorPalette(captured);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return colors.isEmpty();
    }

    public void applyTo(ImGuiStyle style) {
        if (style == null || colors.isEmpty()) {
            return;
        }
        colors.forEach(style::setColor);
    }

    public ColorPalette mergedWith(ColorPalette overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return this;
        }
        if (colors.isEmpty()) {
            return overrides;
        }
        Map<Integer, Integer> merged = new HashMap<>(colors);
        merged.putAll(overrides.colors);
        return new ColorPalette(merged);
    }

    public ColorPalette withColor(int index, int color) {
        Map<Integer, Integer> result = new HashMap<>(colors);
        result.put(index, color);
        return new ColorPalette(result);
    }

    public static final class Builder {
        private final Map<Integer, Integer> entries = new HashMap<>();

        public Builder set(int index, int color) {
            entries.put(index, color);
            return this;
        }

        public ColorPalette build() {
            return ColorPalette.of(entries);
        }
    }
}
