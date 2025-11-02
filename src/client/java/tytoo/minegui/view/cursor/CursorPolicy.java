package tytoo.minegui.view.cursor;

import tytoo.minegui.view.View;

import java.util.function.Consumer;

public interface CursorPolicy {
    static CursorPolicy of(Consumer<View> onOpen, Consumer<View> onClose) {
        Consumer<View> openHandler = onOpen != null ? onOpen : view -> {
        };
        Consumer<View> closeHandler = onClose != null ? onClose : view -> {
        };
        return new CursorPolicy() {
            @Override
            public void onOpen(View view) {
                openHandler.accept(view);
            }

            @Override
            public void onClose(View view) {
                closeHandler.accept(view);
            }
        };
    }

    default void onOpen(View view) {
    }

    default void onClose(View view) {
    }
}
