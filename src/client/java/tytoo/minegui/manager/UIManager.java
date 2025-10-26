package tytoo.minegui.manager;

import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.style.MGStyleDelta;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.style.StyleScope;
import tytoo.minegui.view.MGView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UIManager {
    private static final Map<String, UIManager> INSTANCES = new ConcurrentHashMap<>();

    private final String namespace;
    private final ViewSaveManager viewSaveManager;
    private final StyleManager styleManager;
    private final List<MGView> views = new CopyOnWriteArrayList<>();

    private UIManager(String namespace) {
        this.namespace = namespace;
        this.viewSaveManager = ViewSaveManager.get(namespace);
        this.styleManager = StyleManager.get(namespace);
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

    public void register(MGView view) {
        if (view == null) {
            return;
        }
        if (!views.contains(view)) {
            views.add(view);
            viewSaveManager.register(view);
            view.attach(namespace, viewSaveManager);
        }
    }

    public void unregister(MGView view) {
        if (view == null) {
            return;
        }
        views.remove(view);
        viewSaveManager.unregister(view);
        view.detach();
    }

    public boolean hasVisibleViews() {
        return views.stream().anyMatch(MGView::isVisible);
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
            for (MGView view : views) {
                if (view == null) {
                    continue;
                }
                if (!view.isVisible()) {
                    continue;
                }
                viewSaveManager.prepareView(view);
                Identifier originalKey = styleManager.getGlobalStyleKey();
                MGStyleDescriptor originalDescriptor = styleManager.getGlobalDescriptor().orElse(null);
                applyViewBaseStyle(view, originalDescriptor);
                Profilers.get().push(view.getClass().getSimpleName());
                MGStyleDelta delta = view.configureStyleDelta();
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

    private void applyViewBaseStyle(MGView view, MGStyleDescriptor fallbackDescriptor) {
        Identifier styleKey = view.getStyleKey();
        styleManager.setGlobalStyleKeyTransient(styleKey);
        MGStyleDescriptor descriptor = fallbackDescriptor != null ? fallbackDescriptor : styleManager.getGlobalDescriptor().orElse(null);
        if (descriptor != null) {
            MGStyleDescriptor updated = view.configureBaseStyle(descriptor);
            if (updated != null && updated != descriptor) {
                styleManager.setGlobalDescriptor(updated);
            }
        }
        styleManager.apply();
    }

    private void restoreBaseStyle(Identifier originalKey, MGStyleDescriptor originalDescriptor) {
        styleManager.setGlobalStyleKeyTransient(originalKey);
        if (originalDescriptor != null) {
            styleManager.setGlobalDescriptor(originalDescriptor);
        }
        styleManager.apply();
    }
}
