package tytoo.minegui.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.imgui.ImGuiLoader;

@Mixin(value = RenderSystem.class, remap = false)
public class MGRenderSystemMixin {
    @Inject(at = @At("HEAD"), method = "flipFrame")
    private static void runTickTail(CallbackInfo ci) {
        Profilers.get().push("MineGui Render");
        ImGuiLoader.onFrameRender();
        Profilers.get().pop();
    }
}
