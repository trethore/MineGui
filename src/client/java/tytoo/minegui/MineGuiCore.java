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
import tytoo.minegui.config.ConfigPathStrategies;
import tytoo.minegui.config.ConfigRegistry;
import tytoo.minegui.imgui.ImGuiLoader;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui.runtime.MineGuiRuntimeContext;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.util.MinecraftIdentifiers;
import tytoo.minegui.util.ResourceId;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class MineGuiCore {
    public static final String ID = "minegui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MineGuiCore.class);
    private static final ResourceId IMGUI_IMAGES_RELOAD_ID = ResourceId.of(ID, "imgui_images");
    private static final Map<String, MineGuiRuntimeContext> CONTEXTS = new ConcurrentHashMap<>();
    private static boolean reloadListenerRegistered;
    private static boolean lifecycleRegistered;

    private MineGuiCore() {
    }

    public static synchronized MineGuiContext init(MineGuiInitializationOptions options) {
        String namespace = options.namespace();
        if (CONTEXTS.containsKey(namespace)) {
            return CONTEXTS.get(namespace);
        }
        Objects.requireNonNull(options, "options");

        ConfigRegistry.setDefaultNamespace(ID);
        if (options.configRoot() != null) {
            ConfigRegistry.get(namespace).setPathStrategy(ConfigPathStrategies.root(options.configRoot()));
        }

        MineGuiRuntimeContext context = new MineGuiRuntimeContext(options);
        CONTEXTS.put(namespace, context);

        registerReloadListener();
        registerLifecycleHandlers();
        MineGuiClientCommands.register();
        return context;
    }

    public static MineGuiContext getContext() {
        MineGuiContext context = getContext(ConfigRegistry.defaultNamespace());
        if (context != null) {
            return context;
        }
        if (!CONTEXTS.isEmpty()) {
            return CONTEXTS.values().iterator().next();
        }
        return null;
    }

    public static MineGuiContext getContext(String namespace) {
        return CONTEXTS.get(namespace);
    }

    public static Collection<MineGuiRuntimeContext> getAllContexts() {
        return Collections.unmodifiableCollection(CONTEXTS.values());
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
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.config().reload();
        }
        ConfigRegistry.get().load();
    }

    public static void saveConfig() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.persistence().flushLayouts();
            context.config().save();
        }
        ConfigRegistry.get().save();
    }

    public static String getConfigNamespace() {
        return ConfigRegistry.defaultNamespace();
    }

    public static void requestReload() {
        ImGuiLoader.requestReload();
    }

    public static boolean isInitialized() {
        return !CONTEXTS.isEmpty();
    }

    public static boolean hasAnyVisibleViews() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            if (context.ui().hasVisibleViews()) {
                return true;
            }
        }
        return false;
    }
}
