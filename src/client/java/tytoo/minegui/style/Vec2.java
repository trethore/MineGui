package tytoo.minegui.style;

import imgui.ImVec2;

public record Vec2(float x, float y) {
    public static Vec2 of(float x, float y) {
        return new Vec2(x, y);
    }

    public static Vec2 from(ImVec2 value) {
        if (value == null) {
            return new Vec2(0.0f, 0.0f);
        }
        return new Vec2(value.x, value.y);
    }
}
