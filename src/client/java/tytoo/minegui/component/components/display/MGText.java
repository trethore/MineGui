package tytoo.minegui.component.components.display;

import imgui.ImGui;
import imgui.ImVec2;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Textable;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public class MGText extends MGComponent<MGText> implements Textable<MGText>, Scalable<MGText> {

    private Supplier<String> textSupplier;
    private float scale = 1.0f;

    private MGText(String text) {
        this.textSupplier = () -> text;
    }

    public static MGText of(String text) {
        return new MGText(text);
    }

    public static MGText of(State<String> state) {
        return new MGText(state.get()).bindText(state);
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
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void render() {
        beginRenderLifecycle();
        String text = getText();

        float parentWidth = getParentWidth();
        float parentHeight = getParentHeight();

        Constraints constraints = constraints();
        float requestedWidth = constraints.computeWidth(parentWidth);
        float requestedHeight = constraints.computeHeight(parentHeight);

        boolean applyScale = scale != 1.0f;
        float scaleFactor = applyScale ? scale : 1.0f;
        ImVec2 textSize = ImGui.calcTextSize(text);
        float baselineWidth = textSize.x * scaleFactor;
        float baselineHeight = textSize.y * scaleFactor;

        float measuredWidth = requestedWidth > 0f ? requestedWidth : baselineWidth;
        float measuredHeight = requestedHeight > 0f ? requestedHeight : baselineHeight;
        setMeasuredSize(measuredWidth, measuredHeight);

        float x = constraints.computeX(parentWidth, measuredWidth);
        float y = constraints.computeY(parentHeight, measuredHeight);
        ImGui.setCursorPos(x, y);

        if (applyScale) {
            ImGuiUtils.pushWindowFontScale(scale);
        }
        ImGui.text(text);
        if (applyScale) {
            ImGuiUtils.popWindowFontScale();
        }
        renderChildren();
        endRenderLifecycle();
    }
}
