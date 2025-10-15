package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.flag.ImGuiDataType;
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

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public class MGSlider<T> extends MGComponent<MGSlider<T>> implements Disableable<MGSlider<T>>,
        Stateful<T, MGSlider<T>>, Scalable<MGSlider<T>>, Sizable<MGSlider<T>> {

    private static final double DEFAULT_INT_MIN = 0.0;
    private static final double DEFAULT_INT_MAX = 100.0;
    private static final double DEFAULT_FLOAT_MIN = 0.0;
    private static final double DEFAULT_FLOAT_MAX = 1.0;
    private static final double DEFAULT_DOUBLE_MIN = 0.0;
    private static final double DEFAULT_DOUBLE_MAX = 1.0;
    private static final ComponentPool<MGSlider<Integer>> INT_POOL =
            new ComponentPool<>(() -> new MGSlider<>(SliderKind.INTEGER), MGSlider::prepare);
    private static final ComponentPool<MGSlider<Float>> FLOAT_POOL =
            new ComponentPool<>(() -> new MGSlider<>(SliderKind.FLOAT), MGSlider::prepare);
    private static final ComponentPool<MGSlider<Double>> DOUBLE_POOL =
            new ComponentPool<>(() -> new MGSlider<>(SliderKind.DOUBLE), MGSlider::prepare);
    private final SliderKind kind;
    private final String defaultLabel = "##MGSlider_" + UUID.randomUUID();
    private final ImInt intValue;
    private final ImFloat floatValue;
    private final ImDouble doubleValue;
    private String label = defaultLabel;
    private boolean disabled;
    private float scale = 1.0f;
    private int sliderFlags;
    private double minValue;
    private double maxValue;
    private double value;
    private String format;
    private DoubleUnaryOperator forwardTransform = value -> value;
    private DoubleUnaryOperator backwardTransform = value -> value;
    @Nullable
    private Function<Object, String> valueFormatter;
    @Nullable
    private EnumSelection currentEnumSelection;
    @Nullable
    private State<T> state;
    @Nullable
    private Consumer<T> onChange;
    @Nullable
    private Consumer<T> onCommit;
    @Nullable
    private Consumer<EnumSelection> onEnumChange;
    @Nullable
    private Consumer<EnumSelection> onEnumCommit;
    private Enum<?>[] enumValues;
    private int enumIndex;
    private boolean layoutCommitFlag;

    private MGSlider(SliderKind kind) {
        this.kind = kind;
        this.intValue = kind == SliderKind.INTEGER ? new ImInt() : null;
        this.floatValue = kind == SliderKind.FLOAT ? new ImFloat() : null;
        this.doubleValue = kind == SliderKind.DOUBLE ? new ImDouble() : null;
        if (kind == SliderKind.INTEGER) {
            minValue = DEFAULT_INT_MIN;
            maxValue = DEFAULT_INT_MAX;
            value = DEFAULT_INT_MIN;
            format = "%d";
        } else if (kind == SliderKind.FLOAT) {
            minValue = DEFAULT_FLOAT_MIN;
            maxValue = DEFAULT_FLOAT_MAX;
            value = DEFAULT_FLOAT_MIN;
            format = "%.3f";
        } else {
            minValue = DEFAULT_DOUBLE_MIN;
            maxValue = DEFAULT_DOUBLE_MAX;
            value = DEFAULT_DOUBLE_MIN;
            format = "%.6f";
        }
        currentEnumSelection = null;
        this.enumValues = new Enum<?>[0];
        this.enumIndex = 0;
    }

    private MGSlider(SliderKind kind, Enum<?>[] values) {
        this.kind = kind;
        this.intValue = new ImInt();
        this.floatValue = null;
        this.doubleValue = null;
        this.sliderFlags = 0;
        this.minValue = 0.0;
        this.maxValue = 0.0;
        this.value = 0.0;
        this.format = "%d";
        this.enumValues = values;
        if (values.length == 0) {
            throw new IllegalArgumentException("Enum slider requires at least one value");
        }
        this.enumIndex = 0;
        Enum<?> first = values[0];
        this.currentEnumSelection = new EnumSelection(0, first, first.name());
        this.valueFormatter = obj -> ((Enum<?>) obj).name();
    }

    public static MGSlider<Integer> ofInt() {
        return INT_POOL.acquire();
    }

    public static MGSlider<Integer> ofInt(State<Integer> state) {
        MGSlider<Integer> slider = ofInt();
        slider.setState(state);
        return slider;
    }

    public static MGSlider<Float> ofFloat() {
        return FLOAT_POOL.acquire();
    }

    public static MGSlider<Float> ofFloat(State<Float> state) {
        MGSlider<Float> slider = ofFloat();
        slider.setState(state);
        return slider;
    }

    public static MGSlider<Double> ofDouble() {
        return DOUBLE_POOL.acquire();
    }

    public static MGSlider<Double> ofDouble(State<Double> state) {
        MGSlider<Double> slider = ofDouble();
        slider.setState(state);
        return slider;
    }

    public static <E extends Enum<E>> MGSlider<E> ofEnum(Class<E> enumType) {
        return new MGSlider<>(SliderKind.ENUM, enumType.getEnumConstants());
    }

    public static <E extends Enum<E>> MGSlider<E> ofEnum(Class<E> enumType, State<E> state) {
        MGSlider<E> slider = ofEnum(enumType);
        slider.setState(state);
        return slider;
    }

    private void prepare() {
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        sliderFlags = 0;
        forwardTransform = DoubleUnaryOperator.identity();
        backwardTransform = DoubleUnaryOperator.identity();
        valueFormatter = null;
        onChange = null;
        onCommit = null;
        onEnumChange = null;
        onEnumCommit = null;
        state = null;
        currentEnumSelection = null;
        enumIndex = 0;
        if (kind == SliderKind.INTEGER) {
            minValue = DEFAULT_INT_MIN;
            maxValue = DEFAULT_INT_MAX;
            value = DEFAULT_INT_MIN;
            format = "%d";
        } else if (kind == SliderKind.FLOAT) {
            minValue = DEFAULT_FLOAT_MIN;
            maxValue = DEFAULT_FLOAT_MAX;
            value = DEFAULT_FLOAT_MIN;
            format = "%.3f";
        } else if (kind == SliderKind.DOUBLE) {
            minValue = DEFAULT_DOUBLE_MIN;
            maxValue = DEFAULT_DOUBLE_MAX;
            value = DEFAULT_DOUBLE_MIN;
            format = "%.6f";
        } else {
            if (enumValues.length > 0) {
                enumIndex = 0;
                updateEnumSelection();
            } else {
                currentEnumSelection = null;
            }
        }
        updateBuffersFromValue();
    }

    public MGSlider<T> label(@Nullable String label) {
        this.label = label != null && !label.isBlank() ? label : defaultLabel;
        return self();
    }

    public MGSlider<T> flags(int flags) {
        this.sliderFlags = flags;
        return self();
    }

    public MGSlider<T> addFlags(int flags) {
        this.sliderFlags |= flags;
        return self();
    }

    public MGSlider<T> removeFlags(int flags) {
        this.sliderFlags &= ~flags;
        return self();
    }

    public MGSlider<T> range(T min, T max) {
        ensureNumeric();
        double minValue = toDouble(min);
        double maxValue = toDouble(max);
        if (!Double.isFinite(minValue) || !Double.isFinite(maxValue)) {
            throw new IllegalArgumentException("Range values must be finite");
        }
        if (minValue > maxValue) {
            double temp = minValue;
            minValue = maxValue;
            maxValue = temp;
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        double clamped = clamp(value, minValue, maxValue);
        setInternalNumericValue(clamped, false, false);
        return self();
    }

    public MGSlider<T> format(@Nullable String format) {
        ensureNumeric();
        this.format = format != null && !format.isBlank() ? format : defaultFormat();
        return self();
    }

    public MGSlider<T> value(T value) {
        if (kind == SliderKind.ENUM) {
            setInternalEnumValue((Enum<?>) value, false);
        } else {
            setInternalNumericValue(toDouble(value), false, false);
        }
        return self();
    }

    public MGSlider<T> transform(DoubleUnaryOperator forward, DoubleUnaryOperator backward) {
        ensureTransformSupported();
        this.forwardTransform = forward != null ? forward : DoubleUnaryOperator.identity();
        this.backwardTransform = backward != null ? backward : DoubleUnaryOperator.identity();
        updateBuffersFromValue();
        return self();
    }

    public MGSlider<T> valueFormatter(@Nullable Function<T, String> formatter) {
        this.valueFormatter = formatter != null ? value -> formatter.apply(castType(value)) : null;
        return self();
    }

    public MGSlider<T> enumFormatter(Function<T, String> formatter) {
        ensureEnum();
        this.valueFormatter = formatter != null ? value -> formatter.apply(castType(value)) : value -> ((Enum<?>) value).name();
        updateEnumSelection();
        return self();
    }

    public MGSlider<T> enumValues(T[] values) {
        ensureEnum();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Enum slider requires at least one value");
        }
        this.enumValues = Arrays.copyOf(values, values.length, Enum[].class);
        this.enumIndex = Math.max(0, Math.min(enumIndex, enumValues.length - 1));
        updateBuffersFromValue();
        updateEnumSelection();
        return self();
    }

    public MGSlider<T> onChange(@Nullable Consumer<T> consumer) {
        this.onChange = consumer;
        return self();
    }

    public MGSlider<T> onCommit(@Nullable Consumer<T> consumer) {
        this.onCommit = consumer;
        return self();
    }

    public MGSlider<T> onEnumChange(@Nullable Consumer<EnumSelection> consumer) {
        ensureEnum();
        this.onEnumChange = consumer;
        return self();
    }

    public MGSlider<T> onEnumCommit(@Nullable Consumer<EnumSelection> consumer) {
        ensureEnum();
        this.onEnumCommit = consumer;
        return self();
    }

    public T snapshotValue() {
        if (kind == SliderKind.ENUM) {
            return castType(currentEnumSelection != null ? currentEnumSelection.value() : enumValues[enumIndex]);
        }
        return castNumeric(value);
    }

    public EnumSelection snapshotEnum() {
        ensureEnum();
        return currentEnumSelection;
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
        if (state == null) {
            return;
        }
        T current = state.get();
        if (kind == SliderKind.ENUM) {
            setInternalEnumValue((Enum<?>) current, true);
        } else {
            setInternalNumericValue(toDouble(current), true, false);
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

    private void refreshFromState() {
        if (state == null) {
            return;
        }
        T current = state.get();
        if (kind == SliderKind.ENUM) {
            setInternalEnumValue((Enum<?>) current, true);
        } else {
            setInternalNumericValue(toDouble(current), true, false);
        }
    }

    @Override
    protected void renderComponent() {
        refreshFromState();
        float frameHeight = ImGui.getFrameHeight();
        updateBuffersFromValue();

        boolean scaled = scale != 1.0f;
        boolean disabledScope = disabled;
        layoutCommitFlag = false;
        withLayout(frameHeight * 6.0f, frameHeight, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                if (kind == SliderKind.ENUM) {
                    renderEnumSlider();
                } else {
                    renderNumericSlider();
                }
                layoutCommitFlag = ImGui.isItemDeactivatedAfterEdit();
            } finally {
                if (disabledScope) {
                    ImGui.endDisabled();
                }
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
        });
        boolean committed = layoutCommitFlag;

        if (committed) {
            if (kind == SliderKind.ENUM) {
                notifyEnumCommit();
            } else if (onCommit != null) {
                onCommit.accept(snapshotValue());
            }
        }

        renderChildren();
    }

    private void renderNumericSlider() {
        if (kind == SliderKind.INTEGER) {
            double sliderMin = forwardTransform.applyAsDouble(minValue);
            double sliderMax = forwardTransform.applyAsDouble(maxValue);
            int min = (int) Math.round(sliderMin);
            int max = (int) Math.round(sliderMax);
            boolean activated;
            if (sliderFlags != 0) {
                activated = ImGui.sliderScalar(label, ImGuiDataType.S32, intValue, min, max, format, sliderFlags);
            } else {
                activated = ImGui.sliderScalar(label, ImGuiDataType.S32, intValue, min, max, format);
            }
            if (activated) {
                double raw = backwardTransform.applyAsDouble(intValue.get());
                setInternalNumericValue(raw, false, true);
            }
            renderFormattedValue();
        } else if (kind == SliderKind.FLOAT) {
            double sliderMin = forwardTransform.applyAsDouble(minValue);
            double sliderMax = forwardTransform.applyAsDouble(maxValue);
            float min = (float) sliderMin;
            float max = (float) sliderMax;
            boolean activated;
            if (sliderFlags != 0) {
                activated = ImGui.sliderScalar(label, ImGuiDataType.Float, floatValue, min, max, format, sliderFlags);
            } else {
                activated = ImGui.sliderScalar(label, ImGuiDataType.Float, floatValue, min, max, format);
            }
            if (activated) {
                double raw = backwardTransform.applyAsDouble(floatValue.get());
                setInternalNumericValue(raw, false, true);
            }
            renderFormattedValue();
        } else {
            double sliderMin = forwardTransform.applyAsDouble(minValue);
            double sliderMax = forwardTransform.applyAsDouble(maxValue);
            boolean activated;
            if (sliderFlags != 0) {
                activated = ImGui.sliderScalar(label, ImGuiDataType.Double, doubleValue, sliderMin, sliderMax, format, sliderFlags);
            } else {
                activated = ImGui.sliderScalar(label, ImGuiDataType.Double, doubleValue, sliderMin, sliderMax, format);
            }
            if (activated) {
                double raw = backwardTransform.applyAsDouble(doubleValue.get());
                setInternalNumericValue(raw, false, true);
            }
            renderFormattedValue();
        }
    }

    private void renderEnumSlider() {
        boolean activated = ImGui.sliderInt(label, intValue.getData(), 0, enumValues.length - 1, "");
        if (activated) {
            setInternalEnumIndex(intValue.get(), false, true);
        }
        renderFormattedValue();
    }

    private void renderFormattedValue() {
        if (kind != SliderKind.ENUM && valueFormatter == null) {
            return;
        }
        Object snapshot = kind == SliderKind.ENUM ? (currentEnumSelection != null ? currentEnumSelection.value() : enumValues[enumIndex]) : snapshotValue();
        String display;
        if (valueFormatter != null) {
            display = valueFormatter.apply(snapshot);
        } else {
            display = ((Enum<?>) snapshot).name();
        }
        ImGui.sameLine();
        ImGui.textUnformatted(display);
    }

    private void notifyNumericChange() {
        if (onChange != null) {
            onChange.accept(snapshotValue());
        }
    }

    private void notifyEnumChange() {
        if (onChange != null) {
            onChange.accept(snapshotValue());
        }
        if (onEnumChange != null && currentEnumSelection != null) {
            onEnumChange.accept(currentEnumSelection);
        }
    }

    private void notifyEnumCommit() {
        if (onCommit != null) {
            onCommit.accept(snapshotValue());
        }
        if (onEnumCommit != null && currentEnumSelection != null) {
            onEnumCommit.accept(currentEnumSelection);
        }
    }

    private void updateBuffersFromValue() {
        if (kind == SliderKind.ENUM) {
            if (intValue.get() != enumIndex) {
                intValue.set(enumIndex);
            }
            return;
        }
        if (kind == SliderKind.INTEGER && intValue != null) {
            int transformed = (int) Math.round(forwardTransform.applyAsDouble(value));
            if (intValue.get() != transformed) {
                intValue.set(transformed);
            }
        } else if (kind == SliderKind.FLOAT && floatValue != null) {
            float transformed = (float) forwardTransform.applyAsDouble(value);
            if (floatValue.get() != transformed) {
                floatValue.set(transformed);
            }
        } else if (doubleValue != null) {
            double transformed = forwardTransform.applyAsDouble(value);
            if (Double.doubleToLongBits(doubleValue.get()) != Double.doubleToLongBits(transformed)) {
                doubleValue.set(transformed);
            }
        }
    }

    private void setInternalNumericValue(double newValue, boolean fromState, boolean notifyChange) {
        double sanitized = sanitizeNumericValue(newValue);
        if (Double.doubleToLongBits(value) == Double.doubleToLongBits(sanitized)) {
            return;
        }
        value = sanitized;
        updateBuffersFromValue();
        if (!fromState && state != null) {
            state.set(snapshotValue());
        }
        if (notifyChange) {
            notifyNumericChange();
        }
    }

    private void setInternalEnumValue(@Nullable Enum<?> newValue, boolean fromState) {
        Enum<?> value = newValue != null ? newValue : enumValues[0];
        for (int i = 0; i < enumValues.length; i++) {
            if (enumValues[i] == value) {
                setInternalEnumIndex(i, fromState, false);
                return;
            }
        }
        setInternalEnumIndex(0, fromState, false);
    }

    private void setInternalEnumIndex(int index, boolean fromState, boolean notifyChange) {
        int clamped = Math.max(0, Math.min(enumValues.length - 1, index));
        if (enumIndex == clamped) {
            return;
        }
        enumIndex = clamped;
        updateBuffersFromValue();
        updateEnumSelection();
        if (!fromState && state != null) {
            state.set(snapshotValue());
        }
        if (notifyChange) {
            notifyEnumChange();
        }
    }

    private void updateEnumSelection() {
        Enum<?> current = enumValues[enumIndex];
        String label = valueFormatter != null ? valueFormatter.apply(current) : current.name();
        currentEnumSelection = new EnumSelection(enumIndex, current, label);
    }

    private double sanitizeNumericValue(double incoming) {
        double clamped = clamp(incoming, minValue, maxValue);
        if (kind == SliderKind.INTEGER) {
            double rounded = Math.round(clamped);
            return clamp(rounded, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        if (!Double.isFinite(clamped)) {
            return minValue;
        }
        return clamped;
    }

    private double toDouble(@Nullable Object value) {
        if (value == null) {
            return minValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("Unsupported numeric value type: " + value.getClass().getName());
    }

    private T castNumeric(double value) {
        if (kind == SliderKind.INTEGER) {
            return castType((int) Math.round(value));
        } else if (kind == SliderKind.FLOAT) {
            return castType((float) value);
        } else if (kind == SliderKind.DOUBLE) {
            return castType(value);
        }
        throw new IllegalStateException("Enum slider cannot cast numeric value");
    }

    @SuppressWarnings("unchecked")
    private T castType(Object value) {
        return (T) value;
    }

    private void ensureNumeric() {
        if (kind == SliderKind.ENUM) {
            throw new IllegalStateException("Enum slider does not support numeric range operations");
        }
    }

    private void ensureEnum() {
        if (kind != SliderKind.ENUM) {
            throw new IllegalStateException("Numeric slider does not support enum operations");
        }
    }

    private void ensureTransformSupported() {
        if (kind == SliderKind.INTEGER) {
            throw new IllegalStateException("Integer slider does not support custom transform");
        }
    }

    private String defaultFormat() {
        if (kind == SliderKind.INTEGER) {
            return "%d";
        } else if (kind == SliderKind.FLOAT) {
            return "%.3f";
        } else {
            return "%.6f";
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum SliderKind {
        INTEGER,
        FLOAT,
        DOUBLE,
        ENUM
    }

    public record EnumSelection(int index, Enum<?> value, String label) {
        public <E extends Enum<E>> E value(Class<E> enumClass) {
            return enumClass.cast(value);
        }
    }
}
