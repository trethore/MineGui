package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Consumer;

public final class MGColorPicker extends MGComponent<MGColorPicker>
        implements Disableable<MGColorPicker>, Stateful<float[], MGColorPicker>, Scalable<MGColorPicker>, Sizable<MGColorPicker> {

    private static final ComponentPool<MGColorPicker> RGB_POOL =
            new ComponentPool<>(() -> new MGColorPicker(ColorSlice.Model.RGB), MGColorPicker::prepare);
    private static final ComponentPool<MGColorPicker> RGBA_POOL =
            new ComponentPool<>(() -> new MGColorPicker(ColorSlice.Model.RGBA), MGColorPicker::prepare);

    private final ColorSlice color;
    private final String defaultLabel;
    private String label;
    private boolean disabled;
    private float scale;
    private int flags;

    private MGColorPicker(ColorSlice.Model model) {
        this.color = new ColorSlice(model);
        this.defaultLabel = "";
        this.label = defaultLabel;
        prepare();
    }

    public static MGColorPicker ofRgb() {
        return RGB_POOL.acquire();
    }

    public static MGColorPicker ofRgb(State<float[]> state) {
        MGColorPicker picker = ofRgb();
        picker.setState(state);
        return picker;
    }

    public static MGColorPicker ofRgba() {
        return RGBA_POOL.acquire();
    }

    public static MGColorPicker ofRgba(State<float[]> state) {
        MGColorPicker picker = ofRgba();
        picker.setState(state);
        return picker;
    }

    private void prepare() {
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        flags = 0;
        color.reset();
    }

    public MGColorPicker label(@Nullable String label) {
        this.label = label != null ? visibleLabel(label) : defaultLabel;
        return self();
    }

    public MGColorPicker flags(int flags) {
        this.flags = flags;
        return self();
    }

    public MGColorPicker addFlags(int flags) {
        this.flags |= flags;
        return self();
    }

    public MGColorPicker removeFlags(int flags) {
        this.flags &= ~flags;
        return self();
    }

    public MGColorPicker value(float[] color) {
        this.color.setColor(color, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker value(float r, float g, float b) {
        float[] scratch = color.scratch();
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        if (color.components() == 4) {
            scratch[3] = color.valueBuffer()[3];
        }
        color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker value(float r, float g, float b, float a) {
        if (color.model() != ColorSlice.Model.RGBA) {
            throw new IllegalStateException("RGB picker does not support alpha channel");
        }
        float[] scratch = color.scratch();
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        scratch[3] = a;
        color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker value(int packed) {
        float[] scratch = color.scratch();
        color.model().unpack(packed, scratch);
        color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker onChange(@Nullable Consumer<float[]> consumer) {
        color.onChange(consumer);
        return self();
    }

    public MGColorPicker onCommit(@Nullable Consumer<float[]> consumer) {
        color.onCommit(consumer);
        return self();
    }

    public MGColorPicker onPackedChange(@Nullable Consumer<Integer> consumer) {
        color.onPackedChange(consumer);
        return self();
    }

    public MGColorPicker onPackedCommit(@Nullable Consumer<Integer> consumer) {
        color.onPackedCommit(consumer);
        return self();
    }

    public MGColorPicker packedState(@Nullable State<Integer> state) {
        color.setPackedState(state);
        return self();
    }

    public int components() {
        return color.components();
    }

    public float[] snapshotColor() {
        return color.snapshotColor();
    }

    public int snapshotPacked() {
        return color.snapshotPacked();
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
    public State<float[]> getState() {
        return color.getState();
    }

    @Override
    public void setState(@Nullable State<float[]> state) {
        color.setState(state);
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
        boolean stateChanged = color.refreshFromStates();
        if (stateChanged) {
            System.arraycopy(color.valueBuffer(), 0, color.uiBuffer(), 0, color.components());
        }

        float frameHeight = ImGui.getFrameHeight();
        float preferredSize = Math.max(frameHeight * 7.0f, 210.0f);

        boolean scaled = scale != 1.0f;
        boolean disabledScope = disabled;
        String widgetLabel = widgetLabel(label);
        withLayout(preferredSize, preferredSize, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                renderPicker(widgetLabel);
            } finally {
                if (disabledScope) {
                    ImGui.endDisabled();
                }
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
        });
        boolean committed = ImGui.isItemDeactivatedAfterEdit();
        if (committed && !disabled) {
            color.notifyCommit();
        }
        renderChildren();
    }

    private void renderPicker(String widgetLabel) {
        System.arraycopy(color.valueBuffer(), 0, color.uiBuffer(), 0, color.components());
        boolean changed;
        if (color.model() == ColorSlice.Model.RGB) {
            changed = flags != 0 ? ImGui.colorPicker3(widgetLabel, color.uiBuffer(), flags) : ImGui.colorPicker3(widgetLabel, color.uiBuffer());
        } else {
            changed = flags != 0 ? ImGui.colorPicker4(widgetLabel, color.uiBuffer(), flags) : ImGui.colorPicker4(widgetLabel, color.uiBuffer());
        }
        if (changed && !disabled) {
            color.setColor(color.uiBuffer(), ColorSlice.UpdateOrigin.INTERACTION);
        }
    }
}
