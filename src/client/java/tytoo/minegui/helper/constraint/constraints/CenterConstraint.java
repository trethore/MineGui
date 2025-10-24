package tytoo.minegui.helper.constraint.constraints;

import tytoo.minegui.helper.constraint.ConstraintTarget;
import tytoo.minegui.helper.constraint.XConstraint;
import tytoo.minegui.helper.constraint.YConstraint;

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
