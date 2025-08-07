package tytoo.minegui.windows;

import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;

public class TestWindow extends MGWindow {

    public TestWindow() {
        super("test window");
    }

    @Override
    public void build() {
        super.build();
        // Centered: fixed width + aspect ratio height (16:9)
        MGButton fixed = MGButton.of("Fixed 200px, 16:9 (centered)");
        fixed.setParent(this);
        fixed.constraints().setWidth(Constraints.pixels(200));
        fixed.constraints().setHeight(Constraints.aspect(16f / 9f));
        fixed.constraints().setX(Constraints.center());
        fixed.constraints().setY(Constraints.center());

        // Relative: 30% width, 12% height, positioned at (5%, 10%) with small pixel offsets
        MGButton relative = MGButton.of("Relative 30% x 12% @ (5%, 10%)");
        relative.setParent(this);
        // size
        relative.constraints().setWidth(Constraints.relative(0.90f));
        relative.constraints().setHeight(Constraints.relative(0.2f));
        // position (percent + pixel offset)
        relative.constraints().setX(Constraints.relative(0.05f, 8f));
        relative.constraints().setY(Constraints.relative(0.10f, 8f));
    }
}
