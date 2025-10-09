package tytoo.minegui_debug.windows;

import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.interactive.MGCheckbox;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;

public class TestWindow extends MGWindow {

    public TestWindow() {
        super("test window");
    }

    @Override
    public void build() {
        super.build();
        this.initialBounds(200, 200, 300, 150);

        State<Boolean> showDetails = State.of(true);

        MGText.of("Checkbox Example")
                .scale(5f)
                .pos(10, 120)
                .parent(this);

        MGCheckbox.of("Show Details", showDetails)
                .scale(5f)
                .pos(10, 30)
                .parent(this);

        MGButton.of("Hello, World!")
                .pos(10, 180)
                .width(Constraints.pixels(200))
                .height(Constraints.pixels(60))
                .onClick(() -> System.out.println("Button clicked!"))
                .parent(this);
    }
}
