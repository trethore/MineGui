package tytoo.minegui.config;

import tytoo.minegui.util.ResourceId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record NamespaceConfig(
        String namespace,
        boolean viewportEnabled,
        boolean dockspaceEnabled,
        float globalScale,
        String configPath,
        String viewSavesPath,
        ResourceId globalStyleKey,
        Map<String, String> viewStyles
) {
    public NamespaceConfig {
        namespace = Objects.requireNonNull(namespace, "namespace");
        if (!Float.isFinite(globalScale) || globalScale <= 0.0f) {
            globalScale = 1.0f;
        }
        configPath = configPath != null ? configPath : "";
        viewSavesPath = viewSavesPath != null ? viewSavesPath : "";
        viewStyles = wrapStyles(viewStyles);
    }

    public static NamespaceConfig defaults(String namespace) {
        return new NamespaceConfig(namespace, true, true, 1.0f, "", "", null, Map.of());
    }

    private static Map<String, String> wrapStyles(Map<String, String> styles) {
        if (styles == null || styles.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> copy = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            copy.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(copy);
    }

    public NamespaceConfig withViewportEnabled(boolean enabled) {
        return new NamespaceConfig(namespace, enabled, dockspaceEnabled, globalScale, configPath, viewSavesPath, globalStyleKey, viewStyles);
    }

    public NamespaceConfig withDockspaceEnabled(boolean enabled) {
        return new NamespaceConfig(namespace, viewportEnabled, enabled, globalScale, configPath, viewSavesPath, globalStyleKey, viewStyles);
    }

    public NamespaceConfig withGlobalScale(float scale) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, scale, configPath, viewSavesPath, globalStyleKey, viewStyles);
    }

    public NamespaceConfig withConfigPath(String path) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, path, viewSavesPath, globalStyleKey, viewStyles);
    }

    public NamespaceConfig withViewSavesPath(String path) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, configPath, path, globalStyleKey, viewStyles);
    }

    public NamespaceConfig withGlobalStyleKey(ResourceId styleKey) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, configPath, viewSavesPath, styleKey, viewStyles);
    }

    public NamespaceConfig withViewStyle(String viewId, String styleRef) {
        Map<String, String> next = new LinkedHashMap<>(viewStyles);
        if (styleRef == null || styleRef.isBlank()) {
            next.remove(viewId);
        } else {
            next.put(viewId, styleRef);
        }
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, configPath, viewSavesPath, globalStyleKey, next);
    }

    public NamespaceConfig withoutViewStyle(String viewId) {
        if (!viewStyles.containsKey(viewId)) {
            return this;
        }
        Map<String, String> next = new LinkedHashMap<>(viewStyles);
        next.remove(viewId);
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, configPath, viewSavesPath, globalStyleKey, next);
    }

    public NamespaceConfig withViewStyles(Map<String, String> styles) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, configPath, viewSavesPath, globalStyleKey, styles);
    }
}
