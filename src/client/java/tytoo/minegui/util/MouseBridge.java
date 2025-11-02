package tytoo.minegui.util;

import net.minecraft.client.Mouse;

import java.util.Objects;

public final class MouseBridge {
    private final Mouse mouse;

    private MouseBridge(Mouse mouse) {
        this.mouse = mouse;
    }

    public static MouseBridge of(Mouse mouse) {
        Objects.requireNonNull(mouse, "mouse");
        return new MouseBridge(mouse);
    }

    public boolean isLocked() {
        return mouse.isCursorLocked();
    }

    public void setLocked(boolean locked) {
        if (locked) {
            mouse.lockCursor();
        } else {
            mouse.unlockCursor();
        }
    }
}
