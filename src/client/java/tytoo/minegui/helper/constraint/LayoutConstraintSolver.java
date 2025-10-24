package tytoo.minegui.helper.constraint;

import tytoo.minegui.helper.constraint.constraints.Constraints;

import java.util.Objects;

public final class LayoutConstraintSolver {
    private LayoutConstraintSolver() {
    }

    public static LayoutResult resolve(Constraints constraints, LayoutFrame frame) {
        Objects.requireNonNull(constraints, "constraints");
        Objects.requireNonNull(frame, "frame");
        ConstraintTarget originalTarget = constraints.getTarget();
        if (frame.target() != null && frame.target() != originalTarget) {
            constraints.setTarget(frame.target());
        }
        try {
            float resolvedWidth = resolveWidth(constraints, frame);
            float resolvedHeight = resolveHeight(constraints, frame);
            float resolvedX = resolveX(constraints, frame, resolvedWidth);
            float resolvedY = resolveY(constraints, frame, resolvedHeight);
            return new LayoutResult(resolvedX, resolvedY, resolvedWidth, resolvedHeight);
        } finally {
            constraints.setTarget(originalTarget);
        }
    }

    private static float resolveWidth(Constraints constraints, LayoutFrame frame) {
        float width = normalizeFinite(constraints.computeWidth(frame.parentWidth()));
        if (width <= 0f) {
            width = frame.contentWidth();
        }
        width = constraints.clampWidth(width, frame.parentWidth());
        if (width <= 0f) {
            width = frame.contentWidth();
        }
        if (width < 0f) {
            width = 0f;
        }
        return width;
    }

    private static float resolveHeight(Constraints constraints, LayoutFrame frame) {
        float height = normalizeFinite(constraints.computeHeight(frame.parentHeight()));
        if (height <= 0f) {
            height = frame.contentHeight();
        }
        height = constraints.clampHeight(height, frame.parentHeight());
        if (height <= 0f) {
            height = frame.contentHeight();
        }
        if (height < 0f) {
            height = 0f;
        }
        return height;
    }

    private static float resolveX(Constraints constraints, LayoutFrame frame, float contentWidth) {
        float x = normalizeFinite(constraints.computeX(frame.parentWidth(), contentWidth));
        if (x < 0f) {
            x = 0f;
        }
        return x;
    }

    private static float resolveY(Constraints constraints, LayoutFrame frame, float contentHeight) {
        float y = normalizeFinite(constraints.computeY(frame.parentHeight(), contentHeight));
        if (y < 0f) {
            y = 0f;
        }
        return y;
    }

    private static float normalizeFinite(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return value;
    }

    public record LayoutFrame(
            float parentWidth,
            float parentHeight,
            float contentWidth,
            float contentHeight,
            ConstraintTarget target) {

        public LayoutFrame {
            parentWidth = sanitizeLength(parentWidth);
            parentHeight = sanitizeLength(parentHeight);
            contentWidth = sanitizeLength(contentWidth);
            contentHeight = sanitizeLength(contentHeight);
        }

        public static LayoutFrame of(float parentWidth, float parentHeight, float contentWidth, float contentHeight) {
            return new LayoutFrame(parentWidth, parentHeight, contentWidth, contentHeight, null);
        }

        private static float sanitizeLength(float value) {
            if (!Float.isFinite(value) || value < 0f) {
                return 0f;
            }
            return value;
        }

        public LayoutFrame withTarget(ConstraintTarget newTarget) {
            return new LayoutFrame(parentWidth, parentHeight, contentWidth, contentHeight, newTarget);
        }
    }

    public record LayoutResult(float x, float y, float width, float height) {
        public LayoutResult {
            x = sanitize(x);
            y = sanitize(y);
            width = sanitize(width);
            height = sanitize(height);
        }

        private static float sanitize(float value) {
            if (!Float.isFinite(value)) {
                return 0f;
            }
            return Math.max(value, 0f);
        }
    }
}
