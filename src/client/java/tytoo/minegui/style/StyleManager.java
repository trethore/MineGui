package tytoo.minegui.style;

import imgui.ImFont;
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
    private final ThreadLocal<Deque<MGStyleDelta>> styleStack = ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<ImFont> activeFont = new ThreadLocal<>();
    private volatile MGStyleDescriptor globalDescriptor;
    private volatile Identifier globalStyleKey;

    private StyleManager() {
    }

    public static StyleManager getInstance() {
        return INSTANCE;
    }

    public Optional<MGStyleDescriptor> getGlobalDescriptor() {
        return Optional.ofNullable(globalDescriptor);
    }

    public void setGlobalDescriptor(MGStyleDescriptor descriptor) {
        this.globalDescriptor = Objects.requireNonNull(descriptor, "descriptor");
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

    public Identifier getGlobalStyleKey() {
        return globalStyleKey;
    }

    public void setGlobalStyleKey(Identifier key) {
        this.globalStyleKey = key;
    }

    StyleScope pushRaw(MGStyleDelta delta) {
        Objects.requireNonNull(delta, "delta");
        styleStack.get().push(delta);
        apply();
        return new StyleScope(delta);
    }

    void pop(MGStyleDelta expected) {
        Deque<MGStyleDelta> stack = styleStack.get();
        if (stack.isEmpty()) {
            apply();
            return;
        }
        MGStyleDelta popped = stack.pop();
        if (popped != expected) {
            stack.clear();
        }
        apply();
    }

    public void apply() {
        ImGuiStyle nativeStyle = ImGui.getStyle();
        MGStyleDescriptor descriptor = resolveDescriptor();
        Identifier fontKey = descriptor != null ? descriptor.getFontKey() : null;
        Float fontSize = descriptor != null ? descriptor.getFontSize() : null;
        if (descriptor != null) {
            descriptor.applyTo(nativeStyle);
        }
        Deque<MGStyleDelta> stack = styleStack.get();
        if (!stack.isEmpty()) {
            for (var iterator = stack.descendingIterator(); iterator.hasNext(); ) {
                MGStyleDelta delta = iterator.next();
                delta.applyTo(nativeStyle);
                if (delta.getFontKey() != null) {
                    fontKey = delta.getFontKey();
                }
                if (delta.getFontSize() != null) {
                    fontSize = delta.getFontSize();
                }
            }
        }
        applyFont(fontKey, fontSize);
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

    private void applyFont(Identifier fontKey, Float fontSize) {
        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        ImFont targetFont = fontLibrary.ensureFont(fontKey, fontSize);
        ImFont currentFont = activeFont.get();
        if (targetFont == null || targetFont == currentFont) {
            return;
        }
        ImGui.getIO().setFontDefault(targetFont);
        activeFont.set(targetFont);
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
