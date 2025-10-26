package tytoo.minegui.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public final class MineGuiClientCommands {
    private MineGuiClientCommands() {
    }

    public static void register() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("minegui")
                        .then(MineGuiReloadCommand.builder())
                        .then(MineGuiExportStyleCommand.builder())
        ));
    }
}
