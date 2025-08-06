package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;

public class MGButton implements MGComponent {
    private String text;

    private MGButton(String text) {
        this.text = text;
    }

    public static MGButton of(String text) {
        return new MGButton(text);
    }

    @Override
    public void render() {
        ImGui.button(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
