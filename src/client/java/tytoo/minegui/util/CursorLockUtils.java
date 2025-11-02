package tytoo.minegui.util;

public final class CursorLockUtils {
    private CursorLockUtils() {
    }

    public static void applyCursorLock(MouseBridge cursor, boolean lock) {
        if (cursor == null) {
            return;
        }
        cursor.setLocked(lock);
    }

    public static void applyCursorLock(boolean lock) {
        McClientBridge.mouse().ifPresent(cursor -> cursor.setLocked(lock));
    }

    public static boolean clientWantsLockCursor() {
        return McClientBridge.clientWantsCursorLock();
    }

    public static boolean isCursorLocked() {
        return McClientBridge.mouse()
                .map(MouseBridge::isLocked)
                .orElse(false);
    }
}
