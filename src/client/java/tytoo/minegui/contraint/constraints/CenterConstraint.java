package tytoo.minegui.contraint.constraints;

import tytoo.minegui.contraint.ConstraintTarget;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;

public class CenterConstraint implements XConstraint, YConstraint {
    @Override
    public float calculateX(ConstraintTarget target, float parentWidth, float contentWidth) {
        return (parentWidth / 2f) - (contentWidth / 2f);
    }

    @Override
    public float calculateY(ConstraintTarget target, float parentHeight, float contentHeight) {
        return (parentHeight / 2f) - (contentHeight / 2f);
    }
}
