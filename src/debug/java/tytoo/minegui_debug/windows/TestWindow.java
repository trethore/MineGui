package tytoo.minegui_debug.windows;

import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.component.components.display.MGImage;
import tytoo.minegui.component.components.interactive.MGImageButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui_debug.MineGuiDebugCore;

public final class TestWindow extends MGWindow {

    public TestWindow() {
        super("MineGui Icon");
    }

    @Override
    protected void onCreate() {
        initialBounds(320, 200, 220, 260);
    }

    @Override
    protected void renderContents() {
        Identifier icon = Identifier.of(MineGuiCore.ID, "icon.png");
        MGImage.of(icon)
                .id("minegui_icon")
                .pos(Constraints.center(), Constraints.pixels(30f))
                .dimensions(128f, 128f)
                .render();

        MGImageButton.of(icon)
                .id("minegui_icon_button")
                .pos(Constraints.center(), Constraints.pixels(180f))
                .dimensions(64f, 64f)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Clicked MineGui icon button"))
                .render();
    }
}
