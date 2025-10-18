package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public final class MGCheckbox extends MGComponent<MGCheckbox> implements Textable<MGCheckbox>,
        Clickable<MGCheckbox>, Disableable<MGCheckbox>, Stateful<Boolean, MGCheckbox>, Scalable<MGCheckbox> {

    private static final ComponentPool<MGCheckbox> POOL = new ComponentPool<>(MGCheckbox::new, MGCheckbox::prepare);

    private final ImBoolean checkboxValue = new ImBoolean(false);
    private String literalLabel = "";
    private final Supplier<String> literalSupplier = () -> literalLabel;
    private Supplier<String> labelSupplier;
    @Nullable
    private Runnable onClick;
    @Nullable
    private State<Boolean> state;
    private boolean disabled;
    private boolean value;
    private float scale;

    private MGCheckbox() {
        prepare();
    }

    public static MGCheckbox of(String label) {
        MGCheckbox checkbox = POOL.acquire();
        checkbox.literalLabel = label != null ? label : "";
        checkbox.labelSupplier = checkbox.literalSupplier;
        return checkbox;
    }

    public static MGCheckbox of(String label, boolean initialValue) {
        MGCheckbox checkbox = of(label);
        checkbox.value = initialValue;
        return checkbox;
    }

    public static MGCheckbox of(String label, State<Boolean> state) {
        MGCheckbox checkbox = of(label);
        checkbox.state = state;
        return checkbox;
    }

    public static MGCheckbox of(State<Boolean> state) {
        MGCheckbox checkbox = POOL.acquire();
        checkbox.state = state;
        return checkbox;
    }

    private void prepare() {
        labelSupplier = literalSupplier;
        literalLabel = "";
        onClick = null;
        state = null;
        disabled = false;
        value = false;
        scale = 1.0f;
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
    public State<Boolean> getState() {
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

    @Override
    protected void renderComponent() {
        boolean current = currentValue();
        checkboxValue.set(current);

        boolean disabledScope = disabled;
        boolean scaled = scale != 1.0f;
        float scaleFactor = scaled ? scale : 1.0f;
        String rawLabel = labelSupplier.get();
        String displayLabel = visibleLabel(rawLabel);
        String widgetLabel = widgetLabelFromVisible(displayLabel);
        float checkSize = ImGui.getFrameHeight();
        float spacing = ImGui.getStyle().getItemInnerSpacingX();
        float textWidth = ImGui.calcTextSize(displayLabel).x * scaleFactor;
        float textHeight = ImGui.calcTextSize(displayLabel).y * scaleFactor;
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
                changed = ImGui.checkbox(widgetLabel, checkboxValue);
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
            value = newValue;
        }
    }
}
