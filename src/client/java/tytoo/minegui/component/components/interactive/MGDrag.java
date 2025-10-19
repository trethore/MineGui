package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImDouble;
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
import java.util.function.Consumer;

public final class MGDrag<T extends Number> extends MGComponent<MGDrag<T>>
        implements Disableable<MGDrag<T>>, Stateful<T[], MGDrag<T>>, Scalable<MGDrag<T>>, Sizable<MGDrag<T>> {

    private static final int MAX_COMPONENTS = 4;
    private static final ComponentPool<MGDrag<Integer>>[] INT_POOLS = createIntPools();
    private static final ComponentPool<MGDrag<Float>>[] FLOAT_POOLS = createFloatPools();
    private static final ComponentPool<MGDrag<Double>> DOUBLE_POOL =
            new ComponentPool<>(() -> new MGDrag<>(DragKind.DOUBLE, 1, Double.class), MGDrag::prepare);

    private final DragKind kind;
    private final Class<T> typeClass;
    private final int componentCount;
    private final String defaultLabel;
    private final double[] values;
    private final double[] scratchValues;
    private final int[] intBuffer;
    private final float[] floatBuffer;
    private final ImDouble doubleBuffer;

    private String label;
    private boolean disabled;
    private float scale;
    private float speed;
    private boolean hasMin;
    private boolean hasMax;
    private double minValue;
    private double maxValue;
    private String format;
    private int flags;
    @Nullable
    private State<T[]> state;
    @Nullable
    private State<T> singleState;
    @Nullable
    private Consumer<T[]> onChange;
    @Nullable
    private Consumer<T[]> onCommit;
    private boolean layoutCommitFlag;

    private MGDrag(DragKind kind, int components, Class<T> typeClass) {
        this.kind = kind;
        this.componentCount = Math.max(1, Math.min(MAX_COMPONENTS, components));
        this.typeClass = typeClass;
        this.defaultLabel = "";
        this.values = new double[this.componentCount];
        this.scratchValues = new double[this.componentCount];
        if (kind == DragKind.INTEGER) {
            this.intBuffer = new int[this.componentCount];
            this.floatBuffer = null;
            this.doubleBuffer = null;
        } else if (kind == DragKind.FLOAT) {
            this.intBuffer = null;
            this.floatBuffer = new float[this.componentCount];
            this.doubleBuffer = null;
        } else if (kind == DragKind.DOUBLE) {
            if (this.componentCount != 1) {
                throw new IllegalArgumentException("Double drag only supports a single component");
            }
            this.intBuffer = null;
            this.floatBuffer = null;
            this.doubleBuffer = new ImDouble();
        } else {
            throw new IllegalArgumentException("Unsupported drag kind: " + kind);
        }
        prepare();
    }

    private static ComponentPool<MGDrag<Integer>>[] createIntPools() {
        @SuppressWarnings("unchecked")
        ComponentPool<MGDrag<Integer>>[] pools = (ComponentPool<MGDrag<Integer>>[]) new ComponentPool<?>[MAX_COMPONENTS];
        for (int i = 1; i <= MAX_COMPONENTS; i++) {
            final int components = i;
            pools[i - 1] = new ComponentPool<>(() -> new MGDrag<>(DragKind.INTEGER, components, Integer.class), MGDrag::prepare);
        }
        return pools;
    }

    private static ComponentPool<MGDrag<Float>>[] createFloatPools() {
        @SuppressWarnings("unchecked")
        ComponentPool<MGDrag<Float>>[] pools = (ComponentPool<MGDrag<Float>>[]) new ComponentPool<?>[MAX_COMPONENTS];
        for (int i = 1; i <= MAX_COMPONENTS; i++) {
            final int components = i;
            pools[i - 1] = new ComponentPool<>(() -> new MGDrag<>(DragKind.FLOAT, components, Float.class), MGDrag::prepare);
        }
        return pools;
    }

    private static MGDrag<Integer> acquireInt(int components) {
        if (components < 1 || components > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Components must be between 1 and " + MAX_COMPONENTS);
        }
        return INT_POOLS[components - 1].acquire();
    }

    private static MGDrag<Float> acquireFloat(int components) {
        if (components < 1 || components > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Components must be between 1 and " + MAX_COMPONENTS);
        }
        return FLOAT_POOLS[components - 1].acquire();
    }

    public static MGDrag<Integer> ofInt() {
        return acquireInt(1);
    }

    public static MGDrag<Integer> ofIntComponents(int components) {
        return acquireInt(components);
    }

    public static MGDrag<Integer> ofInt(State<Integer> state) {
        MGDrag<Integer> drag = ofInt();
        drag.bindSingleState(state);
        return drag;
    }

    public static MGDrag<Float> ofFloat() {
        return acquireFloat(1);
    }

    public static MGDrag<Float> ofFloatComponents(int components) {
        return acquireFloat(components);
    }

    public static MGDrag<Float> ofFloat(State<Float> state) {
        MGDrag<Float> drag = ofFloat();
        drag.bindSingleState(state);
        return drag;
    }

    public static MGDrag<Double> ofDouble() {
        return DOUBLE_POOL.acquire();
    }

    public static MGDrag<Double> ofDouble(State<Double> state) {
        MGDrag<Double> drag = ofDouble();
        drag.bindSingleState(state);
        return drag;
    }

    private void prepare() {
        Arrays.fill(values, 0.0);
        Arrays.fill(scratchValues, 0.0);
        if (intBuffer != null) {
            Arrays.fill(intBuffer, 0);
        }
        if (floatBuffer != null) {
            Arrays.fill(floatBuffer, 0.0f);
        }
        if (doubleBuffer != null) {
            doubleBuffer.set(0.0);
        }
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        speed = 1.0f;
        hasMin = false;
        hasMax = false;
        minValue = 0.0;
        maxValue = 0.0;
        format = defaultFormat();
        flags = 0;
        state = null;
        singleState = null;
        onChange = null;
        onCommit = null;
        layoutCommitFlag = false;
        updateBuffersFromValues();
    }

    public MGDrag<T> label(@Nullable String label) {
        this.label = label != null ? label : defaultLabel;
        return self();
    }

    public MGDrag<T> speed(float speed) {
        this.speed = speed;
        return self();
    }

    public MGDrag<T> format(@Nullable String format) {
        this.format = format != null && !format.isBlank() ? format : defaultFormat();
        return self();
    }

    public MGDrag<T> flags(int flags) {
        this.flags = flags;
        return self();
    }

    public MGDrag<T> addFlags(int flags) {
        this.flags |= flags;
        return self();
    }

    public MGDrag<T> removeFlags(int flags) {
        this.flags &= ~flags;
        return self();
    }

    public MGDrag<T> min(@Nullable T value) {
        if (value == null) {
            hasMin = false;
        } else {
            hasMin = true;
            minValue = toDouble(value);
            if (hasMax && minValue > maxValue) {
                double temp = minValue;
                minValue = maxValue;
                maxValue = temp;
            }
        }
        setInternalValues(values, true);
        return self();
    }

    public MGDrag<T> max(@Nullable T value) {
        if (value == null) {
            hasMax = false;
        } else {
            hasMax = true;
            maxValue = toDouble(value);
            if (hasMin && minValue > maxValue) {
                double temp = minValue;
                minValue = maxValue;
                maxValue = temp;
            }
        }
        setInternalValues(values, true);
        return self();
    }

    public MGDrag<T> range(T min, T max) {
        if (min == null && max == null) {
            hasMin = false;
            hasMax = false;
            return self();
        }
        double minVal = min != null ? toDouble(min) : Double.NEGATIVE_INFINITY;
        double maxVal = max != null ? toDouble(max) : Double.POSITIVE_INFINITY;
        if (!Double.isFinite(minVal)) {
            minVal = Double.NEGATIVE_INFINITY;
        }
        if (!Double.isFinite(maxVal)) {
            maxVal = Double.POSITIVE_INFINITY;
        }
        if (minVal > maxVal) {
            double temp = minVal;
            minVal = maxVal;
            maxVal = temp;
        }
        hasMin = Double.isFinite(minVal);
        hasMax = Double.isFinite(maxVal);
        minValue = hasMin ? minVal : 0.0;
        maxValue = hasMax ? maxVal : 0.0;
        setInternalValues(values, true);
        return self();
    }

    public MGDrag<T> value(T value) {
        if (componentCount != 1) {
            throw new IllegalStateException("Single value setter requires component count of 1");
        }
        scratchValues[0] = value != null ? toDouble(value) : 0.0;
        setInternalValues(scratchValues, false);
        return self();
    }

    @SafeVarargs
    public final MGDrag<T> values(@Nullable T... values) {
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
        setInternalValues(scratchValues, false);
        return self();
    }

    public MGDrag<T> onChange(@Nullable Consumer<T[]> consumer) {
        this.onChange = consumer;
        return self();
    }

    public MGDrag<T> onCommit(@Nullable Consumer<T[]> consumer) {
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
        this.state = state;
        if (state != null) {
            syncArrayState();
            updateBuffersFromValues();
        }
    }

    private void bindSingleState(@Nullable State<T> state) {
        this.singleState = state;
        if (state != null && componentCount == 1) {
            values[0] = state.get() != null ? toDouble(state.get()) : 0.0;
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
        syncArrayState();
        syncSingleState();
        updateBuffersFromValues();

        float frameHeight = ImGui.getFrameHeight();
        float componentWidth = frameHeight * 4.0f;
        boolean scaled = scale != 1.0f;
        boolean disabledScope = disabled;
        String widgetLabel = widgetLabel(label);
        layoutCommitFlag = false;
        withLayout(componentWidth * componentCount, frameHeight, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                boolean changed = renderWidget(widgetLabel);
                if (changed) {
                    layoutCommitFlag = true;
                }
            } finally {
                if (disabledScope) {
                    ImGui.endDisabled();
                }
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
        });

        if (layoutCommitFlag && onCommit != null) {
            onCommit.accept(copyTypedArray());
        }
    }

    private boolean renderWidget(String widgetLabel) {
        if (kind == DragKind.INTEGER) {
            return renderIntWidget(widgetLabel);
        } else if (kind == DragKind.FLOAT) {
            return renderFloatWidget(widgetLabel);
        } else if (kind == DragKind.DOUBLE) {
            return renderDoubleWidget(widgetLabel);
        }
        return false;
    }

    private boolean renderIntWidget(String widgetLabel) {
        if (intBuffer == null) {
            return false;
        }
        float dragSpeed = speed;
        int min = hasMin ? (int) Math.round(minValue) : 0;
        int max = hasMax ? (int) Math.round(maxValue) : 0;
        boolean changed;
        if (componentCount == 1) {
            changed = ImGui.dragInt(widgetLabel, intBuffer, dragSpeed, min, max, format);
        } else if (componentCount == 2) {
            changed = ImGui.dragInt2(widgetLabel, intBuffer, dragSpeed, min, max, format);
        } else if (componentCount == 3) {
            changed = ImGui.dragInt3(widgetLabel, intBuffer, dragSpeed, min, max, format);
        } else {
            changed = ImGui.dragInt4(widgetLabel, intBuffer, dragSpeed, min, max, format);
        }
        if (changed) {
            for (int i = 0; i < componentCount; i++) {
                scratchValues[i] = intBuffer[i];
            }
            setInternalValues(scratchValues, false);
        }
        return ImGui.isItemDeactivatedAfterEdit();
    }

    private boolean renderFloatWidget(String widgetLabel) {
        if (floatBuffer == null) {
            return false;
        }
        float dragSpeed = speed;
        float min = hasMin ? (float) minValue : 0.0f;
        float max = hasMax ? (float) maxValue : 0.0f;
        boolean changed;
        if (componentCount == 1) {
            changed = flags != 0 ? ImGui.dragFloat(widgetLabel, floatBuffer, dragSpeed, min, max, format, flags)
                    : ImGui.dragFloat(widgetLabel, floatBuffer, dragSpeed, min, max, format);
        } else if (componentCount == 2) {
            changed = flags != 0 ? ImGui.dragFloat2(widgetLabel, floatBuffer, dragSpeed, min, max, format, flags)
                    : ImGui.dragFloat2(widgetLabel, floatBuffer, dragSpeed, min, max, format);
        } else if (componentCount == 3) {
            changed = flags != 0 ? ImGui.dragFloat3(widgetLabel, floatBuffer, dragSpeed, min, max, format, flags)
                    : ImGui.dragFloat3(widgetLabel, floatBuffer, dragSpeed, min, max, format);
        } else {
            changed = flags != 0 ? ImGui.dragFloat4(widgetLabel, floatBuffer, dragSpeed, min, max, format, flags)
                    : ImGui.dragFloat4(widgetLabel, floatBuffer, dragSpeed, min, max, format);
        }
        if (changed) {
            for (int i = 0; i < componentCount; i++) {
                scratchValues[i] = floatBuffer[i];
            }
            setInternalValues(scratchValues, false);
        }
        return ImGui.isItemDeactivatedAfterEdit();
    }

    private boolean renderDoubleWidget(String widgetLabel) {
        if (doubleBuffer == null) {
            return false;
        }
        float dragSpeed = speed;
        double min = hasMin ? minValue : 0.0;
        double max = hasMax ? maxValue : 0.0;
        boolean changed = ImGui.dragScalar(widgetLabel, ImGuiDataType.Double, doubleBuffer, dragSpeed, min, max, format, flags);
        if (changed) {
            scratchValues[0] = doubleBuffer.get();
            setInternalValues(scratchValues, false);
        }
        return ImGui.isItemDeactivatedAfterEdit();
    }

    private void updateBuffersFromValues() {
        if (kind == DragKind.INTEGER && intBuffer != null) {
            for (int i = 0; i < componentCount; i++) {
                int rounded = (int) Math.round(values[i]);
                intBuffer[i] = rounded;
            }
        } else if (kind == DragKind.FLOAT && floatBuffer != null) {
            for (int i = 0; i < componentCount; i++) {
                float converted = (float) values[i];
                floatBuffer[i] = converted;
            }
        } else if (kind == DragKind.DOUBLE && doubleBuffer != null) {
            double current = values[0];
            if (doubleBuffer.get() != current) {
                doubleBuffer.set(current);
            }
        }
    }

    private boolean setInternalValues(double[] source, boolean fromState) {
        boolean changed = false;
        for (int i = 0; i < componentCount; i++) {
            double incoming = i < source.length ? source[i] : 0.0;
            double sanitized = sanitizeValue(incoming);
            if (Double.doubleToLongBits(values[i]) != Double.doubleToLongBits(sanitized)) {
                values[i] = sanitized;
                changed = true;
            }
        }
        if (changed) {
            updateBuffersFromValues();
        }
        if (!fromState) {
            pushValuesToStates();
            if (changed && onChange != null) {
                onChange.accept(copyTypedArray());
            }
        }
        return changed;
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
        for (int i = 0; i < componentCount; i++) {
            scratchValues[i] = snapshot != null && i < snapshot.length && snapshot[i] != null ? toDouble(snapshot[i]) : 0.0;
        }
        return setInternalValues(scratchValues, true);
    }

    private boolean syncSingleState() {
        if (singleState == null || componentCount != 1) {
            return false;
        }
        T snapshot = singleState.get();
        scratchValues[0] = snapshot != null ? toDouble(snapshot) : 0.0;
        return setInternalValues(scratchValues, true);
    }

    private double sanitizeValue(double value) {
        double adjusted = value;
        if (kind == DragKind.INTEGER) {
            double rounded = Math.round(adjusted);
            adjusted = Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, rounded));
        } else if (kind == DragKind.FLOAT) {
            float converted = (float) adjusted;
            adjusted = Float.isFinite(converted) ? converted : 0.0f;
        } else if (kind == DragKind.DOUBLE) {
            adjusted = Double.isFinite(adjusted) ? adjusted : 0.0;
        }
        if (hasMin) {
            adjusted = Math.max(minValue, adjusted);
        }
        if (hasMax) {
            adjusted = Math.min(maxValue, adjusted);
        }
        return adjusted;
    }

    private double toDouble(@Nullable Number value) {
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

    private String defaultFormat() {
        if (kind == DragKind.INTEGER) {
            return "%d";
        } else if (kind == DragKind.FLOAT) {
            return "%.3f";
        } else if (kind == DragKind.DOUBLE) {
            return "%.6f";
        }
        return "%s";
    }

    private enum DragKind {
        INTEGER,
        FLOAT,
        DOUBLE
    }
}
