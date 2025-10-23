package tytoo.minegui;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.util.ImGuiImageUtils;

import java.nio.file.Path;

@SuppressWarnings("unused")
public final class MineGuiCore {
    public static final String ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiCore.class);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(ID);

    private MineGuiCore() {
    }

    public static void init() {
        loadConfig();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of(ID, "imgui_images");
            }

            @Override
            public void reload(ResourceManager manager) {
                ImGuiImageUtils.invalidateAll();
            }
        });
    }

    public static void loadConfig() {
        GlobalConfigManager.load();
    }
}
