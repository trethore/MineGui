package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
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

public final class MGColorEdit extends MGComponent<MGColorEdit>
        implements Disableable<MGColorEdit>, Stateful<float[], MGColorEdit>, Scalable<MGColorEdit>, Sizable<MGColorEdit> {

    private static final ComponentPool<MGColorEdit> RGB_POOL =
            new ComponentPool<>(() -> new MGColorEdit(ColorSlice.Model.RGB), MGColorEdit::prepare);
    private static final ComponentPool<MGColorEdit> RGBA_POOL =
            new ComponentPool<>(() -> new MGColorEdit(ColorSlice.Model.RGBA), MGColorEdit::prepare);

    private final ColorSlice color;
    private final String defaultLabel;
    private String label;
    private boolean disabled;
    private float scale;
    private int flags;

    private MGColorEdit(ColorSlice.Model model) {
        this.color = new ColorSlice(model);
        this.defaultLabel = "";
        this.label = defaultLabel;
        prepare();
    }

    public static MGColorEdit ofRgb() {
        return RGB_POOL.acquire();
    }

    public static MGColorEdit ofRgb(State<float[]> state) {
        MGColorEdit edit = ofRgb();
        edit.setState(state);
        return edit;
    }

    public static MGColorEdit ofRgba() {
        return RGBA_POOL.acquire();
    }

    public static MGColorEdit ofRgba(State<float[]> state) {
        MGColorEdit edit = ofRgba();
        edit.setState(state);
        return edit;
    }

    private void prepare() {
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        flags = 0;
        color.reset();
    }

    public MGColorEdit label(@Nullable String label) {
        this.label = label != null ? visibleLabel(label) : defaultLabel;
        return self();
    }

    public MGColorEdit flags(int flags) {
        this.flags = flags;
        return self();
    }

    public MGColorEdit addFlags(int flags) {
        this.flags |= flags;
        return self();
    }

    public MGColorEdit removeFlags(int flags) {
        this.flags &= ~flags;
        return self();
    }

    public MGColorEdit display(DisplayMode mode) {
        int mask = ImGuiColorEditFlags.DisplayRGB | ImGuiColorEditFlags.DisplayHSV | ImGuiColorEditFlags.DisplayHex;
        flags &= ~mask;
        if (mode != null) {
            flags |= mode.flag();
        }
        return self();
    }

    public MGColorEdit input(InputMode mode) {
        int mask = ImGuiColorEditFlags.InputRGB | ImGuiColorEditFlags.InputHSV;
        flags &= ~mask;
        if (mode != null) {
            flags |= mode.flag();
        }
        return self();
    }

    public MGColorEdit picker(PickerMode mode) {
        int mask = ImGuiColorEditFlags.PickerHueBar | ImGuiColorEditFlags.PickerHueWheel;
        flags &= ~mask;
        if (mode != null) {
            flags |= mode.flag();
        }
        return self();
    }

    public MGColorEdit alphaPreview(AlphaPreviewMode mode) {
        int mask = ImGuiColorEditFlags.AlphaPreview | ImGuiColorEditFlags.AlphaPreviewHalf;
        flags &= ~mask;
        if (mode != null) {
            flags |= mode.flag();
        }
        return self();
    }

    public MGColorEdit dataType(ColorDataType type) {
        int mask = ImGuiColorEditFlags.Float | ImGuiColorEditFlags.Uint8;
        flags &= ~mask;
        if (type != null) {
            flags |= type.flag();
        }
        return self();
    }

    public MGColorEdit optionsDefault(boolean enabled) {
        if (enabled) {
            flags |= ImGuiColorEditFlags.OptionsDefault;
        } else {
            flags &= ~ImGuiColorEditFlags.OptionsDefault;
        }
        return self();
    }

    public MGColorEdit showAlpha(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoAlpha;
        } else {
            flags |= ImGuiColorEditFlags.NoAlpha;
        }
        return self();
    }

    public MGColorEdit showPicker(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoPicker;
        } else {
            flags |= ImGuiColorEditFlags.NoPicker;
        }
        return self();
    }

    public MGColorEdit showOptionsButton(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoOptions;
        } else {
            flags |= ImGuiColorEditFlags.NoOptions;
        }
        return self();
    }

    public MGColorEdit showSmallPreview(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoSmallPreview;
        } else {
            flags |= ImGuiColorEditFlags.NoSmallPreview;
        }
        return self();
    }

    public MGColorEdit showInputs(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoInputs;
        } else {
            flags |= ImGuiColorEditFlags.NoInputs;
        }
        return self();
    }

    public MGColorEdit showTooltip(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoTooltip;
        } else {
            flags |= ImGuiColorEditFlags.NoTooltip;
        }
        return self();
    }

    public MGColorEdit showLabel(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoLabel;
        } else {
            flags |= ImGuiColorEditFlags.NoLabel;
        }
        return self();
    }

    public MGColorEdit showSidePreview(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoSidePreview;
        } else {
            flags |= ImGuiColorEditFlags.NoSidePreview;
        }
        return self();
    }

    public MGColorEdit enableDragDrop(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoDragDrop;
        } else {
            flags |= ImGuiColorEditFlags.NoDragDrop;
        }
        return self();
    }

    public MGColorEdit showBorder(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoBorder;
        } else {
            flags |= ImGuiColorEditFlags.NoBorder;
        }
        return self();
    }

    public MGColorEdit alphaBar(boolean enabled) {
        if (enabled) {
            flags |= ImGuiColorEditFlags.AlphaBar;
        } else {
            flags &= ~ImGuiColorEditFlags.AlphaBar;
        }
        return self();
    }

    public MGColorEdit hdr(boolean enabled) {
        if (enabled) {
            flags |= ImGuiColorEditFlags.HDR;
        } else {
            flags &= ~ImGuiColorEditFlags.HDR;
        }
        return self();
    }

    public MGColorEdit value(float[] color) {
        this.color.setColor(color, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorEdit value(float r, float g, float b) {
        float[] scratch = color.scratch();
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        if (color.components() == 4) {
            scratch[3] = color.valueBuffer()[3];
        }
        this.color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorEdit value(float r, float g, float b, float a) {
        if (color.model() != ColorSlice.Model.RGBA) {
            throw new IllegalStateException("RGB color edit does not support alpha channel");
        }
        float[] scratch = color.scratch();
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        scratch[3] = a;
        this.color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorEdit value(int packed) {
        float[] scratch = color.scratch();
        color.model().unpack(packed, scratch);
        this.color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorEdit onChange(@Nullable Consumer<float[]> consumer) {
        color.onChange(consumer);
        return self();
    }

    public MGColorEdit onCommit(@Nullable Consumer<float[]> consumer) {
        color.onCommit(consumer);
        return self();
    }

    public MGColorEdit onPackedChange(@Nullable Consumer<Integer> consumer) {
        color.onPackedChange(consumer);
        return self();
    }

    public MGColorEdit onPackedCommit(@Nullable Consumer<Integer> consumer) {
        color.onPackedCommit(consumer);
        return self();
    }

    public MGColorEdit packedState(@Nullable State<Integer> state) {
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
        String visible = visibleLabel(label);
        float baseWidth = frameHeight * 7.0f;
        if (visible != null && !visible.isEmpty()) {
            baseWidth = Math.max(baseWidth, ImGui.calcTextSize(visible).x + frameHeight * 4.0f);
        }
        float baseHeight = frameHeight;

        boolean scaled = scale != 1.0f;
        boolean disabledScope = disabled;
        String widgetLabel = widgetLabel(label);

        withLayout(baseWidth, baseHeight, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                renderColorEdit(widgetLabel);
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

    private void renderColorEdit(String widgetLabel) {
        System.arraycopy(color.valueBuffer(), 0, color.uiBuffer(), 0, color.components());
        boolean changed;
        if (color.model() == ColorSlice.Model.RGB) {
            changed = flags != 0 ? ImGui.colorEdit3(widgetLabel, color.uiBuffer(), flags) : ImGui.colorEdit3(widgetLabel, color.uiBuffer());
        } else {
            changed = flags != 0 ? ImGui.colorEdit4(widgetLabel, color.uiBuffer(), flags) : ImGui.colorEdit4(widgetLabel, color.uiBuffer());
        }
        if (changed && !disabled) {
            color.setColor(color.uiBuffer(), ColorSlice.UpdateOrigin.INTERACTION);
        }
    }

    public enum DisplayMode {
        DEFAULT(0),
        RGB(ImGuiColorEditFlags.DisplayRGB),
        HSV(ImGuiColorEditFlags.DisplayHSV),
        HEX(ImGuiColorEditFlags.DisplayHex);

        private final int flag;

        DisplayMode(int flag) {
            this.flag = flag;
        }

        int flag() {
            return flag;
        }
    }

    public enum InputMode {
        DEFAULT(0),
        RGB(ImGuiColorEditFlags.InputRGB),
        HSV(ImGuiColorEditFlags.InputHSV);

        private final int flag;

        InputMode(int flag) {
            this.flag = flag;
        }

        int flag() {
            return flag;
        }
    }

    public enum PickerMode {
        DEFAULT(0),
        HUE_BAR(ImGuiColorEditFlags.PickerHueBar),
        HUE_WHEEL(ImGuiColorEditFlags.PickerHueWheel);

        private final int flag;

        PickerMode(int flag) {
            this.flag = flag;
        }

        int flag() {
            return flag;
        }
    }

    public enum AlphaPreviewMode {
        NONE(0),
        FULL(ImGuiColorEditFlags.AlphaPreview),
        HALF(ImGuiColorEditFlags.AlphaPreviewHalf);

        private final int flag;

        AlphaPreviewMode(int flag) {
            this.flag = flag;
        }

        int flag() {
            return flag;
        }
    }

    public enum ColorDataType {
        DEFAULT(0),
        FLOAT(ImGuiColorEditFlags.Float),
        UINT8(ImGuiColorEditFlags.Uint8);

        private final int flag;

        ColorDataType(int flag) {
            this.flag = flag;
        }

        int flag() {
            return flag;
        }
    }
}
