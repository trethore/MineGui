package tytoo.minegui.contraint.constraints;

import tytoo.minegui.contraint.ConstraintTarget;
import tytoo.minegui.contraint.HeightConstraint;
import tytoo.minegui.contraint.WidthConstraint;

public record AspectRatioConstraint(float ratio) implements WidthConstraint, HeightConstraint {

    @Override
    public float calculateWidth(ConstraintTarget target, float parentWidth) {
        float h = target.measuredHeight();
        if (h > 0f) {
            return h * ratio;
        }
        return target.measuredWidth();
    }

    @Override
    public float calculateHeight(ConstraintTarget target, float parentHeight) {
        float w = target.measuredWidth();
        if (w > 0f) {
            return w / ratio;
        }
        return target.measuredHeight();
    }
}
