package tytoo.minegui.component.components.display;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;

public class MGText implements MGComponent {

    private final String text;

    private MGText(String text) {
        this.text = text;
    }

    public static MGText of(String text) {
        return new MGText(text);
    }

    @Override
    public void render() {
        ImGui.text(text);
    }
}