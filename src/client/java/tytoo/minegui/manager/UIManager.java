package tytoo.minegui.manager;

import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.style.MGStyleDelta;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.style.StyleScope;
import tytoo.minegui.view.MGView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UIManager {
    private static final UIManager INSTANCE = new UIManager();

    private final List<MGView> views = new CopyOnWriteArrayList<>();

    private UIManager() {
    }

    public static UIManager getInstance() {
        return INSTANCE;
    }

    public void register(MGView view) {
        if (view == null) {
            return;
        }
        if (!views.contains(view)) {
            views.add(view);
            ViewSaveManager.getInstance().register(view);
        }
    }

    public void unregister(MGView view) {
        views.remove(view);
        ViewSaveManager.getInstance().unregister(view);
    }

    public boolean hasVisibleViews() {
        return views.stream().anyMatch(MGView::isVisible);
    }

    public boolean hasViews() {
        return !views.isEmpty();
    }

    public void render() {
        for (MGView view : views) {
            if (view == null) {
                continue;
            }
            if (!view.isVisible()) {
                continue;
            }
            ViewSaveManager.getInstance().prepareView(view);
            Identifier originalKey = StyleManager.getInstance().getGlobalStyleKey();
            MGStyleDescriptor originalDescriptor = StyleManager.getInstance().getGlobalDescriptor().orElse(null);
            applyViewBaseStyle(view, originalDescriptor);
            Profilers.get().push(view.getClass().getSimpleName());
            MGStyleDelta delta = view.configureStyleDelta();
            try (StyleScope ignored = delta != null ? StyleScope.push(delta) : null) {
                view.render();
                ViewSaveManager.getInstance().captureViewStyle(view);
            } finally {
                Profilers.get().pop();
                restoreBaseStyle(originalKey, originalDescriptor);
            }
        }
    }

    private void applyViewBaseStyle(MGView view, MGStyleDescriptor fallbackDescriptor) {
        StyleManager styleManager = StyleManager.getInstance();
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
        StyleManager styleManager = StyleManager.getInstance();
        styleManager.setGlobalStyleKeyTransient(originalKey);
        if (originalDescriptor != null) {
            styleManager.setGlobalDescriptor(originalDescriptor);
        }
        styleManager.apply();
    }
}
