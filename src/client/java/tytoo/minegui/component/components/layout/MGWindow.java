package tytoo.minegui.component.components.layout;

import imgui.ImGui;
import imgui.type.ImBoolean;
import tytoo.minegui.component.MGContainer;

public abstract class MGWindow extends MGContainer {

    private final ImBoolean visible;
    private String title;

    public MGWindow(String title) {
        this.title = title;
        this.visible = new ImBoolean(true);
    }

    public void build() {
        // This method can be overridden to build the window's content.
    }

    @Override
    public void render() {
        if (!isVisible()) {
            return;
        }

        if (ImGui.begin(title, visible)) {
            super.render();
        }
        ImGui.end();
    }

    public void open() {
        setVisible(true);
    }

    public void close() {
        setVisible(false);
    }

    public void toggle() {
        setVisible(!isVisible());
    }

    public String getTitle() {
        return title;
    }

    public String setTitle(String title) {
        return this.title = title;
    }

    public boolean isVisible() {
        return this.visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }


}