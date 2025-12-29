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
import tytoo.minegui.imgui.GlobalLayoutManager;
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

    private static volatile boolean reloadListenerRegistered;
    private static volatile boolean lifecycleRegistered;

    private MineGuiCore() {
    }

    public static synchronized MineGuiContext init(MineGuiOptions options) {
        Objects.requireNonNull(options, "options");
        String namespace = options.namespace();
        MineGuiRuntimeContext existing = CONTEXTS.get(namespace);
        if (existing != null) {
            return existing;
        }

        MineGuiRuntimeContext context = new MineGuiRuntimeContext(options);
        CONTEXTS.put(namespace, context);

        registerReloadListener();
        registerLifecycleHandlers();
        MineGuiClientCommands.register();
        return context;
    }

    public static MineGuiContext getContext() {
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

    public static void saveAll() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.save();
        }
    }

    public static void loadAll() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.load();
        }
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
            GlobalLayoutManager.flush(true);
            saveAll();
            StyleManager.cleanup();
        });
        lifecycleRegistered = true;
    }

    private static void fireShutdownListeners() {
        for (MineGuiRuntimeContext context : CONTEXTS.values()) {
            context.fireShutdown();
        }
    }
}
