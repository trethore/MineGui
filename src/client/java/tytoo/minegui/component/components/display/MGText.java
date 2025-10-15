package tytoo.minegui.component.components.display;

import imgui.ImGui;
import imgui.ImVec2;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Textable;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public class MGText extends MGComponent<MGText> implements Textable<MGText>, Scalable<MGText> {

    private Supplier<String> textSupplier;
    private float scale = 1.0f;
    private boolean bullet = false;
    private boolean unformatted = false;

    private MGText(String text) {
        this.textSupplier = () -> text;
    }

    public static MGText of(String text) {
        return new MGText(text);
    }

    public static MGText of(State<String> state) {
        return new MGText(state.get()).bindText(state);
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
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isBullet() {
        return bullet;
    }

    public void setBullet(boolean bullet) {
        this.bullet = bullet;
    }

    public MGText bullet(boolean bullet) {
        setBullet(bullet);
        return this;
    }

    public boolean isUnformatted() {
        return unformatted;
    }

    public void setUnformatted(boolean unformatted) {
        this.unformatted = unformatted;
    }

    public MGText unformatted(boolean unformatted) {
        setUnformatted(unformatted);
        return this;
    }

    @Override
    protected void renderComponent() {
        String text = getText();

        boolean applyScale = scale != 1.0f;
        float scaleFactor = applyScale ? scale : 1.0f;
        ImVec2 textSize = ImGui.calcTextSize(text);
        float baselineWidth = textSize.x * scaleFactor;
        float baselineHeight = textSize.y * scaleFactor;

        withLayout(baselineWidth, baselineHeight, (width, height) -> {
            if (applyScale) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            try {
                if (bullet && !unformatted) {
                    ImGui.bulletText(text);
                } else if (bullet) {
                    ImGui.bullet();
                    ImGui.sameLine();
                    ImGui.textUnformatted(text);
                } else if (unformatted) {
                    ImGui.textUnformatted(text);
                } else {
                    ImGui.text(text);
                }
            } finally {
                if (applyScale) {
                    ImGuiUtils.popWindowFontScale();
                }
            }
        });
        renderChildren();
    }
}
