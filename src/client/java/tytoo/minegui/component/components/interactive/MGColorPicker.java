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
import tytoo.minegui.utils.ColorUtils;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.Arrays;
import java.util.function.Consumer;

public final class MGColorPicker extends MGComponent<MGColorPicker>
        implements Disableable<MGColorPicker>, Stateful<float[], MGColorPicker>, Scalable<MGColorPicker>, Sizable<MGColorPicker> {

    private static final ComponentPool<MGColorPicker> RGB_POOL =
            new ComponentPool<>(() -> new MGColorPicker(ColorModel.RGB), MGColorPicker::prepare);
    private static final ComponentPool<MGColorPicker> RGBA_POOL =
            new ComponentPool<>(() -> new MGColorPicker(ColorModel.RGBA), MGColorPicker::prepare);

    private final ColorModel model;
    private final String defaultLabel;
    private final float[] value;
    private final float[] buffer;
    private final float[] scratch;
    private String label;
    private boolean disabled;
    private float scale;
    private int flags;
    @Nullable
    private State<float[]> state;
    @Nullable
    private State<Integer> packedState;
    @Nullable
    private Consumer<float[]> onChange;
    @Nullable
    private Consumer<float[]> onCommit;
    @Nullable
    private Consumer<Integer> onPackedChange;
    @Nullable
    private Consumer<Integer> onPackedCommit;

    private MGColorPicker(ColorModel model) {
        this.model = model;
        this.defaultLabel = "";
        int components = model.components();
        this.value = new float[components];
        this.buffer = new float[components];
        this.scratch = new float[Math.max(components, 4)];
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
        state = null;
        packedState = null;
        onChange = null;
        onCommit = null;
        onPackedChange = null;
        onPackedCommit = null;
        for (int i = 0; i < model.components(); i++) {
            float channelDefault = model.defaultValue(i);
            value[i] = channelDefault;
            buffer[i] = channelDefault;
        }
        Arrays.fill(scratch, 0.0f);
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
        setInternalColor(color, UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker value(float r, float g, float b) {
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        if (model.components() == 4) {
            scratch[3] = value[3];
        }
        setInternalColor(scratch, UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker value(float r, float g, float b, float a) {
        if (model != ColorModel.RGBA) {
            throw new IllegalStateException("RGB picker does not support alpha channel");
        }
        scratch[0] = r;
        scratch[1] = g;
        scratch[2] = b;
        scratch[3] = a;
        setInternalColor(scratch, UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker value(int packed) {
        model.unpack(packed, scratch);
        setInternalColor(scratch, UpdateOrigin.PROGRAMMATIC);
        return self();
    }

    public MGColorPicker onChange(@Nullable Consumer<float[]> consumer) {
        onChange = consumer;
        return self();
    }

    public MGColorPicker onCommit(@Nullable Consumer<float[]> consumer) {
        onCommit = consumer;
        return self();
    }

    public MGColorPicker onPackedChange(@Nullable Consumer<Integer> consumer) {
        onPackedChange = consumer;
        return self();
    }

    public MGColorPicker onPackedCommit(@Nullable Consumer<Integer> consumer) {
        onPackedCommit = consumer;
        return self();
    }

    public MGColorPicker packedState(@Nullable State<Integer> state) {
        packedState = state;
        if (state != null) {
            Integer snapshot = state.get();
            if (snapshot != null) {
                model.unpack(snapshot, scratch);
                setInternalColor(scratch, UpdateOrigin.STATE_PACKED);
            }
        }
        return self();
    }

    public int components() {
        return model.components();
    }

    public float[] snapshotColor() {
        return Arrays.copyOf(value, model.components());
    }

    public int snapshotPacked() {
        return model.pack(value);
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
        return state;
    }

    @Override
    public void setState(@Nullable State<float[]> state) {
        this.state = state;
        if (state != null) {
            float[] snapshot = state.get();
            if (snapshot != null) {
                setInternalColor(snapshot, UpdateOrigin.STATE_FLOAT);
            }
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

    @Override
    protected void renderComponent() {
        boolean stateChanged = refreshFromStates();
        if (stateChanged) {
            updateBuffersFromValue();
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
            notifyCommit();
        }
        renderChildren();
    }

    private boolean renderPicker(String widgetLabel) {
        updateBuffersFromValue();
        boolean changed;
        if (model == ColorModel.RGB) {
            changed = flags != 0 ? ImGui.colorPicker3(widgetLabel, buffer, flags) : ImGui.colorPicker3(widgetLabel, buffer);
        } else {
            changed = flags != 0 ? ImGui.colorPicker4(widgetLabel, buffer, flags) : ImGui.colorPicker4(widgetLabel, buffer);
        }
        if (changed && !disabled) {
            setInternalColor(buffer, UpdateOrigin.INTERACTION);
        }
        return changed;
    }

    private boolean refreshFromStates() {
        boolean changed = false;
        if (state != null) {
            float[] snapshot = state.get();
            if (snapshot != null) {
                changed |= setInternalColor(snapshot, UpdateOrigin.STATE_FLOAT);
            }
        }
        if (packedState != null) {
            Integer snapshot = packedState.get();
            if (snapshot != null) {
                model.unpack(snapshot, scratch);
                changed |= setInternalColor(scratch, UpdateOrigin.STATE_PACKED);
            }
        }
        return changed;
    }

    private boolean setInternalColor(float[] source, UpdateOrigin origin) {
        boolean changed = false;
        for (int i = 0; i < model.components(); i++) {
            float incoming = source != null && i < source.length ? source[i] : model.defaultValue(i);
            float sanitized = ColorUtils.clampUnit(incoming);
            if (Float.floatToIntBits(value[i]) != Float.floatToIntBits(sanitized)) {
                value[i] = sanitized;
                changed = true;
            }
        }
        if (!changed) {
            if (origin == UpdateOrigin.INTERACTION) {
                notifyChange();
            }
            return false;
        }
        updateBuffersFromValue();
        if (origin == UpdateOrigin.INTERACTION) {
            pushStates();
            notifyChange();
            return true;
        }
        if (origin == UpdateOrigin.PROGRAMMATIC) {
            pushStates();
        }
        return true;
    }

    private void notifyChange() {
        if (onChange != null) {
            onChange.accept(snapshotColor());
        }
        if (onPackedChange != null) {
            onPackedChange.accept(model.pack(value));
        }
    }

    private void notifyCommit() {
        if (onCommit != null) {
            onCommit.accept(snapshotColor());
        }
        if (onPackedCommit != null) {
            onPackedCommit.accept(model.pack(value));
        }
    }

    private void pushStates() {
        pushFloatState();
        pushPackedState();
    }

    private void pushFloatState() {
        if (state != null) {
            state.set(snapshotColor());
        }
    }

    private void pushPackedState() {
        if (packedState != null) {
            packedState.set(model.pack(value));
        }
    }

    private void updateBuffersFromValue() {
        System.arraycopy(value, 0, buffer, 0, model.components());
    }

    private enum UpdateOrigin {
        STATE_FLOAT,
        STATE_PACKED,
        PROGRAMMATIC,
        INTERACTION
    }

    private enum ColorModel {
        RGB(3) {
            @Override
            float defaultValue(int component) {
                return 0.0f;
            }

            @Override
            int pack(float[] color) {
                return ColorUtils.packRgb(color);
            }

            @Override
            void unpack(int packed, float[] target) {
                ColorUtils.unpackRgb(packed, target);
            }
        },
        RGBA(4) {
            @Override
            float defaultValue(int component) {
                return component == 3 ? 1.0f : 0.0f;
            }

            @Override
            int pack(float[] color) {
                return ColorUtils.packRgba(color);
            }

            @Override
            void unpack(int packed, float[] target) {
                ColorUtils.unpackRgba(packed, target);
            }
        };

        private final int components;

        ColorModel(int components) {
            this.components = components;
        }

        int components() {
            return components;
        }

        abstract float defaultValue(int component);

        abstract int pack(float[] color);

        abstract void unpack(int packed, float[] target);
    }
}
