package tytoo.minegui.runtime.viewport;

import java.util.concurrent.TimeUnit;

public final class ViewportInteractionTracker {
    private static final long INTERACTION_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(250L);
    private static volatile long lastInteractionNanos;

    private ViewportInteractionTracker() {
    }

    public static void notifyInteraction() {
        lastInteractionNanos = System.nanoTime();
    }

    public static boolean isActive() {
        long last = lastInteractionNanos;
        if (last == 0L) {
            return false;
        }
        return System.nanoTime() - last <= INTERACTION_TIMEOUT_NANOS;
    }
}
