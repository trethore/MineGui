package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.Objects;
import java.util.function.Supplier;

public class MGRadioButton<T> extends MGComponent<MGRadioButton<T>>
        implements Textable<MGRadioButton<T>>, Clickable<MGRadioButton<T>>, Disableable<MGRadioButton<T>>, Stateful<T, MGRadioButton<T>>, Scalable<MGRadioButton<T>> {

    private final T value;
    private Supplier<String> labelSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled;
    private float scale = 1.0f;
    private boolean localSelected;
    @Nullable
    private State<T> state;

    private MGRadioButton(String label, T value, @Nullable State<T> state) {
        this.labelSupplier = () -> label;
        this.value = value;
        this.state = state;
    }

    public static <V> MGRadioButton<V> of(String label, V value) {
        return new MGRadioButton<>(label, value, null);
    }

    public static <V> MGRadioButton<V> of(String label, V value, State<V> state) {
        MGRadioButton<V> button = new MGRadioButton<>(label, value, state);
        button.setState(state);
        return button;
    }

    public static <V> MGRadioButton<V> of(Supplier<String> labelSupplier, V value) {
        MGRadioButton<V> button = new MGRadioButton<>(labelSupplier.get(), value, null);
        button.setTextSupplier(labelSupplier);
        return button;
    }

    public static <V> MGRadioButton<V> of(Supplier<String> labelSupplier, V value, State<V> state) {
        MGRadioButton<V> button = new MGRadioButton<>(labelSupplier.get(), value, state);
        button.setTextSupplier(labelSupplier);
        button.setState(state);
        return button;
    }

    @Override
    public Supplier<String> getTextSupplier() {
        return labelSupplier;
    }

    @Override
    public void setTextSupplier(Supplier<String> supplier) {
        this.labelSupplier = supplier;
    }

    @Nullable
    @Override
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
    public @Nullable State<T> getState() {
        return state;
    }

    @Override
    public void setState(@Nullable State<T> state) {
        this.state = state;
        if (state != null) {
            localSelected = false;
        }
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    public T getValue() {
        return value;
    }

    public boolean isSelected() {
        if (state != null) {
            return Objects.equals(state.get(), value);
        }
        return localSelected;
    }

    @Override
    public void render() {
        float parentWidth = getParentWidth();
        float parentHeight = getParentHeight();

        Constraints constraints = constraints();
        boolean widthIsAR = constraints.getWidthConstraint() instanceof AspectRatioConstraint;
        boolean heightIsAR = constraints.getHeightConstraint() instanceof AspectRatioConstraint;

        float width;
        float height;

        if (widthIsAR && !heightIsAR) {
            height = constraints.computeHeight(parentHeight);
            this.measuredHeight = height;
            width = constraints.computeWidth(parentWidth);
        } else if (heightIsAR && !widthIsAR) {
            width = constraints.computeWidth(parentWidth);
            this.measuredWidth = width;
            height = constraints.computeHeight(parentHeight);
        } else {
            width = constraints.computeWidth(parentWidth);
            height = constraints.computeHeight(parentHeight);
        }

        setMeasuredSize(width, height);

        float x = constraints.computeX(parentWidth, width);
        float y = constraints.computeY(parentHeight, height);
        ImGui.setCursorPos(x, y);

        boolean disabledScope = isDisabled();
        if (disabledScope) {
            ImGui.beginDisabled(true);
        }

        boolean scaled = scale != 1.0f;
        if (scaled) {
            ImGuiUtils.pushWindowFontScale(scale);
        }

        boolean active = isSelected();
        boolean pressed = ImGui.radioButton(labelSupplier.get(), active);

        if (scaled) {
            ImGuiUtils.popWindowFontScale();
        }

        if (disabledScope) {
            ImGui.endDisabled();
        }

        if (pressed && !disabledScope) {
            if (applySelection()) {
                performClick();
            }
        }
    }

    private boolean applySelection() {
        if (state != null) {
            if (Objects.equals(state.get(), value)) {
                return false;
            }
            state.set(value);
            return true;
        }
        if (localSelected) {
            return false;
        }
        localSelected = true;
        return true;
    }
}
