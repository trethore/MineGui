package tytoo.minegui_debug.windows;

import tytoo.minegui.component.behavior.TooltipBehavior;
import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;

public class TestWindow extends MGWindow {

    public TestWindow() {
        super("test window");
        initialize();
    }

    @Override
    public void build() {
        super.build();
        this.initialBounds(200, 200, 400, 400);

        State<Integer> count = State.of(0);

        MGText.of(State.computed(() -> "Count: " + count.get()))
                .pos(10, 10)
                .parent(this);

        MGButton.of(State.computed(() -> "Increment (" + count.get() + ")"))
                .onClick(() -> count.set(count.get() + 1))
                .size(160, 28)
                .pos(10, 36)
                .behavior(TooltipBehavior.of("Click to increment counter"))
                .parent(this);

        MGButton.of("Decrement")
                .onClick(() -> count.set(Math.max(0, count.get() - 1)))
                .size(160, 28)
                .pos(180, 36)
                .behavior(TooltipBehavior.of("Click to decrement counter"))
                .parent(this);

        MGButton.of("Reset")
                .onClick(() -> count.set(0))
                .width(Constraints.pixels(120))
                .height(Constraints.pixels(28))
                .x(Constraints.relative(0.50f, -60f))
                .y(Constraints.pixels(72))
                .behavior(TooltipBehavior.of("Reset counter to zero"))
                .parent(this);

        MGButton.of("200px wide, 16:9")
                .width(Constraints.pixels(200))
                .height(Constraints.aspect(16f / 9f))
                .x(Constraints.relative(0.70f, -100f))
                .y(Constraints.pixels(110))
                .parent(this);

        addSubWindow(SettingsWindow.create());

        MGButton.of("Open Settings")
                .onClick(() -> {
                    for (MGWindow subWindow : getSubWindows()) {
                        if (subWindow instanceof SettingsWindow) {
                            subWindow.open();
                        }
                    }
                })
                .size(160, 28)
                .pos(10, 150)
                .behavior(TooltipBehavior.of("Open settings window (follows parent visibility)"))
                .parent(this);
    }
}
