package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.Objects;
import java.util.function.Supplier;

public class MGRadio<T> extends MGComponent<MGRadio<T>>
        implements Textable<MGRadio<T>>, Clickable<MGRadio<T>>, Disableable<MGRadio<T>>, Stateful<T, MGRadio<T>>, Scalable<MGRadio<T>> {

    private final T value;
    private Supplier<String> labelSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled;
    private float scale = 1.0f;
    private boolean localSelected;
    @Nullable
    private State<T> state;

    private MGRadio(String label, T value, @Nullable State<T> state) {
        this.labelSupplier = () -> label;
        this.value = value;
        this.state = state;
    }

    public static <V> MGRadio<V> of(String label, V value) {
        return new MGRadio<>(label, value, null);
    }

    public static <V> MGRadio<V> of(String label, V value, State<V> state) {
        MGRadio<V> button = new MGRadio<>(label, value, state);
        button.setState(state);
        return button;
    }

    public static <V> MGRadio<V> of(Supplier<String> labelSupplier, V value) {
        MGRadio<V> button = new MGRadio<>(labelSupplier.get(), value, null);
        button.setTextSupplier(labelSupplier);
        return button;
    }

    public static <V> MGRadio<V> of(Supplier<String> labelSupplier, V value, State<V> state) {
        MGRadio<V> button = new MGRadio<>(labelSupplier.get(), value, state);
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
        beginRenderLifecycle();
        boolean disabledScope = isDisabled();
        boolean scaled = scale != 1.0f;
        float scaleFactor = scaled ? scale : 1.0f;
        String label = labelSupplier.get();
        float indicatorSize = ImGui.getFrameHeight();
        float spacing = ImGui.getStyle().getItemInnerSpacingX();
        float textWidth = ImGui.calcTextSize(label).x * scaleFactor;
        float textHeight = ImGui.calcTextSize(label).y * scaleFactor;
        float baseWidth = indicatorSize + spacing + textWidth;
        float baseHeight = Math.max(indicatorSize, textHeight);

        final boolean[] pressedHolder = new boolean[1];
        withLayout(baseWidth, baseHeight, (width, height) -> {
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            try {
                pressedHolder[0] = ImGui.radioButton(label, isSelected());
            } finally {
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
                if (disabledScope) {
                    ImGui.endDisabled();
                }
            }
        });
        boolean pressed = pressedHolder[0];

        if (pressed && !disabledScope) {
            if (applySelection()) {
                performClick();
            }
        }
        renderChildren();
        endRenderLifecycle();
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
