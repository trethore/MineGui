package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public final class MGInputNumber<T extends Number> extends MGComponent<MGInputNumber<T>>
        implements Disableable<MGInputNumber<T>>, Stateful<T[], MGInputNumber<T>>, Scalable<MGInputNumber<T>>, Sizable<MGInputNumber<T>> {

    private static final int MAX_COMPONENTS = 4;
    private static final ComponentPool<MGInputNumber<Integer>>[] INT_POOLS = createIntPools();
    private static final ComponentPool<MGInputNumber<Float>>[] FLOAT_POOLS = createFloatPools();
    private static final ComponentPool<MGInputNumber<Double>> DOUBLE_POOL =
            new ComponentPool<>(() -> new MGInputNumber<>(Double.class, 1), MGInputNumber::prepare);

    private final Class<T> typeClass;
    private final String defaultLabel;
    private final int componentCount;
    private final double[] values;
    private final double[] scratchValues;
    private final int[] intArrayBuffer;
    private final float[] floatArrayBuffer;
    private final ImInt intBuffer;
    private final ImFloat floatBuffer;
    private final ImDouble doubleBuffer;

    private String label;
    private boolean disabled;
    private float scale;
    private double step;
    private double fastStep;
    private String format;
    private int userFlags;
    @Nullable
    private State<T[]> state;
    @Nullable
    private State<T> singleState;
    @Nullable
    private Consumer<T[]> onChange;
    @Nullable
    private Consumer<T[]> onCommit;
    private boolean layoutActivated;

    private MGInputNumber(Class<T> typeClass, int components) {
        this.typeClass = typeClass;
        this.componentCount = Math.max(1, Math.min(MAX_COMPONENTS, components));
        if (typeClass == Double.class && this.componentCount != 1) {
            throw new IllegalArgumentException("InputDouble only supports a single component");
        }
        this.defaultLabel = "##MGInputNumber_" + UUID.randomUUID();
        this.label = defaultLabel;
        this.values = new double[this.componentCount];
        this.scratchValues = new double[this.componentCount];
        if (typeClass == Integer.class) {
            this.intArrayBuffer = this.componentCount > 1 ? new int[this.componentCount] : null;
            this.floatArrayBuffer = null;
            this.intBuffer = this.componentCount == 1 ? new ImInt() : null;
            this.floatBuffer = null;
            this.doubleBuffer = null;
        } else if (typeClass == Float.class) {
            this.intArrayBuffer = null;
            this.floatArrayBuffer = this.componentCount > 1 ? new float[this.componentCount] : null;
            this.intBuffer = null;
            this.floatBuffer = this.componentCount == 1 ? new ImFloat() : null;
            this.doubleBuffer = null;
        } else if (typeClass == Double.class) {
            this.intArrayBuffer = null;
            this.floatArrayBuffer = null;
            this.intBuffer = null;
            this.floatBuffer = null;
            this.doubleBuffer = new ImDouble();
        } else {
            throw new IllegalArgumentException("Unsupported numeric type: " + typeClass.getName());
        }
        prepare();
    }

    static ComponentPool<MGInputNumber<Integer>>[] createIntPools() {
        @SuppressWarnings("unchecked")
        ComponentPool<MGInputNumber<Integer>>[] pools = (ComponentPool<MGInputNumber<Integer>>[]) new ComponentPool<?>[MAX_COMPONENTS];
        for (int i = 1; i <= MAX_COMPONENTS; i++) {
            final int componentCount = i;
            pools[i - 1] = new ComponentPool<>(() -> new MGInputNumber<>(Integer.class, componentCount), MGInputNumber::prepare);
        }
        return pools;
    }

    static ComponentPool<MGInputNumber<Float>>[] createFloatPools() {
        @SuppressWarnings("unchecked")
        ComponentPool<MGInputNumber<Float>>[] pools = (ComponentPool<MGInputNumber<Float>>[]) new ComponentPool<?>[MAX_COMPONENTS];
        for (int i = 1; i <= MAX_COMPONENTS; i++) {
            final int componentCount = i;
            pools[i - 1] = new ComponentPool<>(() -> new MGInputNumber<>(Float.class, componentCount), MGInputNumber::prepare);
        }
        return pools;
    }

    static MGInputNumber<Integer> acquireInt(int components) {
        if (components < 1 || components > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Components must be between 1 and " + MAX_COMPONENTS);
        }
        return INT_POOLS[components - 1].acquire();
    }

    static MGInputNumber<Float> acquireFloat(int components) {
        if (components < 1 || components > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Components must be between 1 and " + MAX_COMPONENTS);
        }
        return FLOAT_POOLS[components - 1].acquire();
    }

    static MGInputNumber<Double> acquireDouble() {
        return DOUBLE_POOL.acquire();
    }

    public static MGInputNumber<Integer> ofInt() {
        return acquireInt(1);
    }

    public static MGInputNumber<Integer> ofIntComponents(int components) {
        return acquireInt(components);
    }

    public static MGInputNumber<Integer> ofInt(State<Integer> state) {
        MGInputNumber<Integer> field = ofInt();
        field.bindSingleState(state);
        return field;
    }

    public static MGInputNumber<Float> ofFloat() {
        return acquireFloat(1);
    }

    public static MGInputNumber<Float> ofFloatComponents(int components) {
        return acquireFloat(components);
    }

    public static MGInputNumber<Float> ofFloat(State<Float> state) {
        MGInputNumber<Float> field = ofFloat();
        field.bindSingleState(state);
        return field;
    }

    public static MGInputNumber<Double> ofDouble() {
        return acquireDouble();
    }

    public static MGInputNumber<Double> ofDouble(State<Double> state) {
        MGInputNumber<Double> field = ofDouble();
        field.bindSingleState(state);
        return field;
    }

    private static String defaultFormatFor(Class<?> type) {
        if (type == Integer.class) {
            return "%d";
        } else if (type == Float.class) {
            return "%.3f";
        } else if (type == Double.class) {
            return "%.6f";
        }
        return "%s";
    }

    void prepare() {
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        format = defaultFormatFor(typeClass);
        userFlags = 0;
        onChange = null;
        onCommit = null;
        state = null;
        singleState = null;
        Arrays.fill(values, 0.0);
        Arrays.fill(scratchValues, 0.0);
        if (typeClass == Integer.class) {
            step = 1.0;
            fastStep = 100.0;
        } else {
            step = 0.0;
            fastStep = 0.0;
        }
        updateBuffersFromValues();
    }

    public MGInputNumber<T> label(String label) {
        this.label = label != null && !label.isBlank() ? label : defaultLabel;
        return self();
    }

    public MGInputNumber<T> flags(int flags) {
        userFlags = flags;
        return self();
    }

    public MGInputNumber<T> addFlags(int flags) {
        userFlags |= flags;
        return self();
    }

    public MGInputNumber<T> removeFlags(int flags) {
        userFlags &= ~flags;
        return self();
    }

    public MGInputNumber<T> submitOnEnter(boolean enabled) {
        if (enabled) {
            addFlags(ImGuiInputTextFlags.EnterReturnsTrue);
        } else {
            removeFlags(ImGuiInputTextFlags.EnterReturnsTrue);
        }
        return self();
    }

    public MGInputNumber<T> readOnly(boolean enabled) {
        if (enabled) {
            addFlags(ImGuiInputTextFlags.ReadOnly);
        } else {
            removeFlags(ImGuiInputTextFlags.ReadOnly);
        }
        return self();
    }

    public MGInputNumber<T> step(T value) {
        step = value != null ? toDouble(value) : 0.0;
        return self();
    }

    public MGInputNumber<T> fastStep(T value) {
        fastStep = value != null ? toDouble(value) : 0.0;
        return self();
    }

    public MGInputNumber<T> format(String format) {
        this.format = format != null && !format.isBlank() ? format : defaultFormatFor(typeClass);
        return self();
    }

    public MGInputNumber<T> value(T value) {
        if (componentCount != 1) {
            throw new IllegalStateException("Single value setter requires component count of 1");
        }
        scratchValues[0] = value != null ? toDouble(value) : 0.0;
        setInternalValues(scratchValues);
        return self();
    }

    @SafeVarargs
    public final MGInputNumber<T> values(T... values) {
        if (values == null) {
            Arrays.fill(scratchValues, 0.0);
        } else {
            if (values.length != componentCount) {
                throw new IllegalArgumentException("Expected " + componentCount + " values but got " + values.length);
            }
            for (int i = 0; i < componentCount; i++) {
                scratchValues[i] = values[i] != null ? toDouble(values[i]) : 0.0;
            }
        }
        setInternalValues(scratchValues);
        return self();
    }

    public MGInputNumber<T> onChange(Consumer<T[]> consumer) {
        onChange = consumer;
        return self();
    }

    public MGInputNumber<T> onCommit(Consumer<T[]> consumer) {
        onCommit = consumer;
        return self();
    }

    public T[] snapshotValues() {
        return copyTypedArray();
    }

    public T snapshotValue() {
        if (componentCount != 1) {
            throw new IllegalStateException("Single value access requires component count of 1");
        }
        return castToType(values[0]);
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
    public State<T[]> getState() {
        return state;
    }

    @Override
    public void setState(@Nullable State<T[]> state) {
        this.state = state;
        if (state != null) {
            syncArrayState();
            updateBuffersFromValues();
        }
    }

    private void bindSingleState(@Nullable State<T> state) {
        this.singleState = state;
        if (state != null) {
            double incoming = state.get() != null ? toDouble(state.get()) : 0.0;
            values[0] = incoming;
            updateBuffersFromValues();
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
        boolean updated = syncArrayState();
        updated |= syncSingleState();
        if (updated) {
            updateBuffersFromValues();
        }

        float frameHeight = ImGui.getFrameHeight();
        float componentWidth = frameHeight * 4.0f;
        boolean scaled = scale != 1.0f;
        boolean disabledScope = disabled;
        layoutActivated = false;
        withLayout(componentWidth * componentCount, frameHeight, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                layoutActivated = renderWidget();
            } finally {
                if (disabledScope) {
                    ImGui.endDisabled();
                }
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
        });
        boolean activated = layoutActivated;

        if (activated && onCommit != null) {
            onCommit.accept(copyTypedArray());
        }
    }

    private boolean renderWidget() {
        if (typeClass == Integer.class) {
            return renderIntWidget();
        } else if (typeClass == Float.class) {
            return renderFloatWidget();
        } else if (typeClass == Double.class) {
            return renderDoubleWidget();
        }
        return false;
    }

    private boolean renderIntWidget() {
        if (componentCount == 1 && intBuffer != null) {
            int stepValue = (int) Math.round(step);
            int fastStepValue = (int) Math.round(fastStep);
            boolean activated = ImGui.inputInt(label, intBuffer, stepValue, fastStepValue, userFlags);
            if (activated) {
                scratchValues[0] = intBuffer.get();
                setInternalValues(scratchValues);
            }
            return activated;
        }
        if (intArrayBuffer == null) {
            return false;
        }
        boolean activated;
        if (componentCount == 2) {
            activated = userFlags != 0 ? ImGui.inputInt2(label, intArrayBuffer, userFlags) : ImGui.inputInt2(label, intArrayBuffer);
        } else if (componentCount == 3) {
            activated = userFlags != 0 ? ImGui.inputInt3(label, intArrayBuffer, userFlags) : ImGui.inputInt3(label, intArrayBuffer);
        } else {
            activated = userFlags != 0 ? ImGui.inputInt4(label, intArrayBuffer, userFlags) : ImGui.inputInt4(label, intArrayBuffer);
        }
        if (activated) {
            for (int i = 0; i < componentCount; i++) {
                scratchValues[i] = intArrayBuffer[i];
            }
            setInternalValues(scratchValues);
        }
        return activated;
    }

    private boolean renderFloatWidget() {
        if (componentCount == 1 && floatBuffer != null) {
            float stepValue = (float) step;
            float fastStepValue = (float) fastStep;
            boolean activated = ImGui.inputFloat(label, floatBuffer, stepValue, fastStepValue, format, userFlags);
            if (activated) {
                scratchValues[0] = floatBuffer.get();
                setInternalValues(scratchValues);
            }
            return activated;
        }
        if (floatArrayBuffer == null) {
            return false;
        }
        boolean activated;
        if (componentCount == 2) {
            activated = userFlags != 0 ? ImGui.inputFloat2(label, floatArrayBuffer, format, userFlags) : ImGui.inputFloat2(label, floatArrayBuffer, format);
        } else if (componentCount == 3) {
            activated = userFlags != 0 ? ImGui.inputFloat3(label, floatArrayBuffer, format, userFlags) : ImGui.inputFloat3(label, floatArrayBuffer, format);
        } else {
            activated = userFlags != 0 ? ImGui.inputFloat4(label, floatArrayBuffer, format, userFlags) : ImGui.inputFloat4(label, floatArrayBuffer, format);
        }
        if (activated) {
            for (int i = 0; i < componentCount; i++) {
                scratchValues[i] = floatArrayBuffer[i];
            }
            setInternalValues(scratchValues);
        }
        return activated;
    }

    private boolean renderDoubleWidget() {
        if (doubleBuffer == null) {
            return false;
        }
        double stepValue = step;
        double fastStepValue = fastStep;
        boolean activated = ImGui.inputDouble(label, doubleBuffer, stepValue, fastStepValue, format, userFlags);
        if (activated) {
            scratchValues[0] = doubleBuffer.get();
            setInternalValues(scratchValues);
        }
        return activated;
    }

    private void updateBuffersFromValues() {
        if (typeClass == Integer.class) {
            if (componentCount == 1 && intBuffer != null) {
                int value = (int) Math.round(values[0]);
                if (intBuffer.get() != value) {
                    intBuffer.set(value);
                }
            } else if (intArrayBuffer != null) {
                for (int i = 0; i < componentCount; i++) {
                    intArrayBuffer[i] = (int) Math.round(values[i]);
                }
            }
        } else if (typeClass == Float.class) {
            if (componentCount == 1 && floatBuffer != null) {
                float value = (float) values[0];
                if (floatBuffer.get() != value) {
                    floatBuffer.set(value);
                }
            } else if (floatArrayBuffer != null) {
                for (int i = 0; i < componentCount; i++) {
                    floatArrayBuffer[i] = (float) values[i];
                }
            }
        } else if (typeClass == Double.class) {
            if (doubleBuffer != null) {
                double value = values[0];
                if (doubleBuffer.get() != value) {
                    doubleBuffer.set(value);
                }
            }
        }
    }

    private void setInternalValues(double[] source) {
        boolean changed = false;
        for (int i = 0; i < componentCount; i++) {
            double sanitized = sanitizeValue(i < source.length ? source[i] : 0.0);
            if (Double.doubleToLongBits(values[i]) != Double.doubleToLongBits(sanitized)) {
                values[i] = sanitized;
                changed = true;
            }
        }
        if (!changed) {
            pushValuesToStates();
            return;
        }
        updateBuffersFromValues();
        pushValuesToStates();
        if (onChange != null) {
            onChange.accept(copyTypedArray());
        }
    }

    private void pushValuesToStates() {
        if (state != null) {
            state.set(copyTypedArray());
        }
        if (singleState != null && componentCount == 1) {
            singleState.set(castToType(values[0]));
        }
    }

    private boolean syncArrayState() {
        if (state == null) {
            return false;
        }
        T[] snapshot = state.get();
        boolean changed = false;
        for (int i = 0; i < componentCount; i++) {
            double incoming = snapshot != null && i < snapshot.length && snapshot[i] != null ? toDouble(snapshot[i]) : 0.0;
            if (Double.doubleToLongBits(values[i]) != Double.doubleToLongBits(incoming)) {
                values[i] = incoming;
                changed = true;
            }
        }
        return changed;
    }

    private boolean syncSingleState() {
        if (singleState == null || componentCount != 1) {
            return false;
        }
        T snapshot = singleState.get();
        double incoming = snapshot != null ? toDouble(snapshot) : 0.0;
        if (Double.doubleToLongBits(values[0]) != Double.doubleToLongBits(incoming)) {
            values[0] = incoming;
            return true;
        }
        return false;
    }

    private double sanitizeValue(double value) {
        if (typeClass == Integer.class) {
            double clamped = Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
            return Math.round(clamped);
        } else if (typeClass == Float.class) {
            float f = (float) value;
            return Float.isFinite(f) ? f : 0.0f;
        } else if (typeClass == Double.class) {
            return Double.isFinite(value) ? value : 0.0;
        }
        return value;
    }

    private double toDouble(Number value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    @SuppressWarnings("unchecked")
    private T castToType(double value) {
        if (typeClass == Integer.class) {
            return (T) Integer.valueOf((int) Math.round(value));
        } else if (typeClass == Float.class) {
            return (T) Float.valueOf((float) value);
        } else if (typeClass == Double.class) {
            return (T) Double.valueOf(value);
        }
        throw new IllegalStateException("Unsupported numeric type: " + typeClass.getName());
    }

    @SuppressWarnings("unchecked")
    private T[] copyTypedArray() {
        T[] array = (T[]) Array.newInstance(typeClass, componentCount);
        for (int i = 0; i < componentCount; i++) {
            array[i] = castToType(values[i]);
        }
        return array;
    }
}
