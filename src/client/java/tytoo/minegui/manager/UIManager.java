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

    @Deprecated
    public void registerWindow(MGWindow window) {
        autoRegister(window);
    }

    public void autoRegister(MGWindow window) {
        if (window.isTopLevel() && !windows.contains(window)) {
            windows.add(window);
        }
    }

    public void unregister(MGWindow window) {
        windows.remove(window);
    }

    public boolean isAnyWindowVisible() {
        return windows.stream().anyMatch(MGWindow::isVisible);
    }

    public boolean isPointOverWindow(double mouseX, double mouseY) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            MGWindow window = windows.get(i);
            if (window == null || !window.isVisible()) continue;
            if (!window.isTopLevel()) continue;
            if (isPointInWindow(window, mouseX, mouseY)) {
                return true;
            }
            for (MGWindow subWindow : window.getSubWindows()) {
                if (subWindow.isVisible() && isPointInWindow(subWindow, mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPointInWindow(MGWindow window, double mouseX, double mouseY) {
        int x = window.getX();
        int y = window.getY();
        int w = window.getWidth();
        int h = window.getHeight();
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public boolean isAnyWindowFocused() {
        for (MGWindow window : windows) {
            if (window.isFocused()) {
                return true;
            }
            for (MGWindow subWindow : window.getSubWindows()) {
                if (subWindow.isFocused()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void render() {
        for (MGWindow window : windows) {
            Profilers.get().push(window.getTitle() + " " + window.hashCode());
            window.render();
            Profilers.get().pop();
        }
    }
}