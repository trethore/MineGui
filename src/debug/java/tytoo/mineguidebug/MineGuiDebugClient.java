package tytoo.mineguidebug;

import net.fabricmc.api.ClientModInitializer;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.runtime.MineGuiContext;

@SuppressWarnings("unused")
public final class MineGuiDebugClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MineGuiContext context = MineGuiCore.init(MineGuiInitializationOptions.defaults(MineGuiDebugCore.ID));
        MineGuiDebugCore.init(context);
        MineGuiDebugCore.LOGGER.info("Hello developer! MineGui is initialized !");
    }
}
