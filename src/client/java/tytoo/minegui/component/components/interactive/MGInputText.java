package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.ImGuiInputTextCallbackData;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MGInputText extends MGComponent<MGInputText>
        implements Disableable<MGInputText>, Stateful<String, MGInputText>, Scalable<MGInputText>, Sizable<MGInputText> {

    private final ImString buffer;
    private final String defaultLabel = "##MGInputText_" + UUID.randomUUID();
    private String label = defaultLabel;
    private Supplier<String> hintSupplier = () -> "";
    private boolean disabled;
    private float scale = 1.0f;
    @Nullable
    private State<String> state;
    @Nullable
    private Consumer<String> stateListener;
    private boolean suppressStateCallback;
    private String currentValue;
    @Nullable
    private IntPredicate characterFilter;
    @Nullable
    private Predicate<String> validator;
    @Nullable
    private Consumer<String> onChange;
    @Nullable
    private Consumer<String> onSubmit;
    private int userFlags = ImGuiInputTextFlags.None;
    private int maxLength;
    private final ImGuiInputTextCallback inputTextCallback = new ImGuiInputTextCallback() {
        @Override
        public void accept(ImGuiInputTextCallbackData data) {
            if (data.getEventFlag() != ImGuiInputTextFlags.CallbackCharFilter) {
                return;
            }
            int eventChar = data.getEventChar();
            if (eventChar == 0) {
                return;
            }
            if (maxLength > 0) {
                int charUnits = Character.isValidCodePoint(eventChar) ? Character.charCount(eventChar) : 1;
                int resultingLength = projectedLengthAfterInsertion(data, charUnits);
                if (resultingLength > maxLength) {
                    data.setEventChar(0);
                    return;
                }
            }
            if (characterFilter != null && !characterFilter.test(eventChar)) {
                data.setEventChar(0);
            }
        }
    };

    private MGInputText(String initialValue) {
        String value = initialValue != null ? initialValue : "";
        int capacity = Math.max(32, value.length() + 32);
        this.buffer = new ImString(value, capacity);
        this.buffer.inputData.isResizable = true;
        this.currentValue = value;
    }

    public static MGInputText of() {
        return new MGInputText("");
    }

    public static MGInputText of(String initialValue) {
        return new MGInputText(initialValue);
    }

    public static MGInputText of(State<String> state) {
        MGInputText field = new MGInputText(state != null ? Objects.toString(state.get(), "") : "");
        field.setState(state);
        return field;
    }

    public String getValue() {
        return currentValue;
    }

    public MGInputText value(String value) {
        setInternalValue(value, false, true);
        return self();
    }

    private int projectedLengthAfterInsertion(ImGuiInputTextCallbackData data, int charUnits) {
        int selectionStart = Math.max(0, data.getSelectionStart());
        int selectionEnd = Math.max(selectionStart, data.getSelectionEnd());
        int selectionLength = Math.max(0, selectionEnd - selectionStart);
        String value = currentValue != null ? currentValue : "";
        int baseLength = value.length();
        int remainingLength = Math.max(0, baseLength - selectionLength);
        return remainingLength + Math.max(0, charUnits);
    }

    public MGInputText hint(String hint) {
        return hint(() -> hint != null ? hint : "");
    }

    public MGInputText hint(Supplier<String> supplier) {
        this.hintSupplier = supplier != null ? supplier : () -> "";
        return self();
    }

    public MGInputText label(String label) {
        this.label = label != null && !label.isBlank() ? label : defaultLabel;
        return self();
    }

    public MGInputText onChange(Consumer<String> consumer) {
        this.onChange = consumer;
        return self();
    }

    public MGInputText onSubmit(Consumer<String> consumer) {
        this.onSubmit = consumer;
        return self();
    }

    public MGInputText filter(IntPredicate predicate) {
        this.characterFilter = predicate;
        return self();
    }

    public MGInputText validator(Predicate<String> predicate) {
        this.validator = predicate;
        setInternalValue(currentValue, true, false);
        return self();
    }

    public MGInputText maxLength(int length) {
        this.maxLength = Math.max(0, length);
        setInternalValue(currentValue, true, false);
        return self();
    }

    public MGInputText flags(int flags) {
        this.userFlags = flags;
        return self();
    }

    public MGInputText addFlags(int flags) {
        this.userFlags |= flags;
        return self();
    }

    public MGInputText removeFlags(int flags) {
        this.userFlags &= ~flags;
        return self();
    }

    public MGInputText submitOnEnter(boolean enabled) {
        if (enabled) {
            addFlags(ImGuiInputTextFlags.EnterReturnsTrue);
        } else {
            removeFlags(ImGuiInputTextFlags.EnterReturnsTrue);
        }
        return self();
    }

    public MGInputText readOnly(boolean enabled) {
        if (enabled) {
            addFlags(ImGuiInputTextFlags.ReadOnly);
        } else {
            removeFlags(ImGuiInputTextFlags.ReadOnly);
        }
        return self();
    }

    public MGInputText password(boolean enabled) {
        if (enabled) {
            addFlags(ImGuiInputTextFlags.Password);
        } else {
            removeFlags(ImGuiInputTextFlags.Password);
        }
        return self();
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
    public State<String> getState() {
        return state;
    }

    @Override
    public void setState(@Nullable State<String> state) {
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
            setInternalValue(newValue, true, false);
        };
        state.addListener(stateListener);
        setInternalValue(state.get(), true, false);
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
        String text = readBuffer();
        String hint = hintSupplier.get();
        boolean scaled = scale != 1.0f;
        float scaleFactor = scaled ? scale : 1.0f;
        float framePaddingX = ImGui.getStyle().getFramePaddingX();
        String sampleText = text.isEmpty() ? (hint != null ? hint : "") : text;
        float baseWidth = ImGui.calcTextSize(sampleText).x * scaleFactor + framePaddingX * 2.0f;
        float baseHeight = ImGui.getFrameHeight() * scaleFactor;
        boolean disabledScope = disabled;
        int effectiveFlags = resolveFlags();
        ImGuiInputTextCallback callback = needsCallback(effectiveFlags) ? inputTextCallback : null;

        final boolean[] activation = new boolean[1];
        withLayout(baseWidth, baseHeight, (width, height) -> {
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            if (disabledScope) {
                ImGui.beginDisabled(true);
            }
            try {
                if (hint == null || hint.isEmpty()) {
                    activation[0] = ImGui.inputText(label, buffer, effectiveFlags, callback);
                } else {
                    activation[0] = ImGui.inputTextWithHint(label, hint, buffer, effectiveFlags, callback);
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
        boolean activated = activation[0];

        String after = readBuffer();
        boolean changed = !Objects.equals(text, after);
        if (changed) {
            setInternalValue(after, false, true);
        } else if (!Objects.equals(after, currentValue)) {
            setInternalValue(after, false, false);
        }

        if ((effectiveFlags & ImGuiInputTextFlags.EnterReturnsTrue) != 0 && activated && onSubmit != null) {
            onSubmit.accept(currentValue);
        }
        renderChildren();
        endRenderLifecycle();
    }

    private int resolveFlags() {
        int effective = userFlags;
        if (characterFilter != null || maxLength > 0) {
            effective |= ImGuiInputTextFlags.CallbackCharFilter;
        }
        return effective;
    }

    private boolean needsCallback(int effectiveFlags) {
        return (effectiveFlags & ImGuiInputTextFlags.CallbackCharFilter) != 0;
    }

    private void setInternalValue(String value, boolean fromState, boolean notifyChange) {
        String sanitized = sanitize(value);
        String bufferValue = readBuffer();
        boolean bufferDiffers = !Objects.equals(bufferValue, sanitized);
        if (bufferDiffers) {
            buffer.set(sanitized, true);
        }
        boolean valueChanged = !Objects.equals(currentValue, sanitized);
        if (!valueChanged) {
            if (fromState && state != null && !Objects.equals(value, sanitized)) {
                suppressStateCallback = true;
                state.set(sanitized);
                suppressStateCallback = false;
            }
            return;
        }
        currentValue = sanitized;
        if (!fromState && state != null) {
            suppressStateCallback = true;
            state.set(sanitized);
            suppressStateCallback = false;
        } else if (fromState && state != null && !Objects.equals(value, sanitized)) {
            suppressStateCallback = true;
            state.set(sanitized);
            suppressStateCallback = false;
        }
        if (notifyChange && onChange != null) {
            onChange.accept(sanitized);
        }
    }

    private String readBuffer() {
        byte[] data = buffer.getData();
        int length = 0;
        while (length < data.length && data[length] != 0) {
            length++;
        }
        if (length == 0) {
            return "";
        }
        return new String(data, 0, length, StandardCharsets.UTF_8);
    }

    private String sanitize(String value) {
        String result = value != null ? value : "";
        if (maxLength > 0 && result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }
        if (validator != null && !validator.test(result)) {
            return currentValue;
        }
        return result;
    }
}
