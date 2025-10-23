package tytoo.minegui.style;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MGColorPalette {
    private static final MGColorPalette EMPTY = new MGColorPalette(Collections.emptyMap());

    private final Map<Integer, Integer> colors;

    private MGColorPalette(Map<Integer, Integer> colors) {
        this.colors = Map.copyOf(colors);
    }

    public static MGColorPalette empty() {
        return EMPTY;
    }

    public static MGColorPalette of(Map<Integer, Integer> colors) {
        if (colors == null || colors.isEmpty()) {
            return empty();
        }
        return new MGColorPalette(colors);
    }

    public static MGColorPalette fromStyle(ImGuiStyle style) {
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
        return new MGColorPalette(captured);
    }

    public Map<Integer, Integer> getColors() {
        return colors;
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

    public MGColorPalette mergedWith(MGColorPalette overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return this;
        }
        if (colors.isEmpty()) {
            return overrides;
        }
        Map<Integer, Integer> merged = new HashMap<>(colors);
        merged.putAll(overrides.colors);
        return new MGColorPalette(merged);
    }

    public MGColorPalette withColor(int index, int color) {
        Map<Integer, Integer> result = new HashMap<>(colors);
        result.put(index, color);
        return new MGColorPalette(result);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<Integer, Integer> entries = new HashMap<>();

        public Builder set(int index, int color) {
            entries.put(index, color);
            return this;
        }

        public MGColorPalette build() {
            return MGColorPalette.of(entries);
        }
    }
}
