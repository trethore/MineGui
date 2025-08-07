package tytoo.minegui.contraint;

import tytoo.minegui.component.MGComponent;

public interface WidthConstraint {
    float calculateWidth(MGComponent<?> component, float parentWidth);
}
