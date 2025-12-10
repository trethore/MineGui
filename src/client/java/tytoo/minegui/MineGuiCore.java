package tytoo.minegui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tytoo.minegui.command.MineGuiClientCommands;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.imgui.ImGuiLoader;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui.runtime.MineGuiRuntimeContext;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.util.MinecraftIdentifiers;
import tytoo.minegui.util.ResourceId;

import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public final class MineGuiCore {
    public static final String ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiCore.class);
    private static final ResourceId IMGUI_IMAGES_RELOAD_ID = ResourceId.of(ID, "imgui_images");
    private static boolean reloadListenerRegistered;
    private static boolean lifecycleRegistered;

    private static MineGuiRuntimeContext context;

    private MineGuiCore() {
    }

    public static synchronized MineGuiContext init(Path configPath) {
        return init(configPath, MineGuiInitializationOptions.defaults());
    }

    public static synchronized MineGuiContext init(Path configPath, MineGuiInitializationOptions options) {
        if (context != null) {
            return context;
        }
        Objects.requireNonNull(configPath, "configPath");
        Objects.requireNonNull(options, "options");

        // Setup global config to use the provided path (simplified)
        // For now, we assume GlobalConfigManager handles "main" namespace mapping to this path
        // Ideally we refactor GlobalConfigManager too, but for now:
        GlobalConfigManager.configureDefaultNamespace("main");

        context = new MineGuiRuntimeContext(options);

        registerReloadListener();
        registerLifecycleHandlers();
        MineGuiClientCommands.register();
        return context;
    }

    public static MineGuiContext getContext() {
        return context;
    }

    private static synchronized void registerReloadListener() {
        if (reloadListenerRegistered) {
            return;
        }
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return MinecraftIdentifiers.toMinecraft(IMGUI_IMAGES_RELOAD_ID);
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
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ImGuiLoader.onClientStarted());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());
        lifecycleRegistered = true;
    }

    public static void loadConfig() {
        if (context == null) return;
        GlobalConfigManager.load("main");
    }

    public static void saveConfig() {
        if (context == null) return;
        GlobalConfigManager.save("main");
    }

    public static String getConfigNamespace() {
        return "main";
    }

    public static void requestReload() {
        ImGuiLoader.requestReload();
    }

    public static boolean isInitialized() {
        return context != null;
    }
}
