package tytoo.minegui.manager;

import net.minecraft.util.profiler.Profilers;
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
            Profilers.get().push(view.getClass().getSimpleName());
            try {
                view.render();
            } finally {
                Profilers.get().pop();
            }
        }
    }
}
