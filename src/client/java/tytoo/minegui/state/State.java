package tytoo.minegui.state;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class State<T> {
    static final ThreadLocal<Deque<ComputedState<?>>> computationStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    private final List<Consumer<T>> listeners = new ArrayList<>();
    private T value;

    public State(T initialValue) {
        this.value = initialValue;
    }

    public static <T> State<T> of(T initialValue) {
        return new State<>(initialValue);
    }

    public static <T> State<T> computed(Supplier<T> computer) {
        return new ComputedState<>(computer);
    }

    public T get() {
        Deque<ComputedState<?>> stack = computationStack.get();
        if (!stack.isEmpty()) {
            stack.peek().addDependency(this);
        }
        return value;
    }

    public void set(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            this.value = newValue;
            for (Consumer<T> listener : new ArrayList<>(listeners)) {
                listener.accept(newValue);
            }
        }
    }

    public void addListener(Consumer<T> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Consumer<T> listener) {
        listeners.remove(listener);
    }

    public void bind(Consumer<T> setter) {
        addListener(setter);
        setter.accept(this.value);
    }
}