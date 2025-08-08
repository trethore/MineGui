package tytoo.minegui.windows;

import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;

public class TestWindow extends MGWindow {

    public TestWindow() {
        super("test window");
        this.setInitialBounds(200, 200, 400, 400);
    }

    @Override
    public void build() {
        super.build();

        // --- Reactive state demo ---
        State<Integer> count = State.of(0);

        // Text bound to a computed state -> updates automatically when 'count' changes
        MGText counterText = MGText.of(State.computed(() -> "Count: " + count.get()));
        counterText.setParent(this);
        // place the text near top-left
        counterText.constraints().setX(Constraints.pixels(10));
        counterText.constraints().setY(Constraints.pixels(10));

        // Increment button: label also bound to computed state
        MGButton inc = MGButton.of(State.computed(() -> "Increment (" + count.get() + ")"));
        inc.onPress(() -> count.set(count.get() + 1));
        inc.setParent(this);
        inc.constraints().setWidth(Constraints.pixels(160));
        inc.constraints().setHeight(Constraints.pixels(28));
        inc.constraints().setX(Constraints.pixels(10));
        inc.constraints().setY(Constraints.pixels(36));

        // Decrement button
        MGButton dec = MGButton.of("Decrement");
        dec.onPress(() -> count.set(Math.max(0, count.get() - 1)));
        dec.setParent(this);
        dec.constraints().setWidth(Constraints.pixels(160));
        dec.constraints().setHeight(Constraints.pixels(28));
        dec.constraints().setX(Constraints.pixels(180));
        dec.constraints().setY(Constraints.pixels(36));

        // Reset button centered horizontally using relative constraints
        MGButton reset = MGButton.of("Reset");
        reset.onPress(() -> count.set(0));
        reset.setParent(this);
        reset.constraints().setWidth(Constraints.pixels(120));
        reset.constraints().setHeight(Constraints.pixels(28));
        reset.constraints().setX(Constraints.relative(0.50f, -60f)); // center - half width
        reset.constraints().setY(Constraints.pixels(72));

        // Aspect-ratio example button that reacts to window resize (kept to showcase constraints)
        MGButton ar = MGButton.of("200px wide, 16:9");
        ar.setParent(this);
        ar.constraints().setWidth(Constraints.pixels(200));
        ar.constraints().setHeight(Constraints.aspect(16f / 9f));
        ar.constraints().setX(Constraints.relative(0.70f, -100f));
        ar.constraints().setY(Constraints.pixels(110));
    }
}
