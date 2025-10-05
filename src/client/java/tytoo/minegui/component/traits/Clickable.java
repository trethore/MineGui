package tytoo.minegui.component.traits;

import org.jetbrains.annotations.Nullable;

public interface Clickable<T extends Clickable<T>> {
    @Nullable
    Runnable getOnClick();

    void setOnClick(@Nullable Runnable action);

    default T onClick(Runnable action) {
        setOnClick(action);
        return self();
    }

    default void performClick() {
        Runnable action = getOnClick();
        if (action != null && !isDisabled()) {
            action.run();
        }
    }

    default boolean isDisabled() {
        return false;
    }

    T self();
}
