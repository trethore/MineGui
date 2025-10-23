package tytoo.minegui.contraint.constraints;

import tytoo.minegui.contraint.*;

public record PixelConstraint(float value)
        implements XConstraint, YConstraint, WidthConstraint, HeightConstraint {

    @Override
    public float calculateX(ConstraintTarget target, float parentWidth, float contentWidth) {
        return value;
    }

    @Override
    public float calculateY(ConstraintTarget target, float parentHeight, float contentHeight) {
        return value;
    }

    @Override
    public float calculateWidth(ConstraintTarget target, float parentWidth) {
        return value;
    }

    @Override
    public float calculateHeight(ConstraintTarget target, float parentHeight) {
        return value;
    }
}
