package tytoo.minegui.contraint;

import tytoo.minegui.component.MGComponent;

public interface HeightConstraint {
    float calculateHeight(MGComponent<?> component, float parentHeight);
}
