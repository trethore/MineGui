package tytoo.minegui.component.components.interactive;

import org.jetbrains.annotations.Nullable;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ColorUtils;

import java.util.Arrays;
import java.util.function.Consumer;

final class ColorSlice {

    private final Model model;
    private final float[] value;
    private final float[] buffer;
    private final float[] scratch;
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

    ColorSlice(Model model) {
        this.model = model;
        int components = model.components();
        this.value = new float[components];
        this.buffer = new float[components];
        this.scratch = new float[Math.max(components, 4)];
        reset();
    }

    void reset() {
        for (int i = 0; i < model.components(); i++) {
            float channelDefault = model.defaultValue(i);
            value[i] = channelDefault;
            buffer[i] = channelDefault;
        }
        Arrays.fill(scratch, 0.0f);
        state = null;
        packedState = null;
        onChange = null;
        onCommit = null;
        onPackedChange = null;
        onPackedCommit = null;
    }

    Model model() {
        return model;
    }

    int components() {
        return model.components();
    }

    float[] valueBuffer() {
        return value;
    }

    float[] uiBuffer() {
        return buffer;
    }

    float[] scratch() {
        return scratch;
    }

    float[] snapshotColor() {
        return Arrays.copyOf(value, model.components());
    }

    int snapshotPacked() {
        return model.pack(value);
    }

    @Nullable
    State<float[]> getState() {
        return state;
    }

    void setState(@Nullable State<float[]> state) {
        this.state = state;
        if (state != null) {
            float[] snapshot = state.get();
            if (snapshot != null) {
                setInternalColor(snapshot, UpdateOrigin.STATE_FLOAT, false);
            }
        }
    }

    @Nullable
    State<Integer> getPackedState() {
        return packedState;
    }

    void setPackedState(@Nullable State<Integer> state) {
        this.packedState = state;
        if (state != null) {
            Integer snapshot = state.get();
            if (snapshot != null) {
                model.unpack(snapshot, scratch);
                setInternalColor(scratch, UpdateOrigin.STATE_PACKED, false);
            }
        }
    }

    void onChange(@Nullable Consumer<float[]> consumer) {
        onChange = consumer;
    }

    void onCommit(@Nullable Consumer<float[]> consumer) {
        onCommit = consumer;
    }

    void onPackedChange(@Nullable Consumer<Integer> consumer) {
        onPackedChange = consumer;
    }

    void onPackedCommit(@Nullable Consumer<Integer> consumer) {
        onPackedCommit = consumer;
    }

    boolean setColor(float[] source, UpdateOrigin origin) {
        return setInternalColor(source, origin, true);
    }

    boolean refreshFromStates() {
        boolean changed = false;
        if (state != null) {
            float[] snapshot = state.get();
            if (snapshot != null) {
                changed |= setInternalColor(snapshot, UpdateOrigin.STATE_FLOAT, false);
            }
        }
        if (packedState != null) {
            Integer snapshot = packedState.get();
            if (snapshot != null) {
                model.unpack(snapshot, scratch);
                changed |= setInternalColor(scratch, UpdateOrigin.STATE_PACKED, false);
            }
        }
        return changed;
    }

    void pushStates() {
        if (state != null) {
            state.set(snapshotColor());
        }
        if (packedState != null) {
            packedState.set(model.pack(value));
        }
    }

    void notifyChange() {
        if (onChange != null) {
            onChange.accept(snapshotColor());
        }
        if (onPackedChange != null) {
            onPackedChange.accept(model.pack(value));
        }
    }

    void notifyCommit() {
        if (onCommit != null) {
            onCommit.accept(snapshotColor());
        }
        if (onPackedCommit != null) {
            onPackedCommit.accept(model.pack(value));
        }
    }

    private boolean setInternalColor(float[] source, UpdateOrigin origin, boolean notify) {
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
                pushStates();
                if (notify) {
                    notifyChange();
                }
            }
            return false;
        }
        System.arraycopy(value, 0, buffer, 0, model.components());
        if (origin == UpdateOrigin.INTERACTION) {
            pushStates();
            if (notify) {
                notifyChange();
            }
        } else if (origin == UpdateOrigin.PROGRAMMATIC) {
            pushStates();
        }
        return true;
    }

    enum Model {
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

        Model(int components) {
            this.components = components;
        }

        int components() {
            return components;
        }

        abstract float defaultValue(int component);

        abstract int pack(float[] color);

        abstract void unpack(int packed, float[] target);
    }

    enum UpdateOrigin {
        STATE_FLOAT,
        STATE_PACKED,
        PROGRAMMATIC,
        INTERACTION
    }
}

