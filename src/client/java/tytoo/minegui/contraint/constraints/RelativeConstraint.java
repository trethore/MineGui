package tytoo.minegui.contraint.constraints;

import tytoo.minegui.contraint.*;

public record RelativeConstraint(float value, float offset)
        implements XConstraint, YConstraint, WidthConstraint, HeightConstraint {

    @Override
    public float calculateX(ConstraintTarget target, float parentWidth, float contentWidth) {
        return parentWidth * value + offset;
    }

    @Override
    public float calculateY(ConstraintTarget target, float parentHeight, float contentHeight) {
        return parentHeight * value + offset;
    }

    @Override
    public float calculateWidth(ConstraintTarget target, float parentWidth) {
        return parentWidth * value + offset;
    }

    @Override
    public float calculateHeight(ConstraintTarget target, float parentHeight) {
        return parentHeight * value + offset;
    }
}
