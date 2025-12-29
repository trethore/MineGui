package tytoo.minegui.manager;

import lombok.Getter;
import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui.runtime.MineGuiRuntimeContext;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.style.StyleScope;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;
import tytoo.minegui.view.VisibilityListener;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UIManager implements VisibilityListener {

    private static final Map<String, UIManager> INSTANCES = new ConcurrentHashMap<>();

    private final String namespace;
    private final StyleManager styleManager;
    @Getter
    private final List<View> views = new CopyOnWriteArrayList<>();
    private final List<Runnable> renderCallbacks = new CopyOnWriteArrayList<>();

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
            MineGuiRuntimeContext context = getContext();
            if (context != null) {
                context.registerView(view);
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
        MineGuiRuntimeContext context = getContext();
        if (context != null) {
            context.unregisterView(view);
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

    public boolean hasViews() {
        return !views.isEmpty() || !renderCallbacks.isEmpty();
    }

    @Override
    public void onVisibilityChanged(View view, boolean visible) {
        invalidateVisibilityCache();
    }

    public void render() {
        if (views.isEmpty() && renderCallbacks.isEmpty()) {
            return;
        }
        StyleManager.pushActive(styleManager);
        try {
            MineGuiRuntimeContext context = getContext();
            renderViews();
            renderCallbacks();
            if (context != null) {
                context.flushLayouts();
            }
        } finally {
            StyleManager.popActive(styleManager);
        }
    }

    private void invalidateVisibilityCache() {
        visibilityCacheValid = false;
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
        MineGuiRuntimeContext context = getContext();
        for (View view : views) {
            if (view == null || !view.isVisible()) {
                continue;
            }
            if (context != null) {
                context.ensureViewLoaded(view);
            }
            ResourceId originalKey = styleManager.getGlobalStyleKey();
            StyleDescriptor originalDescriptor = styleManager.getEffectiveDescriptor().orElse(null);
            applyViewBaseStyle(view, originalDescriptor);
            Profilers.get().push(view.getClass().getSimpleName());
            StyleDelta delta = view.configureStyleDelta();
            try (StyleScope ignored = delta != null ? StyleScope.push(delta) : null) {
                view.render();
            } finally {
                Profilers.get().pop();
                restoreBaseStyle(originalKey, originalDescriptor);
                if (context != null && view.isPersistentLayout()) {
                    context.markLayoutDirty(view);
                }
            }
        }
    }

    private void applyViewBaseStyle(View view, StyleDescriptor fallbackDescriptor) {
        ResourceId styleKey = view.getStyleKey();
        if (styleKey != null) {
            styleManager.setGlobalStyleKeyTransient(styleKey);
        }
        StyleDescriptor resolved = styleManager.getEffectiveDescriptor().orElse(fallbackDescriptor);
        if (resolved != null) {
            StyleDescriptor updated = view.configureBaseStyle(resolved);
            if (updated != null) {
                resolved = updated;
            }
            styleManager.setGlobalDescriptor(resolved);
        }
        styleManager.apply();
    }

    private void restoreBaseStyle(ResourceId originalKey, StyleDescriptor originalDescriptor) {
        styleManager.setGlobalStyleKeyTransient(originalKey);
        if (originalDescriptor != null) {
            styleManager.setGlobalDescriptor(originalDescriptor);
        }
        styleManager.apply();
    }

    private MineGuiRuntimeContext getContext() {
        MineGuiContext context = MineGuiCore.getContext(namespace);
        if (context instanceof MineGuiRuntimeContext runtimeContext) {
            return runtimeContext;
        }
        return null;
    }
}
