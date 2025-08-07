package tytoo.minegui.component.components.layout;

import imgui.ImGui;
import imgui.type.ImBoolean;
import tytoo.minegui.component.MGComponent;

// represent a imgui window.
public abstract class MGWindow extends MGComponent<MGWindow> {

    private final ImBoolean visible;
    private String title;

    public MGWindow(String title) {
        this.title = title;
        this.visible = new ImBoolean(true);
        build();
    }

    // add components here
    public void build() {

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