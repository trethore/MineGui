package tytoo.minegui.view;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.layout.LayoutApi;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface ViewSection {
    static ViewSection of(Runnable runnable) {
        return (parent, layout) -> {
            if (runnable != null) {
                runnable.run();
            }
        };
    }

    static ViewSection of(Consumer<View> renderer) {
        return (parent, layout) -> {
            if (renderer == null || parent == null) {
                return;
            }
            renderer.accept(parent);
        };
    }

    static ViewSection ofLayout(BiConsumer<View, LayoutApi> renderer) {
        return (parent, layout) -> {
            if (renderer == null) {
                return;
            }
            LayoutApi resolved = layout != null ? layout : (parent != null ? parent.layout() : null);
            if (resolved == null) {
                MineGuiCore.LOGGER.warn(
                        "ViewSection '{}' skipped because layout API was unavailable for parent '{}'.",
                        ViewSection.class.getName(),
                        parent != null ? parent.getClass().getName() : "<null>"
                );
                return;
            }
            renderer.accept(parent, resolved);
        };
    }

    void render(View parent, LayoutApi layout);
}
