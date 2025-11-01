package tytoo.minegui.style;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiStyle;
import lombok.Getter;
import net.minecraft.util.Identifier;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class StyleManager {
    private static final ConcurrentMap<String, StyleManager> INSTANCES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Identifier, MGStyleDescriptor> DESCRIPTOR_REGISTRY = new ConcurrentHashMap<>();
    private static final ThreadLocal<StyleManager> ACTIVE = new ThreadLocal<>();

    private final String namespace;
    private final ThreadLocal<Deque<MGStyleDelta>> styleStack = ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<ImFont> activeFont = new ThreadLocal<>();
    private volatile MGStyleDescriptor globalDescriptor;
    @Getter
    private volatile Identifier globalStyleKey;

    private StyleManager(String namespace) {
        this.namespace = namespace;
    }

    public static StyleManager get(String namespace) {
        return INSTANCES.computeIfAbsent(namespace, StyleManager::new);
    }

    public static StyleManager getInstance() {
        return get(GlobalConfigManager.getDefaultNamespace());
    }

    public static void backfillGlobalDescriptors(MGStyleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        for (StyleManager manager : INSTANCES.values()) {
            if (manager.globalDescriptor == null) {
                manager.setGlobalDescriptor(MGStyleDescriptor.builder().fromDescriptor(descriptor).build());
            }
        }
    }

    public static void registerDescriptor(Identifier key, MGStyleDescriptor descriptor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(descriptor, "descriptor");
        DESCRIPTOR_REGISTRY.put(key, descriptor);
    }

    public static Optional<MGStyleDescriptor> descriptor(Identifier key) {
        if (key == null) {
            return Optional.empty();
        }
        MGStyleDescriptor descriptor = DESCRIPTOR_REGISTRY.get(key);
        if (descriptor != null) {
            return Optional.of(descriptor);
        }
        return Optional.empty();
    }

    public static Collection<Map.Entry<Identifier, MGStyleDescriptor>> descriptors() {
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

    public Optional<MGStyleDescriptor> getGlobalDescriptor() {
        return Optional.ofNullable(globalDescriptor);
    }

    public void setGlobalDescriptor(MGStyleDescriptor descriptor) {
        this.globalDescriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    public Optional<MGStyleDescriptor> getDescriptor(Identifier key) {
        return descriptor(key);
    }

    public Map<Identifier, MGStyleDescriptor> snapshotDescriptors() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(DESCRIPTOR_REGISTRY));
    }

    public void setGlobalStyleKey(Identifier key) {
        applyStyleKey(key, true);
    }

    public void setGlobalStyleKeyTransient(Identifier key) {
        applyStyleKey(key, false);
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
            for (Iterator<MGStyleDelta> iterator = stack.descendingIterator(); iterator.hasNext(); ) {
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

    public Optional<MGStyleDescriptor> getEffectiveDescriptor() {
        MGStyleDescriptor descriptor = resolveDescriptor();
        if (descriptor == null) {
            return Optional.empty();
        }
        MGStyleDescriptor effective = descriptor;
        Deque<MGStyleDelta> stack = styleStack.get();
        if (!stack.isEmpty()) {
            for (Iterator<MGStyleDelta> iterator = stack.descendingIterator(); iterator.hasNext(); ) {
                MGStyleDelta delta = iterator.next();
                effective = delta.resolve(effective);
            }
        }
        return Optional.of(effective);
    }

    private MGStyleDescriptor resolveDescriptor() {
        MGStyleDescriptor descriptor = globalDescriptor;
        Identifier key = globalStyleKey;
        if (key != null) {
            MGStyleDescriptor registered = DESCRIPTOR_REGISTRY.get(key);
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

    private void resetActiveFont() {
        activeFont.remove();
    }

    private void persistGlobalStyle(Identifier key) {
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

    private void applyStyleKey(Identifier key, boolean persist) {
        if (Objects.equals(this.globalStyleKey, key)) {
            return;
        }
        this.globalStyleKey = key;
        if (persist) {
            persistGlobalStyle(key);
        }
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
