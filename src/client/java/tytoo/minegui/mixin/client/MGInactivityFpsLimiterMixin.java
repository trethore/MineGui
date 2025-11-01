package tytoo.minegui.mixin.client;

import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tytoo.minegui.runtime.viewport.ViewportFrameLimiter;

@Mixin(InactivityFpsLimiter.class)
public abstract class MGInactivityFpsLimiterMixin {
    @Shadow
    private int maxFps;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void minegui$keepViewportFrameRate(CallbackInfoReturnable<Integer> cir) {
        if (ViewportFrameLimiter.shouldHoldMaxFps()) {
            cir.setReturnValue(maxFps);
        }
    }
}
