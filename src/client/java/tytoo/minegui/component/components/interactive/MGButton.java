package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;

import java.util.function.Supplier;

public class MGButton extends MGComponent<MGButton> {
    private Supplier<String> textSupplier;
    private Runnable onPress;
    private boolean disabled = false;

    private MGButton(String text) {
        this.textSupplier = () -> text;
    }

    public static MGButton of(String text) {
        return new MGButton(text);
    }

    public static MGButton of(State<String> state) {
        MGButton b = new MGButton(state.get());
        return b.bind(state);
    }

    public MGButton text(String text) {
        this.textSupplier = () -> text;
        return this;
    }

    public MGButton bind(State<String> state) {
        this.textSupplier = state::get;
        return this;
    }

    public MGButton onPress(Runnable action) {
        this.onPress = action;
        return this;
    }

    public MGButton disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
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
        String label = textSupplier.get();

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
        boolean pressed = ImGui.button(label, width, height);
        if (pressed && !disabled && onPress != null) {
            onPress.run();
        }
    }

    public String getText() {
        return textSupplier.get();
    }

    public void setText(String text) {
        this.textSupplier = () -> text;
    }
}
