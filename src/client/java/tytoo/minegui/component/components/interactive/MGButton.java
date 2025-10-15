package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.*;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public final class MGButton extends MGComponent<MGButton>
        implements Textable<MGButton>, Clickable<MGButton>, Disableable<MGButton>, Sizable<MGButton>,
        Scalable<MGButton>, Repeatable<MGButton> {

    private static final ComponentPool<MGButton> POOL = new ComponentPool<>(MGButton::new, MGButton::prepare);

    private String literalText;
    private final Supplier<String> literalSupplier = () -> literalText;
    private Supplier<String> textSupplier;
    @Nullable
    private Runnable onClick;
    private boolean disabled;
    private float scale;
    private boolean repeatable;

    private MGButton() {
        literalText = "";
        prepare();
    }

    public static MGButton of(String text) {
        MGButton button = POOL.acquire();
        button.literalText = text != null ? text : "";
        button.textSupplier = button.literalSupplier;
        return button;
    }

    public static MGButton of(State<String> state) {
        MGButton button = POOL.acquire();
        button.bindText(state);
        return button;
    }

    public static MGButton of(Supplier<String> supplier) {
        MGButton button = POOL.acquire();
        button.setTextSupplier(supplier);
        return button;
    }

    private void prepare() {
        textSupplier = literalSupplier;
        literalText = "";
        onClick = null;
        disabled = false;
        scale = 1.0f;
        repeatable = false;
    }

    @Override
    public Supplier<String> getTextSupplier() {
        return textSupplier;
    }

    @Override
    public void setTextSupplier(Supplier<String> supplier) {
        textSupplier = supplier != null ? supplier : literalSupplier;
    }

    @Override
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }

    @Override
    public void setOnClick(@Nullable Runnable action) {
        onClick = action;
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
    protected void renderComponent() {
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
            if (pressed && !disabled) {
                performClick();
            }
        });
    }

    @Override
    public MGButton text(String text) {
        literalText = text != null ? text : "";
        textSupplier = literalSupplier;
        return self();
    }

    @Override
    public MGButton bindText(State<String> state) {
        if (state == null) {
            literalText = "";
            textSupplier = literalSupplier;
        } else {
            textSupplier = state::get;
        }
        return self();
    }

}
