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
import tytoo.minegui.config.ConfigRegistry;
import tytoo.minegui.config.GlobalConfigNamespaceConfigStore;
import tytoo.minegui.config.GlobalConfigService;
import tytoo.minegui.config.NamespaceConfigStore;
import tytoo.minegui.imgui.ImGuiLoader;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui.runtime.MineGuiRuntimeContext;
import tytoo.minegui.style.StyleManager;
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
        Objects.requireNonNull(options, "options");
        String namespace = options.namespace();
        MineGuiRuntimeContext existing = CONTEXTS.get(namespace);
        if (existing != null) {
            return existing;
        }

        ConfigRegistry.setDefaultNamespace(ID);
        applyConfigOptions(options);

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

    private static void applyConfigOptions(MineGuiInitializationOptions options) {
        String namespace = options.namespace();
        NamespaceConfigStore store = options.configStore();
        boolean usesGlobalStore = store instanceof GlobalConfigNamespaceConfigStore;
        if (usesGlobalStore && options.configRoot() != null) {
            ConfigRegistry.configure(options.configRoot());
        }
        GlobalConfigService globalConfig = ConfigRegistry.get(namespace);
        if (usesGlobalStore && options.configPathStrategy() != null) {
            globalConfig.setPathStrategy(options.configPathStrategy());
        }
        if (options.ignoreGlobalConfig() || !options.loadGlobalConfig()) {
            globalConfig.setConfigIgnored(true);
            return;
        }
        globalConfig.setConfigIgnored(false);
        globalConfig.setAutoLoadEnabled(true);
        if (options.featureProfile() != null) {
            globalConfig.setFeatureProfile(options.featureProfile());
        }
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
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            fireShutdownListeners();
            saveConfig();
            StyleManager.cleanup();
        });
        lifecycleRegistered = true;
    }

    public static void loadConfig() {
        boolean shouldLoadGlobal = false;
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            MineGuiInitializationOptions options = context.options();
            if (options.ignoreGlobalConfig() || !options.loadGlobalConfig()) {
                continue;
            }
            context.config().reload();
            shouldLoadGlobal = true;
        }
        if (shouldLoadGlobal) {
            ConfigRegistry.get().load();
        }
    }

    public static void saveConfig() {
        boolean shouldSaveGlobal = false;
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.persistence().flushLayouts();
            MineGuiInitializationOptions options = context.options();
            if (options.ignoreGlobalConfig() || !options.loadGlobalConfig()) {
                continue;
            }
            context.config().save();
            shouldSaveGlobal = true;
        }
        if (shouldSaveGlobal) {
            ConfigRegistry.get().save();
        }
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

    public static void fireContextReadyListeners() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.fireContextReady();
        }
    }

    private static void fireShutdownListeners() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.fireShutdown();
        }
    }
}
