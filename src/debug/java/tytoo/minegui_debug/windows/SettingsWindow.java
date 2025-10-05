package tytoo.minegui_debug.windows;

import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.state.State;

public class SettingsWindow extends MGWindow {

    public SettingsWindow() {
        super("Settings", false);
    }

    public static SettingsWindow create() {
        SettingsWindow window = new SettingsWindow();
        window.initialize();
        return window;
    }

    @Override
    public void build() {
        super.build();
        this.initialBounds(300, 300, 300, 200);

        State<Boolean> darkMode = State.of(false);

        MGText.of("Settings Panel")
                .scale(1.2f)
                .pos(10, 10)
                .parent(this);

        MGButton.of(State.computed(() -> "Dark Mode: " + (darkMode.get() ? "ON" : "OFF")))
                .onClick(() -> darkMode.set(!darkMode.get()))
                .size(200, 30)
                .pos(10, 40)
                .parent(this);

        MGButton.of("Close")
                .onClick(this::close)
                .size(100, 30)
                .pos(10, 80)
                .parent(this);
    }
}
