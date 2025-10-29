package tytoo.minegui.mixin.client;

import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.runtime.viewport.ViewportInteractionTracker;

@Mixin(InactivityFpsLimiter.class)
public abstract class MGInactivityFpsLimiterMixin {
    @Shadow
    private int maxFps;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void minegui$keepViewportFrameRate(CallbackInfoReturnable<Integer> cir) {
        if (!GlobalConfigManager.getConfig().isViewportEnabled()) {
            return;
        }
        if (MineGuiNamespaces.anyVisible()) {
            cir.setReturnValue(maxFps);
            return;
        }
        if (!ViewportInteractionTracker.isActive()) {
            return;
        }
        cir.setReturnValue(maxFps);
    }
}
