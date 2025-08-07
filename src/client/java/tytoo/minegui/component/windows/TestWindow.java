package tytoo.minegui.component.windows;

import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;


// this is a test window for development purposes
public class TestWindow extends MGWindow {

    public TestWindow() {
        super("test window");
    }

    @Override
    public void build() {
        super.build();
        this.add(MGButton.of("Click Me !"));
    }
}
