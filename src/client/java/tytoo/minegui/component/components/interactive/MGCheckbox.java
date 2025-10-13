package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public class MGCheckbox extends MGComponent<MGCheckbox> implements Textable<MGCheckbox>,
        Clickable<MGCheckbox>, Disableable<MGCheckbox>, Stateful<Boolean, MGCheckbox>, Scalable<MGCheckbox> {

    private final ImBoolean checkboxValue = new ImBoolean(false);
    private Supplier<String> labelSupplier;
    @Nullable
    private Runnable onClick;
    @Nullable
    private State<Boolean> state;
    private boolean disabled = false;
    private boolean value;
    private float scale = 1.0f;

    private MGCheckbox(String label, boolean initialValue) {
        this.labelSupplier = () -> label;
        this.value = initialValue;
    }

    public static MGCheckbox of(String label) {
        return new MGCheckbox(label, false);
    }

    public static MGCheckbox of(String label, boolean initialValue) {
        return new MGCheckbox(label, initialValue);
    }

    public static MGCheckbox of(String label, State<Boolean> state) {
        MGCheckbox checkbox = new MGCheckbox(label, Boolean.TRUE.equals(state.get()));
        checkbox.setState(state);
        return checkbox;
    }

    @Override
    public Supplier<String> getTextSupplier() {
        return labelSupplier;
    }

    @Override
    public void setTextSupplier(Supplier<String> supplier) {
        this.labelSupplier = supplier;
    }

    @Override
    public @Nullable Runnable getOnClick() {
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
    public @Nullable State<Boolean> getState() {
        return state;
    }

    @Override
    public void setState(@Nullable State<Boolean> state) {
        this.state = state;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    private boolean currentValue() {
        if (state != null) {
            Boolean stateValue = state.get();
            return stateValue != null && stateValue;
        }
        return value;
    }

    private void applyValue(boolean newValue) {
        if (state != null) {
            state.set(newValue);
        } else {
            this.value = newValue;
        }
    }

    @Override
    public void render() {
        beginRenderLifecycle();
        checkboxValue.set(currentValue());

        boolean disabledScope = disabled;
        boolean scaled = scale != 1.0f;
        String label = labelSupplier.get();
        float checkSize = ImGui.getFrameHeight();
        float spacing = ImGui.getStyle().getItemInnerSpacingX();
        float textWidth = ImGui.calcTextSize(label).x * (scaled ? scale : 1.0f);
        float textHeight = ImGui.calcTextSize(label).y * (scaled ? scale : 1.0f);
        float baseWidth = checkSize + spacing + textWidth;
        float baseHeight = Math.max(checkSize, textHeight);

        withLayout(baseWidth, baseHeight, (width, height) -> {
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            boolean changed;
            try {
                changed = ImGui.checkbox(label, checkboxValue);
            } finally {
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
                if (disabledScope) {
                    ImGui.endDisabled();
                }
            }
            if (changed && !disabled) {
                applyValue(checkboxValue.get());
                performClick();
            }
        });
        renderChildren();
        endRenderLifecycle();
    }
}
