package tytoo.minegui.helper.layout;

import imgui.ImGui;
import tytoo.minegui.helper.layout.cursor.LayoutCursor;
import tytoo.minegui.helper.layout.sizing.ScaleUnit;

public final class Margin {
    private Margin() {
    }

    public static Scope apply(float uniform) {
        return apply(uniform, uniform, uniform, uniform, ScaleUnit.RAW);
    }

    public static Scope apply(float vertical, float horizontal) {
        return apply(vertical, horizontal, vertical, horizontal, ScaleUnit.RAW);
    }

    public static Scope apply(float top, float right, float bottom, float left) {
        return apply(top, right, bottom, left, ScaleUnit.RAW);
    }

    public static Scope apply(float top, float right, float bottom, float left, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        float resolvedTop = chosen.applyHeight(top);
        float resolvedRight = chosen.applyWidth(right);
        float resolvedBottom = chosen.applyHeight(bottom);
        float resolvedLeft = chosen.applyWidth(left);
        if (resolvedTop > 0f) {
            ImGui.dummy(0f, resolvedTop);
        }
        if (resolvedLeft > 0f) {
            LayoutCursor.indent(resolvedLeft);
        }
        return new Scope(resolvedRight, resolvedBottom, resolvedLeft);
    }

    public static final class Scope implements AutoCloseable {
        private final float right;
        private final float bottom;
        private final float left;
        private boolean closed;

        private Scope(float right, float bottom, float left) {
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (left > 0f) {
                LayoutCursor.unindent(left);
            }
            if (right > 0f) {
                LayoutCursor.moveBy(right, 0f);
            }
            if (bottom > 0f) {
                ImGui.dummy(0f, bottom);
            }
        }
    }
}
