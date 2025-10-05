package tytoo.minegui.component.components.display;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Textable;
import tytoo.minegui.state.State;

import java.util.function.Supplier;

public class MGText extends MGComponent<MGText> implements Textable<MGText>, Scalable<MGText> {

    private Supplier<String> textSupplier;
    private float scale = 1.0f;

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

    @Override
    public void render() {
        if (scale != 1.0f) {
            float oldScale = ImGui.getIO().getFontGlobalScale();
            ImGui.getIO().setFontGlobalScale(scale);
            ImGui.text(getText());
            ImGui.getIO().setFontGlobalScale(oldScale);
        } else {
            ImGui.text(getText());
        }
    }
}
