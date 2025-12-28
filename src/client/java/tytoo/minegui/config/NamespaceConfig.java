package tytoo.minegui.config;

import tytoo.minegui.util.ResourceId;

import java.util.Objects;

public record NamespaceConfig(
        String namespace,
        boolean viewportEnabled,
        boolean dockspaceEnabled,
        float globalScale,
        ResourceId globalStyleKey
) {
    public NamespaceConfig {
        namespace = Objects.requireNonNull(namespace, "namespace");
        if (!Float.isFinite(globalScale) || globalScale <= 0.0f) {
            globalScale = 1.0f;
        }
    }

    public static NamespaceConfig defaults(String namespace) {
        return new NamespaceConfig(namespace, true, true, 1.0f, null);
    }

    public NamespaceConfig withViewportEnabled(boolean enabled) {
        return new NamespaceConfig(namespace, enabled, dockspaceEnabled, globalScale, globalStyleKey);
    }

    public NamespaceConfig withDockspaceEnabled(boolean enabled) {
        return new NamespaceConfig(namespace, viewportEnabled, enabled, globalScale, globalStyleKey);
    }

    public NamespaceConfig withGlobalScale(float scale) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, scale, globalStyleKey);
    }

    public NamespaceConfig withGlobalStyleKey(ResourceId styleKey) {
        return new NamespaceConfig(namespace, viewportEnabled, dockspaceEnabled, globalScale, styleKey);
    }
}
