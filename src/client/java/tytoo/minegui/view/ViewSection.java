package tytoo.minegui.view;

import java.util.function.Consumer;

@FunctionalInterface
public interface ViewSection {
    static ViewSection of(Runnable runnable) {
        return parent -> {
            if (runnable != null) {
                runnable.run();
            }
        };
    }

    static ViewSection of(Consumer<View> renderer) {
        return parent -> {
            if (renderer == null || parent == null) {
                return;
            }
            renderer.accept(parent);
        };
    }

    void render(View parent);
}
