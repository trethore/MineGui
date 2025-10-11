package tytoo.minegui_debug.windows;

import tytoo.minegui.component.behavior.TooltipBehavior;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.interactive.MGInputNumber;
import tytoo.minegui.component.components.interactive.MGInputText;
import tytoo.minegui.component.components.interactive.MGRadio;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui_debug.MineGuiDebugCore;

public class TestWindow extends MGWindow {

    private final State<String> radioSelection = State.of("option_a");
    private final State<String> nameInput = State.of("Steve");
    private final State<Integer> counterValue = State.of(5);
    private final State<Float[]> vectorValue = State.of(new Float[]{0.25f, 1.5f, -3.0f});
    private final State<Double> speedValue = State.of(12.5);

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
                .repeatable()
                .pos(10, 30)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Test: Button Clicked!"))
                .behavior(TooltipBehavior.of("This is a button tooltip!"))
                .parent(this);

        MGInputText.of(nameInput)
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

        MGRadio.of("Option A", "option_a", radioSelection)
                .pos(10, 110)
                .scale(2.0f)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Radio Selected: {}", radioSelection.get()))
                .parent(this);

        MGRadio.of("Option B", "option_b", radioSelection)
                .pos(10, 158)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Radio Selected: {}", radioSelection.get()))
                .parent(this);

        MGRadio.of("Option C", "option_c", radioSelection)
                .pos(10, 182)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Radio Selected: {}", radioSelection.get()))
                .parent(this);

        MGInputNumber.ofInt(counterValue)
                .pos(220, 110)
                .width(Constraints.pixels(180))
                .step(1)
                .fastStep(10)
                .onChange(values -> MineGuiDebugCore.LOGGER.info("Counter changed: {}", values[0]))
                .onCommit(values -> MineGuiDebugCore.LOGGER.info("Counter committed: {}", values[0]))
                .parent(this);

        MGInputNumber.ofFloatComponents(3)
                .pos(220, 150)
                .width(Constraints.pixels(180))
                .step(0.05f)
                .format("%.2f")
                .state(vectorValue)
                .onChange(values -> MineGuiDebugCore.LOGGER.info("Vector changed: [{}, {}, {}]", values[0], values[1], values[2]))
                .parent(this);

        MGInputNumber.ofDouble(speedValue)
                .pos(220, 190)
                .width(Constraints.pixels(180))
                .step(0.1)
                .fastStep(1.0)
                .submitOnEnter(true)
                .onChange(values -> MineGuiDebugCore.LOGGER.info("Speed changed: {}", values[0]))
                .onCommit(values -> MineGuiDebugCore.LOGGER.info("Speed committed: {}", values[0]))
                .parent(this);

    }
}
