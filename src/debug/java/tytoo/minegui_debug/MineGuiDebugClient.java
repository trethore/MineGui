package tytoo.minegui_debug;

import net.fabricmc.api.ClientModInitializer;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.runtime.MineGuiContext;

@SuppressWarnings("unused")
public final class MineGuiDebugClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MineGuiDebugCore.LOGGER.info("Hello developer! MineGui is initializing...");
        MineGuiContext context = MineGuiCore.init(MineGuiInitializationOptions.defaults(MineGuiDebugCore.ID));
        MineGuiDebugCore.init(context);
    }
}
