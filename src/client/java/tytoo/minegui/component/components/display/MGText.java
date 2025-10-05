package tytoo.minegui.component.components.display;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.state.State;

import java.util.function.Supplier;

public class MGText extends MGComponent<MGText> {

    private Supplier<String> textSupplier;
    private float scale = 1.0f;

    private MGText(String text) {
        this.textSupplier = () -> text;
    }

    public static MGText of(String text) {
        return new MGText(text);
    }

    public static MGText of(State<String> state) {
        MGText t = new MGText(state.get());
        return t.bind(state);
    }

    public MGText text(String text) {
        this.textSupplier = () -> text;
        return this;
    }

    public MGText bind(State<String> state) {
        this.textSupplier = state::get;
        return this;
    }

    public MGText scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public void render() {
        if (scale != 1.0f) {
            float oldScale = ImGui.getIO().getFontGlobalScale();
            ImGui.getIO().setFontGlobalScale(scale);
            ImGui.text(textSupplier.get());
            ImGui.getIO().setFontGlobalScale(oldScale);
        } else {
            ImGui.text(textSupplier.get());
        }
    }
}
