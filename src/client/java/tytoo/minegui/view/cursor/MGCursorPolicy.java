package tytoo.minegui.view.cursor;

import tytoo.minegui.view.MGView;

import java.util.function.Consumer;

public interface MGCursorPolicy {
    static MGCursorPolicy of(Consumer<MGView> onOpen, Consumer<MGView> onClose) {
        Consumer<MGView> openHandler = onOpen != null ? onOpen : view -> {
        };
        Consumer<MGView> closeHandler = onClose != null ? onClose : view -> {
        };
        return new MGCursorPolicy() {
            @Override
            public void onOpen(MGView view) {
                openHandler.accept(view);
            }

            @Override
            public void onClose(MGView view) {
                closeHandler.accept(view);
            }
        };
    }

    default void onOpen(MGView view) {
    }

    default void onClose(MGView view) {
    }
}
