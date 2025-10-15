package tytoo.minegui_debug.windows;

import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.state.State;

public class SettingsWindow extends MGWindow {

    private final State<Boolean> darkMode = State.of(false);

    public SettingsWindow() {
        super("Settings", false);
    }

    @Override
    protected void onCreate() {
        this.initialBounds(300, 300, 300, 200);
    }

    @Override
    protected void renderContents() {
        MGText.of("Settings Panel")
                .scale(1.2f)
                .pos(10, 10)
                .render();

        MGButton.of(() -> "Dark Mode: " + (darkMode.get() ? "ON" : "OFF"))
                .size(200, 30)
                .pos(10, 40)
                .onClick(() -> darkMode.set(!darkMode.get()))
                .render();

        MGButton.of("Close")
                .size(100, 30)
                .pos(10, 80)
                .onClick(this::close)
                .render();
    }
}
