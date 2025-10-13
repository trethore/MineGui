package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class MGInputNumber<T extends Number> extends MGComponent<MGInputNumber<T>>
        implements Disableable<MGInputNumber<T>>, Stateful<T[], MGInputNumber<T>>, Scalable<MGInputNumber<T>>, Sizable<MGInputNumber<T>> {

    private static final int MAX_COMPONENTS = 4;

    private final Class<T> typeClass;
    private final String defaultLabel = "##MGInputNumber_" + UUID.randomUUID();
    private final int componentCount;
    private final double[] pendingValuesBuffer;
    private String label = defaultLabel;
    private boolean disabled;
    private float scale = 1.0f;
    private double[] values;
    private double step;
    private double fastStep;
    private String format;
    private int userFlags;
    @Nullable
    private State<T[]> state;
    @Nullable
    private Consumer<T[]> stateListener;
    @Nullable
    private State<T> singleState;
    @Nullable
    private Consumer<T> singleStateListener;
    private boolean suppressStateCallback;
    private boolean suppressSingleStateCallback;
    @Nullable
    private Consumer<T[]> onChange;
    @Nullable
    private Consumer<T[]> onCommit;

    @Nullable
    private ImInt intBuffer;
    private int[] intArrayBuffer;
    @Nullable
    private ImFloat floatBuffer;
    private float[] floatArrayBuffer;
    @Nullable
    private ImDouble doubleBuffer;

    private MGInputNumber(Class<T> typeClass, int components) {
        this.typeClass = typeClass;
        this.componentCount = Math.max(1, Math.min(MAX_COMPONENTS, components));
        if (typeClass == Double.class && this.componentCount != 1) {
            throw new IllegalArgumentException("InputDouble only supports a single component");
        }
        this.values = new double[this.componentCount];
        this.pendingValuesBuffer = new double[this.componentCount];
        this.format = defaultFormatFor(typeClass);
        if (typeClass == Integer.class) {
            this.step = 1.0;
            this.fastStep = 100.0;
        } else {
            this.step = 0.0;
            this.fastStep = 0.0;
        }
        this.userFlags = 0;
        initializeBuffers();
    }

    public static MGInputNumber<Integer> ofInt() {
        return new MGInputNumber<>(Integer.class, 1);
    }

    public static MGInputNumber<Integer> ofIntComponents(int components) {
        return new MGInputNumber<>(Integer.class, components);
    }

    public static MGInputNumber<Integer> ofInt(State<Integer> state) {
        MGInputNumber<Integer> field = ofInt();
        field.bindSingleState(state);
        return field;
    }

    public static MGInputNumber<Float> ofFloat() {
        return new MGInputNumber<>(Float.class, 1);
    }

    public static MGInputNumber<Float> ofFloatComponents(int components) {
        return new MGInputNumber<>(Float.class, components);
    }

    public static MGInputNumber<Float> ofFloat(State<Float> state) {
        MGInputNumber<Float> field = ofFloat();
        field.bindSingleState(state);
        return field;
    }

    public static MGInputNumber<Double> ofDouble() {
        return new MGInputNumber<>(Double.class, 1);
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

    private void initializeBuffers() {
        if (typeClass == Integer.class) {
            if (componentCount == 1) {
                intBuffer = new ImInt();
            } else {
                intArrayBuffer = new int[componentCount];
            }
        } else if (typeClass == Float.class) {
            if (componentCount == 1) {
                floatBuffer = new ImFloat();
            } else {
                floatArrayBuffer = new float[componentCount];
            }
        } else if (typeClass == Double.class) {
            doubleBuffer = new ImDouble();
        }
    }

    public MGInputNumber<T> label(String label) {
        this.label = label != null && !label.isBlank() ? label : defaultLabel;
        return self();
    }

    public MGInputNumber<T> flags(int flags) {
        this.userFlags = flags;
        return self();
    }

    public MGInputNumber<T> addFlags(int flags) {
        this.userFlags |= flags;
        return self();
    }

    public MGInputNumber<T> removeFlags(int flags) {
        this.userFlags &= ~flags;
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
        this.step = value != null ? toDouble(value) : 0.0;
        return self();
    }

    public MGInputNumber<T> fastStep(T value) {
        this.fastStep = value != null ? toDouble(value) : 0.0;
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
        double[] newValues = new double[]{value != null ? toDouble(value) : 0.0};
        setInternalValues(newValues, false, true);
        return self();
    }

    @SafeVarargs
    public final MGInputNumber<T> values(T... values) {
        if (values == null) {
            double[] zeros = new double[componentCount];
            setInternalValues(zeros, false, true);
            return self();
        }
        if (values.length != componentCount) {
            throw new IllegalArgumentException("Expected " + componentCount + " values but got " + values.length);
        }
        for (int i = 0; i < componentCount; i++) {
            pendingValuesBuffer[i] = values[i] != null ? toDouble(values[i]) : 0.0;
        }
        setInternalValues(pendingValuesBuffer, false, true);
        return self();
    }

    public MGInputNumber<T> onChange(Consumer<T[]> consumer) {
        this.onChange = consumer;
        return self();
    }

    public MGInputNumber<T> onCommit(Consumer<T[]> consumer) {
        this.onCommit = consumer;
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
        if (Objects.equals(this.state, state)) {
            return;
        }
        if (this.state != null && stateListener != null) {
            this.state.removeListener(stateListener);
        }
        this.state = state;
        if (state == null) {
            stateListener = null;
            return;
        }
        stateListener = newValue -> {
            if (suppressStateCallback) {
                return;
            }
            double[] source = new double[componentCount];
            T[] incoming = adjustIncomingArray(newValue);
            for (int i = 0; i < componentCount; i++) {
                source[i] = incoming[i] != null ? toDouble(incoming[i]) : 0.0;
            }
            setInternalValues(source, true, false);
        };
        state.addListener(stateListener);
        T[] current = adjustIncomingArray(state.get());
        double[] initial = new double[componentCount];
        for (int i = 0; i < componentCount; i++) {
            initial[i] = current[i] != null ? toDouble(current[i]) : 0.0;
        }
        setInternalValues(initial, true, false);
    }

    private void bindSingleState(@Nullable State<T> state) {
        if (Objects.equals(this.singleState, state)) {
            return;
        }
        if (this.singleState != null && singleStateListener != null) {
            this.singleState.removeListener(singleStateListener);
        }
        this.singleState = state;
        if (state == null) {
            singleStateListener = null;
            return;
        }
        singleStateListener = newValue -> {
            if (suppressSingleStateCallback) {
                return;
            }
            double[] source = new double[]{newValue != null ? toDouble(newValue) : 0.0};
            setInternalValues(source, true, false);
        };
        state.addListener(singleStateListener);
        double[] initial = new double[]{state.get() != null ? toDouble(state.get()) : 0.0};
        setInternalValues(initial, true, false);
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
    public void render() {
        beginRenderLifecycle();
        float frameHeight = ImGui.getFrameHeight();
        float componentWidth = frameHeight * 4.0f;
        updateBuffersFromValues();
        boolean scaled = scale != 1.0f;
        boolean disabledScope = disabled;
        final boolean[] activation = new boolean[1];
        withLayout(componentWidth * componentCount, frameHeight, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                activation[0] = renderWidget();
            } finally {
                if (disabledScope) {
                    ImGui.endDisabled();
                }
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
        });
        boolean activated = activation[0];

        if (activated && onCommit != null) {
            onCommit.accept(copyTypedArray());
        }

        renderChildren();
        endRenderLifecycle();
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
                double[] next = new double[]{intBuffer.get()};
                setInternalValues(next, false, true);
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
                pendingValuesBuffer[i] = intArrayBuffer[i];
            }
            setInternalValues(pendingValuesBuffer, false, true);
        }
        return activated;
    }

    private boolean renderFloatWidget() {
        if (componentCount == 1 && floatBuffer != null) {
            float stepValue = (float) step;
            float fastStepValue = (float) fastStep;
            boolean activated = ImGui.inputFloat(label, floatBuffer, stepValue, fastStepValue, format, userFlags);
            if (activated) {
                double[] next = new double[]{floatBuffer.get()};
                setInternalValues(next, false, true);
            }
            return activated;
        }
        if (floatArrayBuffer == null) {
            return false;
        }
        boolean activated;
        if (componentCount == 2) {
            activated = userFlags != 0
                    ? ImGui.inputFloat2(label, floatArrayBuffer, format, userFlags)
                    : ImGui.inputFloat2(label, floatArrayBuffer, format);
        } else if (componentCount == 3) {
            activated = userFlags != 0
                    ? ImGui.inputFloat3(label, floatArrayBuffer, format, userFlags)
                    : ImGui.inputFloat3(label, floatArrayBuffer, format);
        } else {
            activated = userFlags != 0
                    ? ImGui.inputFloat4(label, floatArrayBuffer, format, userFlags)
                    : ImGui.inputFloat4(label, floatArrayBuffer, format);
        }
        if (activated) {
            for (int i = 0; i < componentCount; i++) {
                pendingValuesBuffer[i] = floatArrayBuffer[i];
            }
            setInternalValues(pendingValuesBuffer, false, true);
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
            double[] next = new double[]{doubleBuffer.get()};
            setInternalValues(next, false, true);
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

    private void setInternalValues(double[] source, boolean fromState, boolean notifyChange) {
        double[] sanitized = sanitizeValues(source);
        boolean changed = !DoubleArray.equals(values, sanitized, componentCount);
        if (!changed) {
            if (fromState) {
                syncStateIfNeeded(sanitized);
            }
            return;
        }
        this.values = Arrays.copyOf(sanitized, componentCount);
        updateBuffersFromValues();
        T[] typedSnapshot = copyTypedArray();
        if (!fromState && state != null) {
            suppressStateCallback = true;
            state.set(Arrays.copyOf(typedSnapshot, typedSnapshot.length));
            suppressStateCallback = false;
        } else if (fromState && state != null) {
            T[] current = state.get();
            if (!Arrays.equals(adjustIncomingArray(current), typedSnapshot)) {
                suppressStateCallback = true;
                state.set(Arrays.copyOf(typedSnapshot, typedSnapshot.length));
                suppressStateCallback = false;
            }
        }
        if (!fromState && singleState != null && componentCount == 1) {
            suppressSingleStateCallback = true;
            singleState.set(typedSnapshot[0]);
            suppressSingleStateCallback = false;
        } else if (fromState && singleState != null && componentCount == 1) {
            T current = singleState.get();
            if (!Objects.equals(current, typedSnapshot[0])) {
                suppressSingleStateCallback = true;
                singleState.set(typedSnapshot[0]);
                suppressSingleStateCallback = false;
            }
        }
        if (notifyChange && onChange != null) {
            onChange.accept(Arrays.copyOf(typedSnapshot, typedSnapshot.length));
        }
    }

    private void syncStateIfNeeded(double[] sanitized) {
        if (state != null) {
            T[] typedSnapshot = copyTypedArray();
            T[] current = adjustIncomingArray(state.get());
            if (!Arrays.equals(current, typedSnapshot)) {
                suppressStateCallback = true;
                state.set(Arrays.copyOf(typedSnapshot, typedSnapshot.length));
                suppressStateCallback = false;
            }
        }
        if (singleState != null && componentCount == 1) {
            T value = castToType(sanitized[0]);
            if (!Objects.equals(singleState.get(), value)) {
                suppressSingleStateCallback = true;
                singleState.set(value);
                suppressSingleStateCallback = false;
            }
        }
    }

    private double[] sanitizeValues(double[] source) {
        double[] result = new double[componentCount];
        for (int i = 0; i < componentCount; i++) {
            double value = i < source.length ? source[i] : 0.0;
            if (typeClass == Integer.class) {
                double clamped = Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
                result[i] = Math.round(clamped);
            } else if (typeClass == Float.class) {
                float f = (float) value;
                result[i] = Float.isFinite(f) ? f : 0.0f;
            } else if (typeClass == Double.class) {
                result[i] = Double.isFinite(value) ? value : 0.0;
            } else {
                result[i] = value;
            }
        }
        return result;
    }

    private double toDouble(Number value) {
        if (value == null) {
            return 0.0;
        }
        return value.doubleValue();
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

    @SuppressWarnings("unchecked")
    private T[] adjustIncomingArray(@Nullable T[] source) {
        T[] result = (T[]) Array.newInstance(typeClass, componentCount);
        if (source == null) {
            return result;
        }
        int limit = Math.min(componentCount, source.length);
        System.arraycopy(source, 0, result, 0, limit);
        return result;
    }

    private static final class DoubleArray {
        private DoubleArray() {
        }

        private static boolean equals(double[] a, double[] b, int components) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            if (a.length < components || b.length < components) {
                return false;
            }
            for (int i = 0; i < components; i++) {
                if (Double.doubleToLongBits(a[i]) != Double.doubleToLongBits(b[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}
