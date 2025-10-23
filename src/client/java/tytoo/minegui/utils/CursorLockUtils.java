package tytoo.minegui.utils;

import net.minecraft.client.Mouse;

public final class CursorLockUtils {
    private CursorLockUtils() {
    }

    private static void setCursorLock(Mouse mouse, boolean lock) {
        if (mouse == null) {
            return;
        }
        if (lock) {
            mouse.lockCursor();
        } else {
            mouse.unlockCursor();
        }
    }

    public static void applyCursorLock(Mouse mouse, boolean lock) {
        setCursorLock(mouse, lock);
    }

    public static void applyCursorLock(boolean lock) {
        McUtils.getMc().ifPresent(client -> setCursorLock(client.mouse, lock));
    }

    public static boolean clientWantsLockCursor() {
        return McUtils.getMc()
                .map(client -> client.currentScreen == null)
                .orElse(false);
    }
}
