package tytoo.minegui.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;

@Mixin(MinecraftClient.class)
public abstract class MGMinecraftClientMixin {
    @Inject(method = "stop()V", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        MineGuiNamespaces.saveAllConfigs();
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onScreenChanged(CallbackInfo ci) {
        CursorPolicyRegistry.onScreenClosed();
    }
}
