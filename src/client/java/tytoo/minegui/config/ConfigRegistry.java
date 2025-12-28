package tytoo.minegui.config;

import net.fabricmc.loader.api.FabricLoader;
import tytoo.minegui.MineGuiCore;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ConfigRegistry {
    private static final String DEFAULT_NAMESPACE = MineGuiCore.ID;
    private static final Pattern VALID_NAMESPACE = Pattern.compile("[A-Za-z0-9._-]+");
    private static final ConfigPathStrategy DEFAULT_STRATEGY = ConfigPathStrategies.sandboxed();
    private static final Map<String, GlobalConfigService> SERVICES = new HashMap<>();
    private static Path configRoot = determineConfigRoot();
    private static String defaultNamespace = DEFAULT_NAMESPACE;

    private ConfigRegistry() {
    }

    public static synchronized void configure(Path rootPath) {
        if (rootPath == null) {
            return;
        }
        configRoot = rootPath.toAbsolutePath().normalize();
        rebindServices();
    }

    public static synchronized void setDefaultNamespace(String namespace) {
        defaultNamespace = sanitizeNamespace(namespace);
    }

    public static synchronized String defaultNamespace() {
        return defaultNamespace;
    }

    public static synchronized GlobalConfigService get() {
        return get(defaultNamespace);
    }

    public static synchronized GlobalConfigService get(String namespace) {
        String sanitized = sanitizeNamespace(namespace);
        return SERVICES.computeIfAbsent(sanitized, ns -> new GlobalConfigService(ns, configRoot, DEFAULT_STRATEGY));
    }

    public static synchronized GlobalConfigService getOrNull(String namespace) {
        String sanitized = sanitizeNamespace(namespace);
        return SERVICES.get(sanitized);
    }

    public static synchronized void register(String namespace) {
        get(namespace);
    }

    public static synchronized Path configRoot() {
        return configRoot;
    }

    private static String sanitizeNamespace(String namespace) {
        if (namespace == null) {
            return DEFAULT_NAMESPACE;
        }
        String trimmed = namespace.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_NAMESPACE;
        }
        if (!VALID_NAMESPACE.matcher(trimmed).matches()) {
            MineGuiCore.LOGGER.warn("Invalid namespace '{}'; using default '{}'", namespace, DEFAULT_NAMESPACE);
            return DEFAULT_NAMESPACE;
        }
        Path resolved = configRoot.resolve(trimmed).normalize();
        if (!resolved.startsWith(configRoot)) {
            MineGuiCore.LOGGER.warn("Namespace '{}' resolves outside of MineGui config root; using default '{}'", namespace, DEFAULT_NAMESPACE);
            return DEFAULT_NAMESPACE;
        }
        return trimmed;
    }

    private static Path determineConfigRoot() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return Objects.requireNonNullElseGet(configDir, () -> Path.of("config")).toAbsolutePath().normalize();
    }

    private static void rebindServices() {
        if (SERVICES.isEmpty()) {
            return;
        }
        Map<String, GlobalConfigService> previous = new HashMap<>(SERVICES);
        SERVICES.clear();
        for (Map.Entry<String, GlobalConfigService> entry : previous.entrySet()) {
            GlobalConfigService oldService = entry.getValue();
            GlobalConfigService refreshed = oldService.refreshWith(configRoot, DEFAULT_STRATEGY);
            SERVICES.put(entry.getKey(), refreshed);
        }
    }
}
