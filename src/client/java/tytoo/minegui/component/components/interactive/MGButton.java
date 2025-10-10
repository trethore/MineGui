package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public class MGButton extends MGComponent<MGButton>
        implements Textable<MGButton>, Clickable<MGButton>, Disableable<MGButton>, Sizable<MGButton>, Scalable<MGButton> {

    private Supplier<String> textSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled = false;
    private float scale = 1.0f;

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
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void render() {
        float parentWidth = getParentWidth();
        float parentHeight = getParentHeight();

        Constraints c = constraints();
        boolean widthIsAR = c.getWidthConstraint() instanceof AspectRatioConstraint;
        boolean heightIsAR = c.getHeightConstraint() instanceof AspectRatioConstraint;

        String label = textSupplier.get();
        float appliedScale = scale;
        boolean applyScale = appliedScale != 1.0f;
        float stylePaddingX = ImGui.getStyle().getFramePaddingX();
        float stylePaddingY = ImGui.getStyle().getFramePaddingY();
        float textWidth = ImGui.calcTextSize(label).x * appliedScale;
        float textHeight = ImGui.calcTextSize(label).y * appliedScale;
        float baseWidth = textWidth + stylePaddingX * 2.0f;
        float baseHeight = textHeight + stylePaddingY * 2.0f;

        float width;
        float height;

        if (widthIsAR && !heightIsAR) {
            float requestedHeight = c.computeHeight(parentHeight);
            float resolvedHeight = requestedHeight > 0f ? requestedHeight : baseHeight;
            this.measuredHeight = resolvedHeight;
            float requestedWidth = c.computeWidth(parentWidth);
            width = requestedWidth > 0f ? requestedWidth : baseWidth;
            height = resolvedHeight;
        } else if (heightIsAR && !widthIsAR) {
            float requestedWidth = c.computeWidth(parentWidth);
            float resolvedWidth = requestedWidth > 0f ? requestedWidth : baseWidth;
            this.measuredWidth = resolvedWidth;
            float requestedHeight = c.computeHeight(parentHeight);
            width = resolvedWidth;
            height = requestedHeight > 0f ? requestedHeight : baseHeight;
        } else {
            float requestedWidth = c.computeWidth(parentWidth);
            float requestedHeight = c.computeHeight(parentHeight);
            width = requestedWidth > 0f ? requestedWidth : baseWidth;
            height = requestedHeight > 0f ? requestedHeight : baseHeight;
        }

        setMeasuredSize(width, height);

        float x = c.computeX(parentWidth, width);
        float y = c.computeY(parentHeight, height);
        ImGui.setCursorPos(x, y);

        if (applyScale) {
            ImGuiUtils.pushWindowFontScale(appliedScale);
        }
        boolean pressed = ImGui.button(label, width, height);
        if (applyScale) {
            ImGuiUtils.popWindowFontScale();
        }

        if (pressed) {
            performClick();
        }
    }
}
