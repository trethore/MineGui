package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.Objects;
import java.util.function.Supplier;

public final class MGRadio<T> extends MGComponent<MGRadio<T>>
        implements Textable<MGRadio<T>>, Clickable<MGRadio<T>>, Disableable<MGRadio<T>>, Stateful<T, MGRadio<T>>, Scalable<MGRadio<T>> {

    private static final ComponentPool<MGRadio<?>> POOL =
            new ComponentPool<>(MGRadio::createRaw, MGRadio::resetRaw);

    private String literalLabel = "";
    private final Supplier<String> literalSupplier = () -> literalLabel;
    private Supplier<String> labelSupplier = literalSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled;
    private float scale = 1.0f;
    @Nullable
    private State<T> state;
    @Nullable
    private T value;
    private boolean locallySelected;

    private MGRadio() {
    }

    public static <V> MGRadio<V> of(String label, V value) {
        MGRadio<V> radio = borrow();
        radio.literalLabel = label != null ? label : "";
        radio.labelSupplier = radio.literalSupplier;
        radio.value = value;
        return radio;
    }

    public static <V> MGRadio<V> of(String label, V value, State<V> state) {
        MGRadio<V> radio = of(label, value);
        radio.setState(state);
        return radio;
    }

    public static <V> MGRadio<V> of(Supplier<String> labelSupplier, V value) {
        MGRadio<V> radio = borrow();
        radio.labelSupplier = labelSupplier != null ? labelSupplier : radio.literalSupplier;
        radio.literalLabel = labelSupplier != null ? labelSupplier.get() : "";
        radio.value = value;
        return radio;
    }

    public static <V> MGRadio<V> of(Supplier<String> labelSupplier, V value, State<V> state) {
        MGRadio<V> radio = of(labelSupplier, value);
        radio.setState(state);
        return radio;
    }

    private static MGRadio<?> createRaw() {
        return new MGRadio<>();
    }

    private static void resetRaw(MGRadio<?> radio) {
        radio.reset();
    }

    @SuppressWarnings("unchecked")
    private static <V> MGRadio<V> borrow() {
        return (MGRadio<V>) POOL.acquire();
    }

    public MGRadio<T> selected(boolean selected) {
        this.locallySelected = selected;
        return self();
    }

    @Override
    public Supplier<String> getTextSupplier() {
        return labelSupplier;
    }

    @Override
    public void setTextSupplier(Supplier<String> supplier) {
        labelSupplier = supplier != null ? supplier : literalSupplier;
    }

    @Override
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }

    @Override
    public void setOnClick(@Nullable Runnable action) {
        onClick = action;
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
    @Nullable
    public State<T> getState() {
        return state;
    }

    @Override
    public void setState(@Nullable State<T> state) {
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

    @Nullable
    public T getValue() {
        return value;
    }

    public boolean isSelected() {
        if (state != null) {
            return Objects.equals(state.get(), value);
        }
        return locallySelected;
    }

    @Override
    protected void renderComponent() {
        boolean disabledScope = disabled;
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
    }

    private boolean applySelection() {
        if (state != null) {
            if (Objects.equals(state.get(), value)) {
                return false;
            }
            state.set(value);
            return true;
        }
        if (locallySelected) {
            return false;
        }
        locallySelected = true;
        return true;
    }

    private void reset() {
        literalLabel = "";
        labelSupplier = literalSupplier;
        onClick = null;
        disabled = false;
        scale = 1.0f;
        state = null;
        value = null;
        locallySelected = false;
    }
}
