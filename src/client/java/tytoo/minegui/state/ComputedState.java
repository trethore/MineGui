package tytoo.minegui.state;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class ComputedState<T> extends State<T> {
    private final Supplier<T> computer;
    private final Set<State<?>> dependencies = new HashSet<>();
    private final Map<State<?>, Consumer<?>> depListeners = new IdentityHashMap<>();

    ComputedState(Supplier<T> computer) {
        super(null);
        this.computer = computer;
        recompute();
    }

    private static <U> void removeListenerTyped(State<U> state, Consumer<?> raw) {
        @SuppressWarnings("unchecked")
        Consumer<U> typed = (Consumer<U>) raw;
        state.removeListener(typed);
    }

    private void onDependencyChanged(Object ignored) {
        recompute();
    }

    private void recompute() {
        for (State<?> oldDep : dependencies) {
            Consumer<?> listener = depListeners.remove(oldDep);
            if (listener != null) {
                removeListenerTyped(oldDep, listener);
            }
        }
        dependencies.clear();

        State.computationStack.get().push(this);
        try {
            T newValue = this.computer.get();
            super.set(newValue);
        } finally {
            State.computationStack.get().pop();
        }
    }

    <U> void addDependency(State<U> state) {
        if (dependencies.add(state)) {
            Consumer<U> listener = this::onDependencyChanged;
            depListeners.put(state, listener);
            state.addListener(listener);
        }
    }

    @Override
    public void set(T value) {
        throw new UnsupportedOperationException("Cannot set the value of a computed state directly.");
    }
}
