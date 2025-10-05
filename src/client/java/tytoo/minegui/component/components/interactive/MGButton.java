package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Clickable;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Textable;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;

import java.util.function.Supplier;

public class MGButton extends MGComponent<MGButton>
        implements Textable<MGButton>, Clickable<MGButton>, Disableable<MGButton> {

    private Supplier<String> textSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled = false;

    private MGButton(String text) {
        this.textSupplier = () -> text;
    }

    public static MGButton of(String text) {
        return new MGButton(text);
    }

    public static MGButton of(State<String> state) {
        return new MGButton(state.get()).bindText(state);
    }

    @Override
    public Supplier<String> getTextSupplier() {
        return textSupplier;
    }

    @Override
    public void setTextSupplier(Supplier<String> supplier) {
        this.textSupplier = supplier;
    }

    @Override
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }

    @Override
    public void setOnClick(@Nullable Runnable action) {
        this.onClick = action;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
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
        if (pressed) {
            performClick();
        }
    }
}
