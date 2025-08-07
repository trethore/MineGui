package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;

public class MGButton extends MGComponent<MGButton> {
    private String text;

    private MGButton(String text) {
        this.text = text;
    }

    public static MGButton of(String text) {
        return new MGButton(text);
    }

    @Override
    public void render() {
        float parentWidth = getParentWidth();
        float parentHeight = getParentHeight();

        Constraints c = constraints();
        boolean widthIsAR = c.getWidthConstraint() instanceof AspectRatioConstraint;
        boolean heightIsAR = c.getHeightConstraint() instanceof AspectRatioConstraint;

        float width;
        float height;

        if (widthIsAR && !heightIsAR) {
            height = c.computeHeight(parentHeight);
            this.measuredHeight = height;
            width = c.computeWidth(parentWidth);
        } else if (heightIsAR && !widthIsAR) {
            width = c.computeWidth(parentWidth);
            this.measuredWidth = width;
            height = c.computeHeight(parentHeight);
        } else {
            width = c.computeWidth(parentWidth);
            height = c.computeHeight(parentHeight);
        }

        this.setMeasuredSize(width, height);

        float x = c.computeX(parentWidth, width);
        float y = c.computeY(parentHeight, height);
        ImGui.setCursorPos(x, y);
        ImGui.button(text, width, height);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
