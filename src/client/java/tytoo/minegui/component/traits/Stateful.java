package tytoo.minegui.component.traits;

import org.jetbrains.annotations.Nullable;
import tytoo.minegui.state.State;

public interface Stateful<V, T extends Stateful<V, T>> {
    @Nullable
    State<V> getState();

    void setState(@Nullable State<V> state);

    default T state(State<V> state) {
        setState(state);
        return self();
    }

    T self();
}
