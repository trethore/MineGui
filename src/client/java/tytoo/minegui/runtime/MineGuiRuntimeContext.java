package tytoo.minegui.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import imgui.ImGui;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.MineGuiOptions;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.config.PersistenceFlags;
import tytoo.minegui.imgui.dock.DockspaceCustomizer;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class MineGuiRuntimeContext implements MineGuiContext {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long MIN_LAYOUT_SAVE_INTERVAL_NANOS = 500_000_000L;

    private final MineGuiOptions options;
    private final Path namespaceRoot;
    private final UIManager uiManager;
    private final StyleManager styleManager;
    private final List<MineGuiLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();
    private final Map<View, ViewEntry> viewEntries = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> slugUsage = new ConcurrentHashMap<>();
    private final Set<View> dirtyLayouts = ConcurrentHashMap.newKeySet();

    private volatile NamespaceConfig config;
    private volatile ResourceId cursorPolicyId;
    private volatile CursorPolicy cursorPolicy;
    private volatile DockspaceCustomizer dockspaceCustomizer;
    private volatile long lastLayoutFlushNanos;

    public MineGuiRuntimeContext(MineGuiOptions options) {
        this.options = Objects.requireNonNull(options, "options");
        this.namespaceRoot = options.namespaceRoot();
        this.uiManager = UIManager.get(options.namespace());
        this.styleManager = StyleManager.get(options.namespace());

        if (options.persistence().config()) {
            this.config = loadConfigFromDisk();
        } else {
            this.config = NamespaceConfig.defaults(options.namespace());
        }

        setCursorPolicy(options.defaultCursorPolicy());
        setDockspaceCustomizer(options.dockspaceCustomizer());
    }

    @Override
    public String namespace() {
        return options.namespace();
    }

    @Override
    public MineGuiOptions options() {
        return options;
    }

    @Override
    public NamespaceConfig config() {
        return config;
    }

    @Override
    public void updateConfig(UnaryOperator<NamespaceConfig> updater) {
        Objects.requireNonNull(updater, "updater");
        NamespaceConfig next = updater.apply(config);
        if (next == null) {
            throw new IllegalArgumentException("Config updater returned null");
        }
        if (!options.namespace().equals(next.namespace())) {
            throw new IllegalArgumentException("Cannot change namespace via config update");
        }
        this.config = next;
    }

    @Override
    public PersistenceFlags persistence() {
        return options.persistence();
    }

    @Override
    public void save() {
        PersistenceFlags flags = options.persistence();
        if (flags.config()) {
            saveConfigToDisk();
        }
        if (flags.layouts()) {
            flushLayouts(true);
        }
    }

    @Override
    public void load() {
        PersistenceFlags flags = options.persistence();
        if (flags.config()) {
            this.config = loadConfigFromDisk();
        }
    }

    @Override
    public UIManager ui() {
        return uiManager;
    }

    @Override
    public StyleManager style() {
        return styleManager;
    }

    @Override
    public ResourceId cursorPolicyId() {
        return cursorPolicyId;
    }

    @Override
    public CursorPolicy cursorPolicy() {
        return cursorPolicy;
    }

    @Override
    public void setCursorPolicy(ResourceId policyId) {
        ResourceId normalized = policyId != null ? policyId : CursorPolicies.emptyId();
        CursorPolicy resolved = CursorPolicyRegistry.resolvePolicyOrDefault(normalized, CursorPolicies.empty());
        if (Objects.equals(normalized, cursorPolicyId) && resolved == cursorPolicy) {
            return;
        }
        this.cursorPolicyId = normalized;
        this.cursorPolicy = resolved;
        this.uiManager.setDefaultCursorPolicy(resolved);
    }

    @Override
    public DockspaceCustomizer dockspaceCustomizer() {
        return dockspaceCustomizer;
    }

    @Override
    public void setDockspaceCustomizer(DockspaceCustomizer customizer) {
        this.dockspaceCustomizer = customizer != null ? customizer : DockspaceCustomizer.noop();
    }

    @Override
    public void addLifecycleListener(MineGuiLifecycleListener listener) {
        if (listener != null && !lifecycleListeners.contains(listener)) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(MineGuiLifecycleListener listener) {
        if (listener != null) {
            lifecycleListeners.remove(listener);
        }
    }

    public void registerView(View view) {
        if (view == null) {
            return;
        }
        ensureViewEntry(view);
    }

    public void unregisterView(View view) {
        if (view == null) {
            return;
        }
        ViewEntry entry = viewEntries.remove(view);
        if (entry != null) {
            releaseSlug(entry);
        }
        dirtyLayouts.remove(view);
    }

    public void ensureViewLoaded(View view) {
        if (view == null || !options.persistence().layouts()) {
            return;
        }
        ViewEntry entry = ensureViewEntry(view);
        if (entry == null) {
            return;
        }
        if (!entry.layoutLoaded && view.shouldPersistLayout(options.persistence())) {
            Path layoutPath = viewLayoutPath(entry.slug);
            readString(layoutPath).ifPresent(ImGui::loadIniSettingsFromMemory);
            entry.layoutLoaded = true;
        }
    }

    public void markLayoutDirty(View view) {
        if (view == null) {
            return;
        }
        if (!view.shouldPersistLayout(options.persistence())) {
            return;
        }
        ViewEntry entry = ensureViewEntry(view);
        if (entry != null) {
            dirtyLayouts.add(view);
        }
    }

    public void flushLayouts() {
        flushLayouts(false);
    }

    public void fireContextReady() {
        notifyLifecycle("context_ready", listener -> listener.onContextReady(this));
    }

    public void firePreRender() {
        notifyLifecycle("pre_render", listener -> listener.onPreRender(this));
    }

    public void firePostRender() {
        notifyLifecycle("post_render", listener -> listener.onPostRender(this));
    }

    public void fireShutdown() {
        notifyLifecycle("shutdown", listener -> listener.onShutdown(this));
    }

    private void flushLayouts(boolean force) {
        if (dirtyLayouts.isEmpty() || !options.persistence().layouts()) {
            return;
        }
        long now = System.nanoTime();
        if (!force && now - lastLayoutFlushNanos < MIN_LAYOUT_SAVE_INTERVAL_NANOS) {
            return;
        }
        lastLayoutFlushNanos = now;
        String payload = ImGui.saveIniSettingsToMemory();
        Set<View> snapshot = new HashSet<>(dirtyLayouts);
        Set<View> registered = new HashSet<>(viewEntries.keySet());
        Map<View, StringBuilder> perView = splitLayouts(payload, registered);

        for (Map.Entry<View, StringBuilder> entry : perView.entrySet()) {
            View view = entry.getKey();
            if (!snapshot.contains(view)) {
                continue;
            }
            if (entry.getValue().isEmpty()) {
                continue;
            }
            ViewEntry state = viewEntries.get(view);
            if (state == null) {
                continue;
            }
            Path layoutPath = viewLayoutPath(state.slug);
            writeString(layoutPath, entry.getValue().toString());
        }
        dirtyLayouts.removeAll(snapshot);
    }

    private Map<View, StringBuilder> splitLayouts(String payload, Collection<View> knownViews) {
        Map<View, StringBuilder> perView = new HashMap<>();
        List<View> viewList = new ArrayList<>(knownViews);
        StringBuilder currentBuffer = null;
        String[] lines = payload.split("\\R");
        for (String line : lines) {
            if (line.startsWith("[")) {
                View matched = matchView(line, viewList);
                currentBuffer = matched != null
                        ? perView.computeIfAbsent(matched, k -> new StringBuilder())
                        : null;
            }
            if (currentBuffer != null) {
                currentBuffer.append(line).append('\n');
            }
        }
        return perView;
    }

    private View matchView(String header, List<View> views) {
        if (header == null || !header.startsWith("[Window][")) {
            return null;
        }
        int start = header.indexOf("][");
        int end = header.lastIndexOf(']');
        if (start < 0 || end <= start + 2) {
            return null;
        }
        String windowName = header.substring(start + 2, end);
        for (View view : views) {
            if (windowName.endsWith("##" + view.getId())) {
                return view;
            }
        }
        return null;
    }

    private ViewEntry ensureViewEntry(View view) {
        if (view == null) {
            return null;
        }
        ViewEntry existing = viewEntries.get(view);
        if (existing != null && existing.viewId.equals(view.getId())) {
            return existing;
        }
        if (existing != null) {
            releaseSlug(existing);
        }
        ViewEntry created = createViewEntry(view);
        viewEntries.put(view, created);
        return created;
    }

    private void releaseSlug(ViewEntry entry) {
        AtomicInteger counter = slugUsage.get(entry.baseSlug);
        if (counter != null && counter.decrementAndGet() <= 0) {
            slugUsage.remove(entry.baseSlug, counter);
        }
    }

    private ViewEntry createViewEntry(View view) {
        String viewId = view.getId();
        String base = toSlug(viewId);
        AtomicInteger counter = slugUsage.computeIfAbsent(base, key -> new AtomicInteger(0));
        int value = counter.incrementAndGet();
        String slug = value == 1 ? base : base + "_" + value;
        return new ViewEntry(viewId, slug, base);
    }

    private String toSlug(String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return "view";
        }
        return viewId.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private Path viewLayoutPath(String slug) {
        return namespaceRoot.resolve("views").resolve(slug + ".ini");
    }

    private NamespaceConfig loadConfigFromDisk() {
        Path configPath = namespaceRoot.resolve("config.json");
        if (!Files.exists(configPath)) {
            return NamespaceConfig.defaults(options.namespace());
        }
        try {
            String json = Files.readString(configPath, StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            boolean viewport = !obj.has("viewport") || obj.get("viewport").getAsBoolean();
            boolean dockspace = !obj.has("dockspace") || obj.get("dockspace").getAsBoolean();
            float scale = obj.has("globalScale") ? obj.get("globalScale").getAsFloat() : 1.0f;
            ResourceId styleKey = null;
            if (obj.has("globalStyleKey") && !obj.get("globalStyleKey").isJsonNull()) {
                styleKey = ResourceId.tryParse(obj.get("globalStyleKey").getAsString());
            }
            return new NamespaceConfig(options.namespace(), viewport, dockspace, scale, styleKey);
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to load config from {}", configPath, e);
            return NamespaceConfig.defaults(options.namespace());
        }
    }

    private void saveConfigToDisk() {
        Path configPath = namespaceRoot.resolve("config.json");
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject obj = new JsonObject();
            obj.addProperty("viewport", config.viewportEnabled());
            obj.addProperty("dockspace", config.dockspaceEnabled());
            obj.addProperty("globalScale", config.globalScale());
            if (config.globalStyleKey() != null) {
                obj.addProperty("globalStyleKey", config.globalStyleKey().toString());
            }
            String json = GSON.toJson(obj);
            Files.writeString(configPath, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to save config to {}", configPath, e);
        }
    }

    private Optional<String> readString(Path path) {
        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to read file {}", path, e);
            return Optional.empty();
        }
    }

    private void writeString(Path path, String content) {
        if (path == null || content == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to write file {}", path, e);
        }
    }

    private void notifyLifecycle(String phase, Consumer<MineGuiLifecycleListener> action) {
        for (MineGuiLifecycleListener listener : lifecycleListeners) {
            try {
                action.accept(listener);
            } catch (RuntimeException exception) {
                MineGuiCore.LOGGER.error("MineGui lifecycle listener '{}' failed for namespace '{}'",
                        phase, options.namespace(), exception);
            }
        }
    }

    private static final class ViewEntry {
        private final String viewId;
        private final String slug;
        private final String baseSlug;
        private volatile boolean layoutLoaded;

        private ViewEntry(String viewId, String slug, String baseSlug) {
            this.viewId = viewId;
            this.slug = slug;
            this.baseSlug = baseSlug;
        }
    }

}
