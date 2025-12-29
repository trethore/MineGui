package tytoo.minegui;

import net.fabricmc.loader.api.FabricLoader;
import tytoo.minegui.config.PersistenceFlags;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;

import java.nio.file.Path;
import java.util.Objects;

public record MineGuiOptions(
        String namespace,
        Path configRoot,
        PersistenceFlags persistence,
        ResourceId defaultCursorPolicy,
        DockspaceCustomizer dockspaceCustomizer
) {

    public MineGuiOptions {
        Objects.requireNonNull(namespace, "namespace");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("Namespace must be non-blank");
        }
        if (configRoot == null) {
            configRoot = FabricLoader.getInstance().getConfigDir();
        }
        if (persistence == null) {
            persistence = PersistenceFlags.all();
        }
        if (defaultCursorPolicy == null) {
            defaultCursorPolicy = CursorPolicies.clickToLockId();
        }
        if (dockspaceCustomizer == null) {
            dockspaceCustomizer = DockspaceCustomizer.noop();
        }
    }

    public static MineGuiOptions of(String namespace) {
        return builder(namespace).build();
    }

    public static MineGuiOptions lite(String namespace) {
        return builder(namespace).none().build();
    }

    public static Builder builder(String namespace) {
        return new Builder(namespace);
    }

    public Path namespaceRoot() {
        return configRoot.resolve(namespace);
    }

    public static final class Builder {
        private final String namespace;
        private Path configRoot;
        private boolean persistConfig = true;
        private boolean persistLayouts = true;
        private boolean persistStyles = true;
        private ResourceId defaultCursorPolicy;
        private DockspaceCustomizer dockspaceCustomizer;

        private Builder(String namespace) {
            this.namespace = Objects.requireNonNull(namespace, "namespace");
        }

        public Builder configRoot(Path path) {
            this.configRoot = path;
            return this;
        }

        public Builder withConfig(boolean enabled) {
            this.persistConfig = enabled;
            return this;
        }

        public Builder withLayouts(boolean enabled) {
            this.persistLayouts = enabled;
            return this;
        }

        public Builder withStyles(boolean enabled) {
            this.persistStyles = enabled;
            return this;
        }

        public Builder none() {
            this.persistConfig = false;
            this.persistLayouts = false;
            this.persistStyles = false;
            return this;
        }

        public Builder cursorPolicy(ResourceId policy) {
            this.defaultCursorPolicy = policy;
            return this;
        }

        public Builder dockspaceCustomizer(DockspaceCustomizer customizer) {
            this.dockspaceCustomizer = customizer;
            return this;
        }

        public MineGuiOptions build() {
            PersistenceFlags flags = new PersistenceFlags(persistConfig, persistLayouts, persistStyles);
            return new MineGuiOptions(
                    namespace,
                    configRoot,
                    flags,
                    defaultCursorPolicy,
                    dockspaceCustomizer
            );
        }
    }
}
