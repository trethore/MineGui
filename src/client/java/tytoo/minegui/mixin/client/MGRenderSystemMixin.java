package tytoo.minegui.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.imgui.ImGuiLoader;
import tytoo.minegui.manager.UIManager;

@Mixin(value = RenderSystem.class, remap = false)
public abstract class MGRenderSystemMixin {
    @Inject(at = @At("HEAD"), method = "flipFrame")
    private static void runTickTail(CallbackInfo ci) {
        if (!UIManager.getInstance().hasVisibleViews()) {
            return;
        }
        Profilers.get().push("MineGui Render");
        ImGuiLoader.onFrameRender();
        Profilers.get().pop();
    }
}
