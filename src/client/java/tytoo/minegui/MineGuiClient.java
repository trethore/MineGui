package tytoo.minegui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.component.windows.TestWindow;
import tytoo.minegui.manager.UIManager;

public class MineGuiClient implements ClientModInitializer {
    public static final String MOD_ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            registerTestWindow();
        }
        LOGGER.info("MineGui Client Initialized");

    }

    private void registerTestWindow() {
        UIManager.getInstance().registerWindow(new TestWindow());
    }
}