package tytoo.minegui.runtime.viewport;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.runtime.MineGuiNamespaces;

public final class ViewportFrameLimiter {
    private ViewportFrameLimiter() {
    }

    public static boolean shouldHoldMaxFps() {
        if (!MineGuiCore.isInitialized()) {
            return false;
        }
        if (!GlobalConfigManager.getConfig().isViewportEnabled()) {
            return false;
        }
        if (MineGuiNamespaces.anyVisible()) {
            return true;
        }
        return ViewportInteractionTracker.isActive();
    }
}
