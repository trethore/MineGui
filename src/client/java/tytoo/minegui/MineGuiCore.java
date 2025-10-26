package tytoo.minegui;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.command.MineGuiClientCommands;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.util.ImGuiImageUtils;

import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public final class MineGuiCore {
    public static final String ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiCore.class);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(ID);
    private static boolean reloadListenerRegistered;
    private static MineGuiInitializationOptions initializationOptions = MineGuiInitializationOptions.defaults();

    private MineGuiCore() {
    }

    public static void init() {
        init(MineGuiInitializationOptions.defaults());
    }

    public static synchronized void init(MineGuiInitializationOptions options) {
        initializationOptions = Objects.requireNonNull(options, "options");
        GlobalConfigManager.configureDefaultNamespace(initializationOptions.configNamespace());
        GlobalConfigManager.setConfigIgnored(initializationOptions.ignoreGlobalConfig());
        boolean shouldAutoLoad = initializationOptions.loadGlobalConfig() && !initializationOptions.ignoreGlobalConfig();
        GlobalConfigManager.setAutoLoadEnabled(shouldAutoLoad);
        if (initializationOptions.ignoreGlobalConfig()) {
            GlobalConfigManager.ensureContext(initializationOptions.configNamespace());
        } else if (shouldAutoLoad) {
            GlobalConfigManager.load(initializationOptions.configNamespace());
        } else {
            GlobalConfigManager.ensureContext(initializationOptions.configNamespace());
        }
        registerReloadListener();
        MineGuiClientCommands.register();
    }

    private static synchronized void registerReloadListener() {
        if (reloadListenerRegistered) {
            return;
        }
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
        reloadListenerRegistered = true;
    }

    public static void loadConfig() {
        if (initializationOptions.ignoreGlobalConfig() || !initializationOptions.loadGlobalConfig()) {
            return;
        }
        GlobalConfigManager.load(initializationOptions.configNamespace());
    }

    public static String getConfigNamespace() {
        return initializationOptions.configNamespace();
    }

    public static boolean isGlobalConfigAutoLoaded() {
        return initializationOptions.loadGlobalConfig() && !initializationOptions.ignoreGlobalConfig();
    }

    public static boolean isGlobalConfigIgnored() {
        return initializationOptions.ignoreGlobalConfig();
    }

    public static MineGuiInitializationOptions getInitializationOptions() {
        return initializationOptions;
    }
}
