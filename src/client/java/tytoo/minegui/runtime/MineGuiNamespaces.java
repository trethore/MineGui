package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.persistence.ViewPersistenceAdapter;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.util.ResourceId;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MineGuiNamespaces {
    private static final Map<String, MineGuiNamespaceContext> CONTEXTS = new ConcurrentHashMap<>();

    private MineGuiNamespaces() {
    }

    public static MineGuiNamespaceContext initialize(String namespace) {
        return initialize(MineGuiInitializationOptions.defaults(namespace));
    }

    public static MineGuiNamespaceContext initialize(MineGuiInitializationOptions options) {
        Objects.requireNonNull(options, "options");
        String namespace = Objects.requireNonNull(options.configNamespace(), "namespace");
        GlobalConfigManager.setConfigPathStrategy(namespace, options.configPathStrategy());
        GlobalConfigManager.setConfigIgnored(namespace, options.ignoreGlobalConfig());
        GlobalConfigManager.setFeatureProfile(namespace, options.featureProfile());
        ViewSaveManager.setAdapter(namespace, options.viewPersistenceAdapter());
        boolean shouldAutoLoad = options.loadGlobalConfig() && !options.ignoreGlobalConfig();
        GlobalConfigManager.setAutoLoadEnabled(namespace, shouldAutoLoad);
        if (options.ignoreGlobalConfig()) {
            GlobalConfigManager.ensureContext(namespace);
        } else if (shouldAutoLoad) {
            GlobalConfigManager.load(namespace);
        } else {
            GlobalConfigManager.ensureContext(namespace);
        }
        MineGuiNamespaceContext context = new MineGuiNamespaceContext(namespace, options);
        CONTEXTS.put(namespace, context);
        return context;
    }

    public static MineGuiNamespaceContext get(String namespace) {
        return CONTEXTS.get(namespace);
    }

    public static Collection<MineGuiNamespaceContext> all() {
        return Collections.unmodifiableCollection(CONTEXTS.values());
    }

    public static boolean anyVisible() {
        boolean anyVisible = false;
        for (MineGuiNamespaceContext context : CONTEXTS.values()) {
            if (context.ui().hasVisibleViews()) {
                anyVisible = true;
            }
        }
        if (anyVisible) {
            CursorPolicyRegistry.ensureUnlockedIfRequested();
        }
        return anyVisible;
    }

    public static void saveAllConfigs() {
        for (MineGuiNamespaceContext context : CONTEXTS.values()) {
            GlobalConfigManager.save(context.namespace());
        }
    }

    public static void setDefaultCursorPolicy(ResourceId policyId) {
        setDefaultCursorPolicy(GlobalConfigManager.getDefaultNamespace(), policyId);
    }

    public static void setDefaultCursorPolicy(String namespace, ResourceId policyId) {
        MineGuiNamespaceContext context = CONTEXTS.get(namespace);
        if (context == null) {
            return;
        }
        context.setDefaultCursorPolicy(policyId);
    }

    public static void setDockspaceCustomizer(DockspaceCustomizer customizer) {
        setDockspaceCustomizer(GlobalConfigManager.getDefaultNamespace(), customizer);
    }

    public static void setDockspaceCustomizer(String namespace, DockspaceCustomizer customizer) {
        MineGuiNamespaceContext context = CONTEXTS.get(namespace);
        if (context == null) {
            return;
        }
        context.setDockspaceCustomizer(customizer);
    }

    public static void setViewPersistenceAdapter(ViewPersistenceAdapter adapter) {
        setViewPersistenceAdapter(GlobalConfigManager.getDefaultNamespace(), adapter);
    }

    public static void setViewPersistenceAdapter(String namespace, ViewPersistenceAdapter adapter) {
        ViewSaveManager.setAdapter(namespace, adapter);
    }
}
