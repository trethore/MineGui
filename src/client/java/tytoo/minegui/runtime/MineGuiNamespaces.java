package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.GlobalConfigManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MineGuiNamespaces {
    private static final Map<String, MineGuiNamespaceContext> CONTEXTS = new ConcurrentHashMap<>();

    private MineGuiNamespaces() {
    }

    public static MineGuiNamespaceContext initialize(MineGuiInitializationOptions options) {
        Objects.requireNonNull(options, "options");
        String namespace = Objects.requireNonNull(options.configNamespace(), "namespace");
        MineGuiNamespaceContext context = new MineGuiNamespaceContext(namespace, options);
        CONTEXTS.put(namespace, context);
        GlobalConfigManager.setConfigIgnored(namespace, options.ignoreGlobalConfig());
        boolean shouldAutoLoad = options.loadGlobalConfig() && !options.ignoreGlobalConfig();
        GlobalConfigManager.setAutoLoadEnabled(namespace, shouldAutoLoad);
        if (options.ignoreGlobalConfig()) {
            GlobalConfigManager.ensureContext(namespace);
        } else if (shouldAutoLoad) {
            GlobalConfigManager.load(namespace);
        } else {
            GlobalConfigManager.ensureContext(namespace);
        }
        return context;
    }

    public static MineGuiNamespaceContext get(String namespace) {
        return CONTEXTS.get(namespace);
    }

    public static Collection<MineGuiNamespaceContext> all() {
        return Collections.unmodifiableCollection(CONTEXTS.values());
    }

    public static boolean anyVisible() {
        for (MineGuiNamespaceContext context : CONTEXTS.values()) {
            if (context.ui().hasVisibleViews()) {
                return true;
            }
        }
        return false;
    }
}
