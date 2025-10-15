package tytoo.minegui.component.components.display;

import imgui.ImGui;
import imgui.ImVec2;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Textable;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.function.Supplier;

public class MGTextWrapped extends MGComponent<MGTextWrapped> implements Textable<MGTextWrapped>, Scalable<MGTextWrapped>, Sizable<MGTextWrapped> {

    private Supplier<String> textSupplier;
    private float scale = 1.0f;
    private boolean bullet = false;
    private boolean unformatted = false;

    private MGTextWrapped(String text) {
        this.textSupplier = () -> text;
    }

    public static MGTextWrapped of(String text) {
        return new MGTextWrapped(text);
    }

    public static MGTextWrapped of(State<String> state) {
        return new MGTextWrapped(state.get()).bindText(state);
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

    public MGTextWrapped bullet(boolean bullet) {
        setBullet(bullet);
        return this;
    }

    public boolean isUnformatted() {
        return unformatted;
    }

    public void setUnformatted(boolean unformatted) {
        this.unformatted = unformatted;
    }

    public MGTextWrapped unformatted(boolean unformatted) {
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
                if (bullet) {
                    ImGui.bullet();
                    ImGui.sameLine();
                }
                if (unformatted) {
                    ImGui.textUnformatted(text);
                } else {
                    ImGui.textWrapped(text);
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
