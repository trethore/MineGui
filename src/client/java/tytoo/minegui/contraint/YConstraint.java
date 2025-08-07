package tytoo.minegui.contraint;

import tytoo.minegui.component.MGComponent;

public interface YConstraint {
    float calculateY(MGComponent<?> component, float parentHeight, float componentHeight);
}
