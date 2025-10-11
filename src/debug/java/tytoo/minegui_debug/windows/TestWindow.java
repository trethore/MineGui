package tytoo.minegui_debug.windows;

import tytoo.minegui.component.behavior.TooltipBehavior;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.interactive.MGRadioButton;
import tytoo.minegui.component.components.interactive.MGTextField;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui_debug.MineGuiDebugCore;

public class TestWindow extends MGWindow {

    private final State<String> radioSelection = State.of("option_a");
    private final State<String> nameInput = State.of("Steve");

    public TestWindow() {
        super("test window");
    }

    @Override
    public void build() {
        super.build();
        this.initialBounds(200, 180, 430, 260);

        MGButton.of("Test")
                .width(Constraints.pixels(200))
                .height(Constraints.pixels(60))
                .pos(10, 30)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Test: Button Clicked!"))
                .behavior(TooltipBehavior.of("This is a button tooltip!"))
                .parent(this);

        MGTextField.of(nameInput)
                .hint("Enter player name")
                .pos(220, 30)
                .width(Constraints.pixels(180))
                .maxLength(24)
                .filter(Character::isLetterOrDigit)
                .validator(text -> text.isEmpty() || !text.trim().isEmpty())
                .submitOnEnter(true)
                .onChange(value -> MineGuiDebugCore.LOGGER.info("Name changed to: {}", value))
                .onSubmit(value -> MineGuiDebugCore.LOGGER.info("Name submitted: {}", value))
                .parent(this);

        MGRadioButton.of("Option A", "option_a", radioSelection)
                .pos(10, 110)
                .scale(2.0f)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Radio Selected: {}", radioSelection.get()))
                .parent(this);

        MGRadioButton.of("Option B", "option_b", radioSelection)
                .pos(10, 158)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Radio Selected: {}", radioSelection.get()))
                .parent(this);

        MGRadioButton.of("Option C", "option_c", radioSelection)
                .pos(10, 182)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Radio Selected: {}", radioSelection.get()))
                .parent(this);
    }
}
