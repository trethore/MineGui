package tytoo.minegui.manager;

import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentRegistry {
    private static final ComponentRegistry INSTANCE = new ComponentRegistry();

    private final Map<String, Supplier<? extends MGComponent<?>>> factories = new HashMap<>();

    private ComponentRegistry() {
    }

    public static ComponentRegistry getInstance() {
        return INSTANCE;
    }

    public <T extends MGComponent<?>> void register(String id, Supplier<T> factory) {
        if (id == null || factory == null) {
            throw new IllegalArgumentException("Component ID and factory cannot be null");
        }
        factories.put(id, factory);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends MGComponent<?>> T create(String id) {
        Supplier<? extends MGComponent<?>> factory = factories.get(id);
        if (factory == null) {
            return null;
        }
        return (T) factory.get();
    }

    public boolean isRegistered(String id) {
        return factories.containsKey(id);
    }

    public void unregister(String id) {
        factories.remove(id);
    }
}
