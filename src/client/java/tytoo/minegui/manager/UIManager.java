package tytoo.minegui.manager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.style.StyleScope;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;
import tytoo.minegui.view.VisibilityListener;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;
import tytoo.minegui.view.persistence.ViewPersistenceManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UIManager implements VisibilityListener {
    private static final Map<String, UIManager> INSTANCES = new ConcurrentHashMap<>();

    private final String namespace;
    private final StyleManager styleManager;
    @Getter
    private final List<View> views = new CopyOnWriteArrayList<>();
    private final List<Runnable> renderCallbacks = new CopyOnWriteArrayList<>();
    @Setter
    private ViewPersistenceManager persistenceManager;
    @Getter
    private volatile CursorPolicy defaultCursorPolicy;
    private volatile boolean visibilityCacheValid;
    private volatile boolean cachedHasVisibleViews;

    private UIManager(String namespace) {
        this.namespace = namespace;
        this.styleManager = StyleManager.get(namespace);
        this.defaultCursorPolicy = CursorPolicies.empty();
    }

    public static UIManager get(String namespace) {
        return INSTANCES.computeIfAbsent(namespace, UIManager::new);
    }

    public static UIManager getInstance() {
        return get(MineGuiCore.getConfigNamespace());
    }

    private ViewPersistenceManager persistence() {
        if (persistenceManager == null) {
            MineGuiContext context = MineGuiCore.getContext(namespace);
            if (context != null) {
                persistenceManager = context.persistence();
            }
        }
        return persistenceManager;
    }

    public String namespace() {
        return namespace;
    }


    public void registerRenderCallback(Runnable callback) {
        if (callback != null && !renderCallbacks.contains(callback)) {
            renderCallbacks.add(callback);
            invalidateVisibilityCache();
        }
    }


    public void unregisterRenderCallback(Runnable callback) {
        if (callback != null) {
            renderCallbacks.remove(callback);
            invalidateVisibilityCache();
        }
    }

    public <T extends View> T register(T view) {
        if (view == null) {
            return null;
        }
        if (!views.contains(view)) {
            views.add(view);
            view.addVisibilityListener(this);
            view.attach();
            view.applyDefaultCursorPolicy(defaultCursorPolicy);
            invalidateVisibilityCache();
            ViewPersistenceManager manager = persistence();
            if (manager != null) {
                manager.register(view);
            }
        }
        return view;
    }

    public <T extends View> T registerAndShow(T view) {
        if (view == null) {
            return null;
        }
        register(view);
        view.show();
        return view;
    }

    public void registerAll(View... viewsToRegister) {
        if (viewsToRegister == null) {
            return;
        }
        for (View view : viewsToRegister) {
            register(view);
        }
    }

    public void unregister(View view) {
        if (view == null) {
            return;
        }
        if (view.isVisible()) {
            view.setVisible(false);
        }
        view.removeVisibilityListener(this);
        views.remove(view);
        view.detach();
        invalidateVisibilityCache();
        ViewPersistenceManager manager = persistence();
        if (manager != null) {
            manager.unregister(view);
        }
    }

    public void setDefaultCursorPolicy(CursorPolicy policy) {
        CursorPolicy resolved = policy != null ? policy : CursorPolicies.empty();
        if (this.defaultCursorPolicy == resolved) {
            return;
        }
        this.defaultCursorPolicy = resolved;
        for (View view : views) {
            if (view != null) {
                view.applyDefaultCursorPolicy(resolved);
            }
        }
    }

    public boolean hasVisibleViews() {
        if (!renderCallbacks.isEmpty()) {
            return true;
        }
        if (visibilityCacheValid) {
            return cachedHasVisibleViews;
        }
        boolean result = views.stream().anyMatch(View::isVisible);
        cachedHasVisibleViews = result;
        visibilityCacheValid = true;
        return result;
    }

    @Override
    public void onVisibilityChanged(View view, boolean visible) {
        invalidateVisibilityCache();
    }

    private void invalidateVisibilityCache() {
        visibilityCacheValid = false;
    }

    public boolean hasViews() {
        return !views.isEmpty() || !renderCallbacks.isEmpty();
    }

    public void saveLayoutNow(View view) {
        ViewPersistenceManager manager = persistence();
        if (manager != null) {
            manager.saveLayoutNow(view);
        }
    }

    public void saveStyleSnapshot(View view) {
        StyleDescriptor descriptor = styleManager.getEffectiveDescriptor().orElse(null);
        saveStyleSnapshot(view, descriptor);
    }

    public void saveStyleSnapshot(View view, StyleDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        ViewPersistenceManager manager = persistence();
        if (manager != null) {
            manager.saveStyleSnapshot(view, descriptor, true);
        }
    }

    public void deleteStyleSnapshot(View view) {
        ViewPersistenceManager manager = persistence();
        if (manager != null) {
            manager.deleteStyleSnapshot(view);
        }
    }

    public void render() {
        if (views.isEmpty() && renderCallbacks.isEmpty()) {
            return;
        }
        StyleManager.pushActive(styleManager);
        try {
            ViewPersistenceManager manager = persistence();
            if (manager != null) {
                manager.ensureSharedLayoutLoaded();
            }
            renderViews();
            renderCallbacks();
            if (manager != null) {
                manager.flushLayouts();
            }
        } finally {
            StyleManager.popActive(styleManager);
        }
    }

    private void renderCallbacks() {
        for (Runnable callback : renderCallbacks) {
            try {
                callback.run();
            } catch (Exception e) {
                MineGuiCore.LOGGER.error("Error executing MineGui render callback for namespace '{}'", namespace, e);
            }
        }
    }

    private void renderViews() {
        for (View view : views) {
            if (view == null) {
                continue;
            }
            if (!view.isVisible()) {
                continue;
            }
            ViewPersistenceManager manager = persistence();
            if (manager != null) {
                manager.ensureLoaded(view);
            }
            ResourceId originalKey = styleManager.getGlobalStyleKey();
            StyleDescriptor originalDescriptor = styleManager.getEffectiveDescriptor().orElse(null);
            applyViewBaseStyle(view, originalDescriptor);
            Profilers.get().push(view.getClass().getSimpleName());
            StyleDelta delta = view.configureStyleDelta();
            try (StyleScope ignored = delta != null ? StyleScope.push(delta) : null) {
                view.render();
                if (manager != null && view.isPersistentStyle()) {
                    styleManager.getEffectiveDescriptor().ifPresent(effective -> manager.saveStyleSnapshot(view, effective, false));
                }
            } finally {
                Profilers.get().pop();
                restoreBaseStyle(originalKey, originalDescriptor);
                if (manager != null) {
                    if (view.isPersistentLayout()) {
                        manager.markLayoutDirty(view, false);
                    }
                }
            }
        }
    }

    private StyleDescriptor applyViewBaseStyle(View view, StyleDescriptor fallbackDescriptor) {
        ResourceId styleKey = view.getStyleKey();
        if (styleKey != null) {
            styleManager.setGlobalStyleKeyTransient(styleKey);
        }
        StyleDescriptor descriptor = styleManager.getEffectiveDescriptor().orElse(fallbackDescriptor);
        StyleDescriptor persisted = Optional.ofNullable(persistence())
                .flatMap(manager -> manager.styleSnapshot(view))
                .orElse(null);
        StyleDescriptor resolved = persisted != null ? persisted : descriptor;
        if (resolved != null) {
            StyleDescriptor updated = view.configureBaseStyle(resolved);
            if (updated != null) {
                resolved = updated;
            }
            styleManager.setGlobalDescriptor(resolved);
        }
        styleManager.apply();
        return styleManager.getEffectiveDescriptor().orElse(resolved);
    }

    private void restoreBaseStyle(ResourceId originalKey, StyleDescriptor originalDescriptor) {
        styleManager.setGlobalStyleKeyTransient(originalKey);
        if (originalDescriptor != null) {
            styleManager.setGlobalDescriptor(originalDescriptor);
        }
        styleManager.apply();
    }
}
