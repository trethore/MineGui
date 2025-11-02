package tytoo.minegui.manager;

import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.style.StyleScope;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UIManager {
    private static final Map<String, UIManager> INSTANCES = new ConcurrentHashMap<>();

    private final String namespace;
    private final ViewSaveManager viewSaveManager;
    private final StyleManager styleManager;
    private final List<View> views = new CopyOnWriteArrayList<>();
    private volatile CursorPolicy defaultCursorPolicy;

    private UIManager(String namespace) {
        this.namespace = namespace;
        this.viewSaveManager = ViewSaveManager.get(namespace);
        this.styleManager = StyleManager.get(namespace);
        this.defaultCursorPolicy = CursorPolicies.empty();
    }

    public static UIManager get(String namespace) {
        return INSTANCES.computeIfAbsent(namespace, UIManager::new);
    }

    public static UIManager getInstance() {
        return get(MineGuiCore.getConfigNamespace());
    }

    public String namespace() {
        return namespace;
    }

    public void register(View view) {
        if (view == null) {
            return;
        }
        if (!views.contains(view)) {
            views.add(view);
            viewSaveManager.register(view);
            view.attach(namespace, viewSaveManager);
            view.applyDefaultCursorPolicy(defaultCursorPolicy);
        }
    }

    public void unregister(View view) {
        if (view == null) {
            return;
        }
        if (view.isVisible()) {
            view.setVisible(false);
        }
        views.remove(view);
        viewSaveManager.unregister(view);
        view.detach();
    }

    public CursorPolicy getDefaultCursorPolicy() {
        return defaultCursorPolicy;
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
        return views.stream().anyMatch(View::isVisible);
    }

    public boolean hasViews() {
        return !views.isEmpty();
    }

    public void render() {
        if (views.isEmpty()) {
            return;
        }
        StyleManager.pushActive(styleManager);
        try {
            for (View view : views) {
                if (view == null) {
                    continue;
                }
                if (!view.isVisible()) {
                    continue;
                }
                viewSaveManager.prepareView(view);
                Identifier originalKey = styleManager.getGlobalStyleKey();
                StyleDescriptor originalDescriptor = styleManager.getGlobalDescriptor().orElse(null);
                applyViewBaseStyle(view, originalDescriptor);
                Profilers.get().push(view.getClass().getSimpleName());
                StyleDelta delta = view.configureStyleDelta();
                try (StyleScope ignored = delta != null ? StyleScope.push(delta) : null) {
                    view.render();
                    viewSaveManager.captureViewStyle(view);
                } finally {
                    Profilers.get().pop();
                    restoreBaseStyle(originalKey, originalDescriptor);
                }
            }
        } finally {
            StyleManager.popActive(styleManager);
        }
    }

    private void applyViewBaseStyle(View view, StyleDescriptor fallbackDescriptor) {
        Identifier styleKey = view.getStyleKey();
        styleManager.setGlobalStyleKeyTransient(styleKey);
        StyleDescriptor descriptor = fallbackDescriptor != null ? fallbackDescriptor : styleManager.getGlobalDescriptor().orElse(null);
        if (descriptor != null) {
            StyleDescriptor updated = view.configureBaseStyle(descriptor);
            if (updated != null && updated != descriptor) {
                styleManager.setGlobalDescriptor(updated);
            }
        }
        styleManager.apply();
    }

    private void restoreBaseStyle(Identifier originalKey, StyleDescriptor originalDescriptor) {
        styleManager.setGlobalStyleKeyTransient(originalKey);
        if (originalDescriptor != null) {
            styleManager.setGlobalDescriptor(originalDescriptor);
        }
        styleManager.apply();
    }
}
