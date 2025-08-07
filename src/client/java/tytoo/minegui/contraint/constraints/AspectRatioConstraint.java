package tytoo.minegui.contraint.constraints;

import tytoo.minegui.component.MGComponent;
import tytoo.minegui.contraint.HeightConstraint;
import tytoo.minegui.contraint.WidthConstraint;

public record AspectRatioConstraint(float ratio) implements WidthConstraint, HeightConstraint {

    @Override
    public float calculateWidth(MGComponent<?> component, float parentWidth) {
        float h = component.getMeasuredHeight();
        if (h > 0f) {
            return h * ratio;
        }
        return component.getMeasuredWidth();
    }

    @Override
    public float calculateHeight(MGComponent<?> component, float parentHeight) {
        float w = component.getMeasuredWidth();
        if (w > 0f) {
            return w / ratio;
        }
        return component.getMeasuredHeight();
    }
}
