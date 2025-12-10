package tytoo.minegui.runtime.viewport;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.runtime.MineGuiContext;

public final class ViewportFrameLimiter {
    private ViewportFrameLimiter() {
    }

    public static boolean shouldHoldMaxFps() {
        if (!MineGuiCore.isInitialized()) {
            return false;
        }
        MineGuiContext context = MineGuiCore.getContext();
        if (context == null) {
            return false;
        }
        NamespaceConfig config = context.config().current();
        if (config == null || !config.viewportEnabled()) {
            return false;
        }
        if (context.ui().hasVisibleViews()) {
            return true;
        }
        return ViewportInteractionTracker.isActive();
    }
}
