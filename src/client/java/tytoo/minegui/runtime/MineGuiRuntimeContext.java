package tytoo.minegui.runtime;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.MineGuiInitializationOptions;
import tytoo.minegui.config.ConfigRegistry;
import tytoo.minegui.config.MemoryNamespaceConfigStore;
import tytoo.minegui.config.NamespaceConfigStore;
import tytoo.minegui.imgui.ImGuiContextManager;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;
import tytoo.minegui.view.persistence.DefaultViewPersistenceAdapter;
import tytoo.minegui.view.persistence.ViewPersistenceAdapter;
import tytoo.minegui.view.persistence.ViewPersistenceManager;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class MineGuiRuntimeContext implements MineGuiContext {
    private final MineGuiInitializationOptions options;
    private final NamespaceConfigService config;
    private final UIManager uiManager;
    private final StyleManager styleManager;
    private final ViewPersistenceManager persistenceManager;
    private final List<MineGuiLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();
    private ResourceId defaultCursorPolicyId;
    private CursorPolicy defaultCursorPolicy;
    private volatile DockspaceCustomizer dockspaceCustomizer;

    public MineGuiRuntimeContext(MineGuiInitializationOptions options) {
        this.options = options;
        NamespaceConfigStore store = options.configStore();
        String namespace = options.namespace();
        if (options.ignoreGlobalConfig() || !options.loadGlobalConfig()) {
            store = new MemoryNamespaceConfigStore();
        }
        this.config = new NamespaceConfigService(namespace, store);

        this.uiManager = UIManager.get(namespace);
        this.styleManager = StyleManager.get(namespace);
        ViewPersistenceAdapter persistenceAdapter = options.viewPersistenceAdapter();
        if (persistenceAdapter == null) {
            persistenceAdapter = new DefaultViewPersistenceAdapter(ConfigRegistry.get(namespace).viewSavesDirectory());
        }
        this.persistenceManager = new ViewPersistenceManager(namespace, this.config, persistenceAdapter);
        if (options.fontRegistrar() != null && ImGuiContextManager.isContextInitialized()) {
            MineGuiCore.LOGGER.warn("Font registrar for namespace '{}' was registered after ImGui initialization; it will not run until the next client restart.", namespace);
        }
        this.uiManager.setPersistenceManager(this.persistenceManager);
        StyleManager defaultStyleManager = StyleManager.get(ConfigRegistry.defaultNamespace());
        if (this.styleManager.getGlobalDescriptor().isEmpty()) {
            defaultStyleManager.getGlobalDescriptor()
                    .map(descriptor -> StyleDescriptor.builder().fromDescriptor(descriptor).build())
                    .ifPresent(this.styleManager::setGlobalDescriptor);
        }
        setDefaultCursorPolicy(options.defaultCursorPolicyId());
        setDockspaceCustomizer(options.dockspaceCustomizer());
    }

    @Override
    public MineGuiInitializationOptions options() {
        return options;
    }

    @Override
    public NamespaceConfigService config() {
        return config;
    }

    @Override
    public UIManager ui() {
        return uiManager;
    }

    @Override
    public StyleManager style() {
        return styleManager;
    }

    @Override
    public ViewPersistenceManager persistence() {
        return persistenceManager;
    }

    @Override
    public ResourceId defaultCursorPolicyId() {
        return defaultCursorPolicyId;
    }

    @Override
    public CursorPolicy defaultCursorPolicy() {
        return defaultCursorPolicy;
    }

    @Override
    public void setDefaultCursorPolicy(ResourceId policyId) {
        ResourceId normalized = policyId != null ? policyId : CursorPolicies.emptyId();
        CursorPolicy resolved = CursorPolicyRegistry.resolvePolicyOrDefault(normalized, CursorPolicies.empty());
        if (Objects.equals(normalized, defaultCursorPolicyId) && resolved == defaultCursorPolicy) {
            return;
        }
        this.defaultCursorPolicyId = normalized;
        this.defaultCursorPolicy = resolved;
        this.uiManager.setDefaultCursorPolicy(resolved);
    }

    @Override
    public DockspaceCustomizer dockspaceCustomizer() {
        return dockspaceCustomizer;
    }

    @Override
    public void setDockspaceCustomizer(DockspaceCustomizer customizer) {
        dockspaceCustomizer = customizer != null ? customizer : DockspaceCustomizer.noop();
    }

    @Override
    public void addLifecycleListener(MineGuiLifecycleListener listener) {
        if (listener != null && !lifecycleListeners.contains(listener)) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(MineGuiLifecycleListener listener) {
        if (listener != null) {
            lifecycleListeners.remove(listener);
        }
    }

    public void fireContextReady() {
        notifyLifecycle("context_ready", listener -> listener.onContextReady(this));
    }

    public void firePreRender() {
        notifyLifecycle("pre_render", listener -> listener.onPreRender(this));
    }

    public void firePostRender() {
        notifyLifecycle("post_render", listener -> listener.onPostRender(this));
    }

    public void fireShutdown() {
        notifyLifecycle("shutdown", listener -> listener.onShutdown(this));
    }

    private void notifyLifecycle(String phase, Consumer<MineGuiLifecycleListener> action) {
        for (MineGuiLifecycleListener listener : lifecycleListeners) {
            try {
                action.accept(listener);
            } catch (RuntimeException exception) {
                MineGuiCore.LOGGER.error("MineGui lifecycle listener '{}' failed for namespace '{}'", phase, options.namespace(), exception);
            }
        }
    }
}
