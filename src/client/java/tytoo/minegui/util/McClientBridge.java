package tytoo.minegui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import java.util.Objects;
import java.util.Optional;

public final class McClientBridge {
    private McClientBridge() {
    }

    public static Optional<MinecraftClient> client() {
        return Optional.ofNullable(MinecraftClient.getInstance());
    }

    public static Optional<ClientPlayerEntity> player() {
        return client().map(mc -> mc.player);
    }

    public static Optional<ClientWorld> world() {
        return client().map(mc -> mc.world);
    }

    public static void execute(Runnable task) {
        Objects.requireNonNull(task, "task");
        client().ifPresent(client -> client.execute(task));
    }

    public static long windowHandle() {
        return client()
                .map(mc -> mc.getWindow().getHandle())
                .orElse(-1L);
    }

    public static boolean isOnMac() {
        return MinecraftClient.IS_SYSTEM_MAC;
    }

    public static boolean clientWantsCursorLock() {
        return client()
                .map(mc -> mc.currentScreen == null)
                .orElse(false);
    }

    public static Optional<MouseBridge> mouse() {
        return client()
                .map(mc -> mc.mouse)
                .map(MouseBridge::of);
    }
}
