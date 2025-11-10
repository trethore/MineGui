package tytoo.minegui.helper.constraint.constraints;

import tytoo.minegui.helper.constraint.ConstraintTarget;
import tytoo.minegui.helper.constraint.HeightConstraint;
import tytoo.minegui.helper.constraint.WidthConstraint;

public record AspectRatioConstraint(float ratio) implements WidthConstraint, HeightConstraint {

    public AspectRatioConstraint(float ratio) {
        this.ratio = normalizeRatio(ratio);
    }

    private static float normalizeRatio(float raw) {
        float sanitized = Float.isFinite(raw) ? Math.abs(raw) : 0f;
        return sanitized > 1e-6f ? sanitized : 1f;
    }

    @Override
    public float calculateWidth(ConstraintTarget target, float parentWidth) {
        float h = target.measuredHeight();
        return h > 0f ? h * ratio : sanitizeFallback(target.measuredWidth());
    }

    @Override
    public float calculateHeight(ConstraintTarget target, float parentHeight) {
        float w = target.measuredWidth();
        return w > 0f ? w / ratio : sanitizeFallback(target.measuredHeight());
    }

    private float sanitizeFallback(float value) {
        if (!Float.isFinite(value) || value < 0f) {
            return 0f;
        }
        return value;
    }
}
