package tytoo.minegui.helper.layout.cursor;

import imgui.ImGui;
import tytoo.minegui.helper.constraint.LayoutConstraintSolver;
import tytoo.minegui.helper.constraint.constraints.Constraints;
import tytoo.minegui.helper.layout.LayoutConstraints;

public final class LayoutCursor {
    private LayoutCursor() {
    }

    public static void moveTo(float x, float y) {
        float targetX = Float.isFinite(x) ? x : ImGui.getCursorPosX();
        float targetY = Float.isFinite(y) ? y : ImGui.getCursorPosY();
        ImGui.setCursorPos(targetX, targetY);
    }

    public static void moveTo(LayoutConstraints request) {
        moveTo(request, LayoutContext.capture());
    }

    public static void moveTo(LayoutConstraints request, LayoutContext context) {
        LayoutConstraints constraints = request != null ? request : LayoutConstraints.empty();
        LayoutContext layoutContext = context != null ? context : LayoutContext.capture();
        float rawX = constraints.rawX().orElse(Float.NaN);
        float rawY = constraints.rawY().orElse(Float.NaN);
        boolean hasRawX = Float.isFinite(rawX);
        boolean hasRawY = Float.isFinite(rawY);
        LayoutConstraintSolver.LayoutResult result = null;
        Constraints constraintSet = constraints.constraints().orElse(null);
        if (constraintSet != null && (!hasRawX || !hasRawY)) {
            LayoutConstraintSolver.LayoutFrame frame = layoutContext.toLayoutFrame(constraints);
            result = LayoutConstraintSolver.resolve(constraintSet, frame);
        }
        float targetX = hasRawX ? rawX : resolveAxis(result != null ? result.x() : Float.NaN, ImGui.getCursorPosX());
        float targetY = hasRawY ? rawY : resolveAxis(result != null ? result.y() : Float.NaN, ImGui.getCursorPosY());
        ImGui.setCursorPos(targetX, targetY);
    }

    public static void moveTo(LayoutConstraintSolver.LayoutResult result) {
        if (result == null) {
            return;
        }
        moveTo(result.x(), result.y());
    }

    public static void moveBy(float deltaX, float deltaY) {
        float baseX = ImGui.getCursorPosX();
        float baseY = ImGui.getCursorPosY();
        float targetX = baseX + deltaX;
        float targetY = baseY + deltaY;
        ImGui.setCursorPos(targetX, targetY);
    }

    public static void indent(float amount) {
        if (amount == 0f) {
            return;
        }
        ImGui.indent(amount);
    }

    public static void unindent(float amount) {
        if (amount == 0f) {
            return;
        }
        ImGui.unindent(amount);
    }

    public static void line() {
        ImGui.newLine();
    }

    public static void line(float spacing) {
        ImGui.newLine();
        if (spacing > 0f) {
            ImGui.dummy(0f, spacing);
        }
    }

    public static void sameLine() {
        ImGui.sameLine();
    }

    public static void sameLine(float offset) {
        ImGui.sameLine(offset);
    }

    public static void sameLine(float offset, float spacing) {
        ImGui.sameLine(offset, spacing);
    }

    private static float resolveAxis(float candidate, float fallback) {
        if (Float.isFinite(candidate)) {
            return candidate;
        }
        if (Float.isFinite(fallback)) {
            return fallback;
        }
        return 0f;
    }
}
