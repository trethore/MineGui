package tytoo.mineguidebug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.runtime.MineGuiContext;

@SuppressWarnings("unused")
public final class MineGuiDebugClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MineGuiContext context = MineGuiCore.init(
                MineGuiInitializationOptions.builder()
                        .namespace(MineGuiDebugCore.ID)
                        .configRoot(FabricLoader.getInstance().getConfigDir().resolve(MineGuiDebugCore.ID))
                        .build()
        );
        MineGuiDebugCore.init(context);
        MineGuiDebugCore.LOGGER.info("Hello developer! MineGui is initialized !");
    }
}
