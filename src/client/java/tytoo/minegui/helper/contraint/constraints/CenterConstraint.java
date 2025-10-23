package tytoo.minegui.helper.contraint.constraints;

import tytoo.minegui.helper.contraint.ConstraintTarget;
import tytoo.minegui.helper.contraint.XConstraint;
import tytoo.minegui.helper.contraint.YConstraint;

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
