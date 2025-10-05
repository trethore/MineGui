package tytoo.minegui.component.behavior;

import tytoo.minegui.component.MGComponent;

public interface Behavior<T extends MGComponent<?>> {
    default void onAttach(T component) {
    }

    default void onDetach(T component) {
    }

    default void preRender(T component) {
    }

    default void postRender(T component) {
    }

    default void onUpdate(T component) {
    }
}
