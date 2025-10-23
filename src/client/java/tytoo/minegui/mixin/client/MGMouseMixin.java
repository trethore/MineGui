package tytoo.minegui.mixin.client;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.imgui.ImGuiLoader;
import tytoo.minegui.input.InputRouter;
import tytoo.minegui.manager.UIManager;

@Mixin(Mouse.class)
public abstract class MGMouseMixin {
    @Inject(at = @At("HEAD"), method = "onMouseScroll(JDD)V", cancellable = true)
    private void onOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        ImGuiLoader.onMouseScroll(window, horizontal, vertical);
        if (InputRouter.getInstance().onScroll(horizontal, vertical)) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseButton", cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (InputRouter.getInstance().onMouseButton(button, action)) {
            ci.cancel();
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void minegui$onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (InputRouter.getInstance().onMouseMove()) {
            ci.cancel();
        }
    }

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void minegui$lockCursor(CallbackInfo ci) {
        if (!UIManager.getInstance().hasVisibleViews()) return;

        if (InputRouter.getInstance().shouldPreventLock()) {
            ci.cancel();
        }
    }
}
