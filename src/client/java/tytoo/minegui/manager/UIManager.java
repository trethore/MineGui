package tytoo.minegui.manager;

import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.component.components.layout.MGWindow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UIManager {
    private static final UIManager INSTANCE = new UIManager();

    private final List<MGWindow> windows = new CopyOnWriteArrayList<>();

    private UIManager() {
    }

    public static UIManager getInstance() {
        return INSTANCE;
    }

    public void registerWindow(MGWindow window) {
        windows.add(window);
    }

    public boolean isAnyWindowVisible() {
        return windows.stream().anyMatch(MGWindow::isVisible);
    }

    public void render() {
        for (MGWindow window : windows) {
            Profilers.get().push(window.getTitle() + " " + window.hashCode());
            window.render();
            Profilers.get().pop();
        }
    }
}