package tytoo.minegui.mixin.client;

import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.imgui.ImGuiLoader;

@Mixin(Window.class)
public abstract class MGGLFWMixin {
    @Shadow
    @Final
    private long handle;

    @Inject(at = @At("TAIL"), method = "<init>", remap = false)
    private void onGLFWInit(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, @Nullable String videoMode, String title, CallbackInfo ci) {
        ImGuiLoader.onGlfwInit(handle);
    }
}
