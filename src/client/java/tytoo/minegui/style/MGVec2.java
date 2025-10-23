package tytoo.minegui.style;

import imgui.ImVec2;

public record MGVec2(float x, float y) {
    public static MGVec2 of(float x, float y) {
        return new MGVec2(x, y);
    }

    public static MGVec2 from(ImVec2 value) {
        if (value == null) {
            return new MGVec2(0.0f, 0.0f);
        }
        return new MGVec2(value.x, value.y);
    }
}
