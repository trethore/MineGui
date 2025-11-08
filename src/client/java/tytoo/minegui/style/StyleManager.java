package tytoo.minegui.style;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiStyle;
import lombok.Getter;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.util.ResourceId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class StyleManager {
    private static final ConcurrentMap<String, StyleManager> INSTANCES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<ResourceId, StyleDescriptor> DESCRIPTOR_REGISTRY = new ConcurrentHashMap<>();
    private static final ThreadLocal<StyleManager> ACTIVE = new ThreadLocal<>();
    private static final CopyOnWriteArrayList<Consumer<StyleDescriptor>> GLOBAL_DESCRIPTOR_READY_LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile StyleDescriptor globalDescriptorSnapshot;

    private final String namespace;
    private final ThreadLocal<Deque<StyleDelta>> styleStack = ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<ImFont> activeFont = new ThreadLocal<>();
    private volatile StyleDescriptor globalDescriptor;
    @Getter
    private volatile ResourceId globalStyleKey;

    private StyleManager(String namespace) {
        this.namespace = namespace;
    }

    public static StyleManager get(String namespace) {
        return INSTANCES.computeIfAbsent(namespace, StyleManager::new);
    }

    public static StyleManager getInstance() {
        return get(GlobalConfigManager.getDefaultNamespace());
    }

    public static void backfillGlobalDescriptors(StyleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        for (StyleManager manager : INSTANCES.values()) {
            if (manager.globalDescriptor == null) {
                manager.setGlobalDescriptor(StyleDescriptor.builder().fromDescriptor(descriptor).build());
            }
        }
    }

    public static void registerDescriptor(ResourceId key, StyleDescriptor descriptor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(descriptor, "descriptor");
        DESCRIPTOR_REGISTRY.put(key, descriptor);
    }

    public static void onGlobalDescriptorReady(Consumer<StyleDescriptor> listener) {
        Objects.requireNonNull(listener, "listener");
        GLOBAL_DESCRIPTOR_READY_LISTENERS.add(listener);
        StyleDescriptor descriptor = globalDescriptorSnapshot;
        if (descriptor == null) {
            descriptor = getInstance().globalDescriptor;
            if (descriptor != null) {
                globalDescriptorSnapshot = descriptor;
            }
        }
        if (descriptor != null) {
            listener.accept(descriptor);
        }
    }

    public static void publishGlobalDescriptor(StyleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        globalDescriptorSnapshot = descriptor;
        for (Consumer<StyleDescriptor> listener : GLOBAL_DESCRIPTOR_READY_LISTENERS) {
            try {
                listener.accept(descriptor);
            } catch (RuntimeException exception) {
                MineGuiCore.LOGGER.error("Failed to notify global style listener", exception);
            }
        }
    }

    public static Optional<StyleDescriptor> descriptor(ResourceId key) {
        if (key == null) {
            return Optional.empty();
        }
        StyleDescriptor descriptor = DESCRIPTOR_REGISTRY.get(key);
        if (descriptor != null) {
            return Optional.of(descriptor);
        }
        return Optional.empty();
    }

    public static Collection<Map.Entry<ResourceId, StyleDescriptor>> descriptors() {
        return Collections.unmodifiableCollection(new ArrayList<>(DESCRIPTOR_REGISTRY.entrySet()));
    }

    public static void resetAllActiveFonts() {
        for (StyleManager manager : INSTANCES.values()) {
            manager.resetActiveFont();
        }
    }

    public static void pushActive(StyleManager manager) {
        ACTIVE.set(manager);
    }

    public static void popActive(StyleManager manager) {
        StyleManager current = ACTIVE.get();
        if (current == manager) {
            ACTIVE.remove();
        }
    }

    static StyleManager current() {
        StyleManager manager = ACTIVE.get();
        if (manager != null) {
            return manager;
        }
        return getInstance();
    }

    public String namespace() {
        return namespace;
    }

    public Optional<StyleDescriptor> getGlobalDescriptor() {
        return Optional.ofNullable(globalDescriptor);
    }

    public void setGlobalDescriptor(StyleDescriptor descriptor) {
        this.globalDescriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    public Optional<StyleDescriptor> getDescriptor(ResourceId key) {
        return descriptor(key);
    }

    public Map<ResourceId, StyleDescriptor> snapshotDescriptors() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(DESCRIPTOR_REGISTRY));
    }

    public void setGlobalStyleKey(ResourceId key) {
        applyStyleKey(key, true);
    }

    public void setGlobalStyleKeyTransient(ResourceId key) {
        applyStyleKey(key, false);
    }

    StyleScope pushRaw(StyleDelta delta) {
        Objects.requireNonNull(delta, "delta");
        styleStack.get().push(delta);
        apply();
        return new StyleScope(delta);
    }

    void pop(StyleDelta expected) {
        Deque<StyleDelta> stack = styleStack.get();
        if (stack.isEmpty()) {
            apply();
            return;
        }
        StyleDelta popped = stack.pop();
        if (popped != expected) {
            stack.clear();
        }
        apply();
    }

    public void apply() {
        ImGuiStyle nativeStyle = ImGui.getStyle();
        StyleDescriptor descriptor = resolveDescriptor();
        ResourceId fontKey = descriptor != null ? descriptor.getFontKey() : null;
        Float fontSize = descriptor != null ? descriptor.getFontSize() : null;
        if (descriptor != null) {
            descriptor.applyTo(nativeStyle);
        }
        Deque<StyleDelta> stack = styleStack.get();
        if (!stack.isEmpty()) {
            for (Iterator<StyleDelta> iterator = stack.descendingIterator(); iterator.hasNext(); ) {
                StyleDelta delta = iterator.next();
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

    public Optional<StyleDescriptor> getEffectiveDescriptor() {
        StyleDescriptor descriptor = resolveDescriptor();
        if (descriptor == null) {
            return Optional.empty();
        }
        StyleDescriptor effective = descriptor;
        Deque<StyleDelta> stack = styleStack.get();
        if (!stack.isEmpty()) {
            for (Iterator<StyleDelta> iterator = stack.descendingIterator(); iterator.hasNext(); ) {
                StyleDelta delta = iterator.next();
                effective = delta.resolve(effective);
            }
        }
        return Optional.of(effective);
    }

    private StyleDescriptor resolveDescriptor() {
        StyleDescriptor descriptor = globalDescriptor;
        ResourceId key = globalStyleKey;
        if (key != null) {
            StyleDescriptor registered = DESCRIPTOR_REGISTRY.get(key);
            if (registered != null) {
                descriptor = registered;
            }
        }
        return descriptor;
    }

    private void applyFont(ResourceId fontKey, Float fontSize) {
        FontLibrary fontLibrary = FontLibrary.getInstance();
        ImFont targetFont = fontLibrary.ensureFont(fontKey, fontSize);
        ImFont currentFont = activeFont.get();
        if (targetFont == null || targetFont == currentFont) {
            return;
        }
        ImGui.getIO().setFontDefault(targetFont);
        activeFont.set(targetFont);
    }

    private void resetActiveFont() {
        activeFont.remove();
    }

    private void persistGlobalStyle(ResourceId key) {
        if (GlobalConfigManager.isConfigIgnored(namespace)) {
            return;
        }
        if (!GlobalConfigManager.shouldSaveFeature(namespace, ConfigFeature.STYLE_REFERENCES)) {
            return;
        }
        GlobalConfig config = GlobalConfigManager.getConfig(namespace);
        String value = key != null ? key.toString() : null;
        if (!Objects.equals(config.getGlobalStyleKey(), value)) {
            config.setGlobalStyleKey(value);
            GlobalConfigManager.save(namespace);
        }
    }

    private void applyStyleKey(ResourceId key, boolean persist) {
        if (Objects.equals(this.globalStyleKey, key)) {
            return;
        }
        this.globalStyleKey = key;
        if (persist) {
            persistGlobalStyle(key);
        }
    }

    public final class StyleScope implements AutoCloseable {
        private final StyleDelta delta;
        private boolean closed;

        private StyleScope(StyleDelta delta) {
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
