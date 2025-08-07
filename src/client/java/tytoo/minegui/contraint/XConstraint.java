package tytoo.minegui.contraint;

import tytoo.minegui.component.MGComponent;

public interface XConstraint {
    float calculateX(MGComponent<?> component, float parentWidth, float componentWidth);
}
