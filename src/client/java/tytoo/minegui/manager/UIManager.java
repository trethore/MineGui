package tytoo.minegui.manager;

import net.minecraft.util.profiler.Profilers;
import tytoo.minegui.component.MGComponent;
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

    public boolean isPointOverWindow(double mouseX, double mouseY) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            MGWindow window = windows.get(i);
            if (window == null || !window.isVisible()) continue;
            MGComponent<?> parent = window.getParent();
            if (parent != null) continue;
            int x = window.getX();
            int y = window.getY();
            int w = window.getWidth();
            int h = window.getHeight();
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyWindowFocused() {
        return windows.stream().anyMatch(MGWindow::isFocused);
    }

    public void render() {
        //ImGui.showDemoWindow(); // for testing purposes
        for (MGWindow window : windows) {
            Profilers.get().push(window.getTitle() + " " + window.hashCode());
            window.render();
            Profilers.get().pop();
        }
    }
}