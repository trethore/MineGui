package tytoo.minegui.contraint.constraints;

import tytoo.minegui.component.MGComponent;
import tytoo.minegui.contraint.HeightConstraint;
import tytoo.minegui.contraint.WidthConstraint;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;

public record PixelConstraint(float value)
        implements XConstraint, YConstraint, WidthConstraint, HeightConstraint {

    @Override
    public float calculateX(MGComponent<?> component, float parentWidth, float componentWidth) {
        return value;
    }

    @Override
    public float calculateY(MGComponent<?> component, float parentHeight, float componentHeight) {
        return value;
    }

    @Override
    public float calculateWidth(MGComponent<?> component, float parentWidth) {
        return value;
    }

    @Override
    public float calculateHeight(MGComponent<?> component, float parentHeight) {
        return value;
    }
}