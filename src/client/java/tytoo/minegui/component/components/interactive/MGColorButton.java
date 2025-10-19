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

import java.util.function.Consumer;

public final class MGColorButton extends MGComponent<MGColorButton>
        implements Disableable<MGColorButton>, Stateful<float[], MGColorButton>, Sizable<MGColorButton>, Scalable<MGColorButton> {

    private static final ComponentPool<MGColorButton> RGB_POOL =
            new ComponentPool<>(() -> new MGColorButton(ColorSlice.Model.RGB), MGColorButton::prepare);
    private static final ComponentPool<MGColorButton> RGBA_POOL =
            new ComponentPool<>(() -> new MGColorButton(ColorSlice.Model.RGBA), MGColorButton::prepare);

    private final ColorSlice color;
    private final String defaultLabel;
    private String label;
    private boolean disabled;
    private float scale;
    private int flags;
    private boolean pickerEnabled;
    private int pickerFlags;
    @Nullable
    private Runnable onClick;
    @Nullable
    private Consumer<float[]> onColorClick;
    @Nullable
    private Consumer<Integer> onPackedClick;

    private MGColorButton(ColorSlice.Model model) {
        this.color = new ColorSlice(model);
        this.defaultLabel = "";
        this.label = defaultLabel;
        prepare();
    }

    public static MGColorButton ofRgb() {
        return RGB_POOL.acquire();
    }

    public static MGColorButton ofRgb(State<float[]> state) {
        MGColorButton button = ofRgb();
        button.setState(state);
        return button;
    }

    public static MGColorButton ofRgba() {
        return RGBA_POOL.acquire();
    }

    public static MGColorButton ofRgba(State<float[]> state) {
        MGColorButton button = ofRgba();
        button.setState(state);
        return button;
    }

    private void prepare() {
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        flags = 0;
        pickerEnabled = false;
        pickerFlags = 0;
        onClick = null;
        onColorClick = null;
        onPackedClick = null;
        color.reset();
    }

    public MGColorButton label(@Nullable String label) {
        this.label = label != null ? label : defaultLabel;
        return self();
    }

    public MGColorButton flags(int flags) {
        this.flags = flags;
        return self();
    }

    public MGColorButton addFlags(int flags) {
        this.flags |= flags;
        return self();
    }

    public MGColorButton removeFlags(int flags) {
        this.flags &= ~flags;
        return self();
    }

    public MGColorButton showAlpha(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoAlpha;
        } else {
            flags |= ImGuiColorEditFlags.NoAlpha;
        }
        return self();
    }

    public MGColorButton alphaPreview(AlphaPreviewMode mode) {
        int mask = ImGuiColorEditFlags.AlphaPreview | ImGuiColorEditFlags.AlphaPreviewHalf;
        flags &= ~mask;
        if (mode != null) {
            flags |= mode.flag();
        }
        return self();
    }

    public MGColorButton showTooltip(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoTooltip;
        } else {
            flags |= ImGuiColorEditFlags.NoTooltip;
        }
        return self();
    }

    public MGColorButton enableDragDrop(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoDragDrop;
        } else {
            flags |= ImGuiColorEditFlags.NoDragDrop;
        }
        return self();
    }

    public MGColorButton showBorder(boolean enabled) {
        if (enabled) {
            flags &= ~ImGuiColorEditFlags.NoBorder;
        } else {
            flags |= ImGuiColorEditFlags.NoBorder;
        }
        return self();
    }

    public MGColorButton hdr(boolean enabled) {
        if (enabled) {
            flags |= ImGuiColorEditFlags.HDR;
        } else {
            flags &= ~ImGuiColorEditFlags.HDR;
        }
        return self();
    }

    public MGColorButton withPicker(boolean enabled) {
        pickerEnabled = enabled;
        return self();
    }

    public MGColorButton pickerFlags(int flags) {
        pickerFlags = flags;
        return self();
    }

    public MGColorButton addPickerFlags(int flags) {
        pickerFlags |= flags;
        return self();
    }

    public MGColorButton removePickerFlags(int flags) {
        pickerFlags &= ~flags;
        return self();
    }

    public MGColorButton value(float[] color) {
        this.color.setColor(color, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorButton value(float r, float g, float b) {
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

    public MGColorButton value(float r, float g, float b, float a) {
        if (color.model() != ColorSlice.Model.RGBA) {
            throw new IllegalStateException("RGB color button does not support alpha channel");
        }
        float[] scratch = color.scratch();
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        scratch[3] = a;
        this.color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorButton value(int packed) {
        float[] scratch = color.scratch();
        color.model().unpack(packed, scratch);
        this.color.setColor(scratch, ColorSlice.UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorButton onClick(@Nullable Runnable action) {
        onClick = action;
        return self();
    }

    public MGColorButton onColorClick(@Nullable Consumer<float[]> consumer) {
        onColorClick = consumer;
        return self();
    }

    public MGColorButton onPackedClick(@Nullable Consumer<Integer> consumer) {
        onPackedClick = consumer;
        return self();
    }

    public MGColorButton onChange(@Nullable Consumer<float[]> consumer) {
        color.onChange(consumer);
        return self();
    }

    public MGColorButton onPackedChange(@Nullable Consumer<Integer> consumer) {
        color.onPackedChange(consumer);
        return self();
    }

    public MGColorButton onCommit(@Nullable Consumer<float[]> consumer) {
        color.onCommit(consumer);
        return self();
    }

    public MGColorButton onPackedCommit(@Nullable Consumer<Integer> consumer) {
        color.onPackedCommit(consumer);
        return self();
    }

    public MGColorButton packedState(@Nullable State<Integer> state) {
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
        float scaledSize = frameHeight * Math.max(0.1f, scale);
        float baseWidth = scaledSize;
        float baseHeight = scaledSize;

        boolean disabledScope = disabled;
        String widgetLabel = widgetLabel(label);

        final boolean[] clicked = {false};
        withLayout(baseWidth, baseHeight, (width, height) -> {
            float resolvedSize = resolveSize(width, height, baseWidth);
            setMeasuredSize(resolvedSize, resolvedSize);
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                clicked[0] = renderColorButton(widgetLabel, resolvedSize);
            } finally {
                if (disabledScope) {
                    ImGui.endDisabled();
                }
            }
        });
        if (clicked[0] && !disabled) {
            notifyClick();
            if (pickerEnabled) {
                ImGui.openPopup(popupId());
            }
        }
        if (pickerEnabled) {
            renderPickerPopup();
        }
        renderChildren();
    }

    private boolean renderColorButton(String widgetLabel, float size) {
        System.arraycopy(color.valueBuffer(), 0, color.uiBuffer(), 0, color.components());
        float clampedSize = Math.max(1f, size);
        return flags != 0
                ? ImGui.colorButton(widgetLabel, color.uiBuffer(), flags, clampedSize, clampedSize)
                : ImGui.colorButton(widgetLabel, color.uiBuffer(), 0, clampedSize, clampedSize);
    }

    private void renderPickerPopup() {
        String popupId = popupId();
        if (pickerEnabled && ImGui.beginPopup(popupId)) {
            System.arraycopy(color.valueBuffer(), 0, color.uiBuffer(), 0, color.components());
            boolean changed;
            if (color.model() == ColorSlice.Model.RGB) {
                changed = pickerFlags != 0 ? ImGui.colorPicker3("##picker", color.uiBuffer(), pickerFlags) : ImGui.colorPicker3("##picker", color.uiBuffer());
            } else {
                changed = pickerFlags != 0 ? ImGui.colorPicker4("##picker", color.uiBuffer(), pickerFlags) : ImGui.colorPicker4("##picker", color.uiBuffer());
            }
            if (changed && !disabled) {
                color.setColor(color.uiBuffer(), ColorSlice.UpdateOrigin.INTERACTION);
            }
            if (ImGui.isItemDeactivatedAfterEdit() && !disabled) {
                color.notifyCommit();
            }
            ImGui.endPopup();
        }
    }

    private void notifyClick() {
        if (onClick != null) {
            onClick.run();
        }
        if (onColorClick != null) {
            onColorClick.accept(color.snapshotColor());
        }
        if (onPackedClick != null) {
            onPackedClick.accept(color.snapshotPacked());
        }
    }

    private float resolveSize(float width, float height, float fallback) {
        float effectiveWidth = Float.isFinite(width) && width > 0f ? width : 0f;
        float effectiveHeight = Float.isFinite(height) && height > 0f ? height : 0f;
        float side = Math.max(effectiveWidth, effectiveHeight);
        if (side <= 0f) {
            side = fallback;
        }
        return Math.max(1f, side);
    }

    private String popupId() {
        String visible = visibleLabel(label);
        if (visible == null || visible.isEmpty()) {
            visible = "Color Picker";
        }
        String id = getId();
        if (id != null && !id.isEmpty()) {
            return visible + "##picker:" + id;
        }
        return visible + "##picker:" + Integer.toHexString(System.identityHashCode(this));
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
}

