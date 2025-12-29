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
        boolean viewportEnabledFound = false;
        for (MineGuiContext context : MineGuiCore.getAllContexts()) {
            NamespaceConfig config = context.config();
            if (config == null || !config.viewportEnabled()) {
                continue;
            }
            viewportEnabledFound = true;
            if (context.ui().hasVisibleViews()) {
                return true;
            }
        }
        if (!viewportEnabledFound) {
            return false;
        }
        return ViewportInteractionTracker.isActive();
    }
}
