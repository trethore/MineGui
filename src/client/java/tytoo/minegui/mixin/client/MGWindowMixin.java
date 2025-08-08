package tytoo.minegui.mixin.client;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.imgui.ImGuiLoader;

@Mixin(Window.class)
public abstract class MGWindowMixin {
    @Inject(method = "onFramebufferSizeChanged", at = @At("TAIL"))
    private void onFramebufferResize(long window, int width, int height, CallbackInfo ci) {
        ImGuiLoader.onWindowResize(width, height);
    }

    @Inject(method = "onWindowPosChanged", at = @At("TAIL"))
    private void onWindowResize(long window, int x, int y, CallbackInfo ci) {
        ImGuiLoader.onWindowMoved(x, y);
    }
}