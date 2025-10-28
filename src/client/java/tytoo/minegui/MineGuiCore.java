package tytoo.minegui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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
import tytoo.minegui.imgui.ImGuiLoader;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.util.ImGuiImageUtils;

import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public final class MineGuiCore {
    public static final String ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiCore.class);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(ID);
    private static boolean reloadListenerRegistered;
    private static boolean lifecycleRegistered;
    private static boolean defaultNamespaceConfigured;
    private static MineGuiInitializationOptions initializationOptions = MineGuiInitializationOptions.defaults();

    private MineGuiCore() {
    }

    public static void init() {
        init(MineGuiInitializationOptions.defaults());
    }

    public static synchronized void init(MineGuiInitializationOptions options) {
        Objects.requireNonNull(options, "options");
        String namespace = options.configNamespace();
        if (!defaultNamespaceConfigured) {
            initializationOptions = options;
            GlobalConfigManager.configureDefaultNamespace(namespace);
            defaultNamespaceConfigured = true;
        } else {
            String defaultNamespace = GlobalConfigManager.getDefaultNamespace();
            if (namespace.equals(defaultNamespace)) {
                initializationOptions = options;
            } else {
                LOGGER.info("MineGui default namespace remains '{}'; additional namespace '{}' registered without overriding the default.", defaultNamespace, namespace);
            }
        }
        MineGuiNamespaces.initialize(options);
        registerReloadListener();
        registerLifecycleHandlers();
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

    private static synchronized void registerLifecycleHandlers() {
        if (lifecycleRegistered) {
            return;
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            for (var context : MineGuiNamespaces.all()) {
                context.viewSaves().flush();
            }
        });
        lifecycleRegistered = true;
    }

    public static void loadConfig() {
        if (!defaultNamespaceConfigured) {
            return;
        }
        if (initializationOptions.ignoreGlobalConfig() || !initializationOptions.loadGlobalConfig()) {
            return;
        }
        GlobalConfigManager.load(initializationOptions.configNamespace());
    }

    public static String getConfigNamespace() {
        return GlobalConfigManager.getDefaultNamespace();
    }

    public static boolean isGlobalConfigAutoLoaded() {
        if (!defaultNamespaceConfigured) {
            return false;
        }
        return initializationOptions.loadGlobalConfig() && !initializationOptions.ignoreGlobalConfig();
    }

    public static boolean isGlobalConfigIgnored() {
        if (!defaultNamespaceConfigured) {
            return false;
        }
        return initializationOptions.ignoreGlobalConfig();
    }

    public static MineGuiInitializationOptions getInitializationOptions() {
        return initializationOptions;
    }

    public static void requestReload() {
        ImGuiLoader.requestReload();
    }
}
