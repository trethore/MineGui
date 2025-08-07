package tytoo.minegui.mixin.client;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.utils.ImGuiUtils;

@Mixin(Keyboard.class)
public class MGKeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void keyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (ImGuiUtils.shouldCancelGameInputs())
            ci.cancel();
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    public void charTyped(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (ImGuiUtils.shouldCancelGameInputs())
            ci.cancel();
    }
}
