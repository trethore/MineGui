package tytoo.minegui.view;

import java.util.function.Consumer;

@FunctionalInterface
public interface ViewSection {
    static ViewSection of(Runnable runnable) {
        return view -> runnable.run();
    }

    static ViewSection of(Consumer<View> renderer) {
        return renderer::accept;
    }

    void render(View parent);
}
