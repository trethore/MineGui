package tytoo.mineguidebug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import tytoo.minegui.MineGui;
import tytoo.minegui.MineGuiOptions;
import tytoo.minegui.runtime.MineGuiContext;

@SuppressWarnings("unused")
public final class MineGuiDebugClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DebugFonts.registerAll();

        MineGuiContext context = MineGui.setup(
                MineGuiOptions.builder(MineGuiDebugCore.ID)
                        .configRoot(FabricLoader.getInstance().getConfigDir())
                        .build()
        );

        MineGuiDebugCore.init(context);

        HelloMineGui.run();

        MineGuiDebugCore.LOGGER.info("Hello developer! MineGui is initialized !");
    }
}
