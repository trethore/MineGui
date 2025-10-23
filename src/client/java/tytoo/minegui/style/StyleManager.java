package tytoo.minegui.style;

import imgui.ImGui;
import imgui.ImGuiStyle;
import net.minecraft.util.Identifier;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class StyleManager {
    private static final StyleManager INSTANCE = new StyleManager();

    private final ConcurrentMap<Identifier, MGStyleDescriptor> descriptorRegistry = new ConcurrentHashMap<>();
    private volatile MGStyleDescriptor globalDescriptor;
    private volatile Identifier globalStyleKey;
    private final ThreadLocal<Deque<MGStyleDelta>> styleStack = ThreadLocal.withInitial(ArrayDeque::new);

    private StyleManager() {
    }

    public static StyleManager getInstance() {
        return INSTANCE;
    }

    public void setGlobalDescriptor(MGStyleDescriptor descriptor) {
        this.globalDescriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    public Optional<MGStyleDescriptor> getGlobalDescriptor() {
        return Optional.ofNullable(globalDescriptor);
    }

    public void registerDescriptor(Identifier key, MGStyleDescriptor descriptor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(descriptor, "descriptor");
        descriptorRegistry.put(key, descriptor);
    }

    public Optional<MGStyleDescriptor> getDescriptor(Identifier key) {
        if (key == null) {
            return Optional.empty();
        }
        MGStyleDescriptor descriptor = descriptorRegistry.get(key);
        if (descriptor != null) {
            return Optional.of(descriptor);
        }
        return Optional.empty();
    }

    public void setGlobalStyleKey(Identifier key) {
        this.globalStyleKey = key;
    }

    public Identifier getGlobalStyleKey() {
        return globalStyleKey;
    }

    public StyleScope push(MGStyleDelta delta) {
        Objects.requireNonNull(delta, "delta");
        styleStack.get().push(delta);
        return new StyleScope(delta);
    }

    public void pop(MGStyleDelta expected) {
        Deque<MGStyleDelta> stack = styleStack.get();
        if (stack.isEmpty()) {
            return;
        }
        MGStyleDelta popped = stack.pop();
        if (popped != expected) {
            stack.clear();
        }
    }

    public void apply() {
        ImGuiStyle nativeStyle = ImGui.getStyle();
        MGStyleDescriptor descriptor = resolveDescriptor();
        if (descriptor != null) {
            descriptor.applyTo(nativeStyle);
        }
        Deque<MGStyleDelta> stack = styleStack.get();
        if (stack.isEmpty()) {
            return;
        }
        for (var iterator = stack.descendingIterator(); iterator.hasNext(); ) {
            MGStyleDelta delta = iterator.next();
            delta.applyTo(nativeStyle);
        }
    }

    private MGStyleDescriptor resolveDescriptor() {
        MGStyleDescriptor descriptor = globalDescriptor;
        Identifier key = globalStyleKey;
        if (key != null) {
            MGStyleDescriptor registered = descriptorRegistry.get(key);
            if (registered != null) {
                descriptor = registered;
            }
        }
        return descriptor;
    }

    public final class StyleScope implements AutoCloseable {
        private final MGStyleDelta delta;
        private boolean closed;

        private StyleScope(MGStyleDelta delta) {
            this.delta = delta;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            pop(delta);
        }
    }
}
