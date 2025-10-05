package tytoo.minegui;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@SuppressWarnings("unused")
public final class MineGuiCore {
    public static final String ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiCore.class);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(ID);

    private MineGuiCore() {
    }

    public static void init() {


    }
}