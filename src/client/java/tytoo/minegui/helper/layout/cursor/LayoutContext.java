package tytoo.minegui.helper.layout.cursor;

import imgui.ImGui;
import imgui.ImVec2;
import tytoo.minegui.helper.constraint.ConstraintTarget;
import tytoo.minegui.helper.constraint.LayoutConstraintSolver;
import tytoo.minegui.helper.layout.LayoutConstraints;

public final class LayoutContext {
    private final float windowPosX;
    private final float windowPosY;
    private final float windowWidth;
    private final float windowHeight;
    private final float cursorPosX;
    private final float cursorPosY;
    private final float contentRegionMinX;
    private final float contentRegionMinY;
    private final float contentRegionMaxX;
    private final float contentRegionMaxY;
    private final float contentRegionAvailX;
    private final float contentRegionAvailY;

    private LayoutContext(
            float windowPosX,
            float windowPosY,
            float windowWidth,
            float windowHeight,
            float cursorPosX,
            float cursorPosY,
            float contentRegionMinX,
            float contentRegionMinY,
            float contentRegionMaxX,
            float contentRegionMaxY,
            float contentRegionAvailX,
            float contentRegionAvailY) {
        this.windowPosX = windowPosX;
        this.windowPosY = windowPosY;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.cursorPosX = cursorPosX;
        this.cursorPosY = cursorPosY;
        this.contentRegionMinX = contentRegionMinX;
        this.contentRegionMinY = contentRegionMinY;
        this.contentRegionMaxX = contentRegionMaxX;
        this.contentRegionMaxY = contentRegionMaxY;
        this.contentRegionAvailX = contentRegionAvailX;
        this.contentRegionAvailY = contentRegionAvailY;
    }

    public static LayoutContext capture() {
        ImVec2 windowPos = new ImVec2();
        ImVec2 windowSize = new ImVec2();
        ImVec2 cursorPos = new ImVec2();
        ImVec2 contentMin = new ImVec2();
        ImVec2 contentMax = new ImVec2();
        ImVec2 contentAvail = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImGui.getWindowSize(windowSize);
        ImGui.getCursorPos(cursorPos);
        ImGui.getWindowContentRegionMin(contentMin);
        ImGui.getWindowContentRegionMax(contentMax);
        ImGui.getContentRegionAvail(contentAvail);
        return new LayoutContext(
                sanitize(windowPos.x),
                sanitize(windowPos.y),
                sanitizePositive(windowSize.x),
                sanitizePositive(windowSize.y),
                sanitize(cursorPos.x),
                sanitize(cursorPos.y),
                sanitize(contentMin.x),
                sanitize(contentMin.y),
                sanitize(contentMax.x),
                sanitize(contentMax.y),
                sanitizePositive(contentAvail.x),
                sanitizePositive(contentAvail.y)
        );
    }

    private static float sanitize(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return value;
    }

    private static float sanitizePositive(float value) {
        if (!Float.isFinite(value) || value < 0f) {
            return 0f;
        }
        return value;
    }

    private static float clampPositive(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(value, 0f);
    }

    public float windowPosX() {
        return windowPosX;
    }

    public float windowPosY() {
        return windowPosY;
    }

    public float windowWidth() {
        return windowWidth;
    }

    public float windowHeight() {
        return windowHeight;
    }

    public float cursorPosX() {
        return cursorPosX;
    }

    public float cursorPosY() {
        return cursorPosY;
    }

    public float contentRegionMinX() {
        return contentRegionMinX;
    }

    public float contentRegionMinY() {
        return contentRegionMinY;
    }

    public float contentRegionMaxX() {
        return contentRegionMaxX;
    }

    public float contentRegionMaxY() {
        return contentRegionMaxY;
    }

    public float contentRegionWidth() {
        return clampPositive(contentRegionMaxX - contentRegionMinX);
    }

    public float contentRegionHeight() {
        return clampPositive(contentRegionMaxY - contentRegionMinY);
    }

    public float contentRegionAvailX() {
        return contentRegionAvailX;
    }

    public float contentRegionAvailY() {
        return contentRegionAvailY;
    }

    public ConstraintTarget constraintTarget() {
        return ConstraintTarget.of(contentRegionWidth(), contentRegionHeight());
    }

    public LayoutConstraintSolver.LayoutFrame toLayoutFrame(LayoutConstraints request) {
        LayoutConstraints safeRequest = request != null ? request : LayoutConstraints.empty();
        ConstraintTarget target = safeRequest
                .targetOverride()
                .orElseGet(this::constraintTarget);
        float parentWidth = contentRegionWidth();
        float parentHeight = contentRegionHeight();
        float contentWidth = safeRequest.widthOverride().orElse(0f);
        float contentHeight = safeRequest.heightOverride().orElse(0f);
        return new LayoutConstraintSolver.LayoutFrame(
                parentWidth,
                parentHeight,
                contentWidth,
                contentHeight,
                target
        );
    }
}
