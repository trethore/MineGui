package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public class MGButton extends MGComponent<MGButton>
        implements Textable<MGButton>, Clickable<MGButton>, Disableable<MGButton>, Sizable<MGButton>, Scalable<MGButton>, Repeatable<MGButton> {

    private Supplier<String> textSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled = false;
    private float scale = 1.0f;
    private boolean repeatable = false;

    private MGButton(String text) {
        this.textSupplier = () -> text;
    }

    public static MGButton of(String text) {
        return new MGButton(text);
    }

    public static MGButton of(State<String> state) {
        return new MGButton(state.get()).bindText(state);
    }

    @Override
    public Supplier<String> getTextSupplier() {
        return textSupplier;
    }

    @Override
    public void setTextSupplier(Supplier<String> supplier) {
        this.textSupplier = supplier;
    }

    @Override
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }

    @Override
    public void setOnClick(@Nullable Runnable action) {
        this.onClick = action;
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
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Override
    public void render() {
        beginRenderLifecycle();
        String label = textSupplier.get();
        float appliedScale = scale;
        boolean applyScale = appliedScale != 1.0f;
        float stylePaddingX = ImGui.getStyle().getFramePaddingX();
        float stylePaddingY = ImGui.getStyle().getFramePaddingY();
        float textWidth = ImGui.calcTextSize(label).x * appliedScale;
        float textHeight = ImGui.calcTextSize(label).y * appliedScale;
        float baseWidth = textWidth + stylePaddingX * 2.0f;
        float baseHeight = textHeight + stylePaddingY * 2.0f;

        withLayout(baseWidth, baseHeight, (width, height) -> {
            if (applyScale) {
                ImGuiUtils.pushWindowFontScale(appliedScale);
            }
            if (repeatable) {
                ImGui.pushButtonRepeat(true);
            }
            boolean pressed;
            try {
                pressed = ImGui.button(label, width, height);
            } finally {
                if (repeatable) {
                    ImGui.popButtonRepeat();
                }
                if (applyScale) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
            if (pressed) {
                performClick();
            }
        });
        renderChildren();
        endRenderLifecycle();
    }
}
