package tytoo.minegui.mixin.client;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.utils.ImGuiUtils;

@Mixin(Mouse.class)
public class MGMouseMixin {

    @Inject(at = @At("HEAD"), method = "onMouseScroll(JDD)V", cancellable = true)
    private void onOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ImGuiUtils.shouldCancelGameInputs()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseButton", cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (ImGuiUtils.shouldCancelGameInputs()) {
            ci.cancel();
        }
    }
}
