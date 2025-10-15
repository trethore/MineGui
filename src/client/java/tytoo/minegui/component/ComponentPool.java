package tytoo.minegui.component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ComponentPool<T extends MGComponent<?>> {
    private final Supplier<T> factory;
    private final Consumer<T> onBorrow;
    private final Consumer<T> onRecycle;
    private final ThreadLocal<Deque<T>> localPool = ThreadLocal.withInitial(ArrayDeque::new);

    public ComponentPool(Supplier<T> factory, Consumer<T> onBorrow, Consumer<T> onRecycle) {
        this.factory = factory;
        this.onBorrow = onBorrow;
        this.onRecycle = onRecycle;
    }

    public ComponentPool(Supplier<T> factory, Consumer<T> onBorrow) {
        this(factory, onBorrow, null);
    }

    public ComponentPool(Supplier<T> factory) {
        this(factory, null, null);
    }

    public T acquire() {
        Deque<T> pool = localPool.get();
        T instance = pool.pollFirst();
        if (instance == null) {
            instance = factory.get();
        }
        instance.resetRootState();
        if (onBorrow != null) {
            onBorrow.accept(instance);
        }
        T finalInstance = instance;
        instance.setAfterRenderHook(() -> recycle(finalInstance));
        return instance;
    }

    private void recycle(T instance) {
        if (onRecycle != null) {
            onRecycle.accept(instance);
        }
        localPool.get().addFirst(instance);
    }
}
