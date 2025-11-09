package tytoo.minegui.runtime.viewport;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;

public final class ViewportFrameLimiter {
    private ViewportFrameLimiter() {
    }

    public static boolean shouldHoldMaxFps() {
        if (!MineGuiCore.isInitialized()) {
            return false;
        }
        MineGuiNamespaceContext context = MineGuiNamespaces.get(MineGuiCore.getConfigNamespace());
        if (context == null) {
            return false;
        }
        NamespaceConfig config = context.config().current();
        if (config == null || !config.viewportEnabled()) {
            return false;
        }
        if (MineGuiNamespaces.anyVisible()) {
            return true;
        }
        return ViewportInteractionTracker.isActive();
    }
}
