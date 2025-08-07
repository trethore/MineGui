package tytoo.minegui.contraint.constraints;

import tytoo.minegui.component.MGComponent;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;

public class CenterConstraint implements XConstraint, YConstraint {
    @Override
    public float calculateX(MGComponent<?> component, float parentWidth, float componentWidth) {
        return (parentWidth / 2f) - (componentWidth / 2f);
    }

    @Override
    public float calculateY(MGComponent<?> component, float parentHeight, float componentHeight) {
        return (parentHeight / 2f) - (componentHeight / 2f);
    }
}
