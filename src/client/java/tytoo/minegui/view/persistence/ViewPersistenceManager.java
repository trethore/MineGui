package tytoo.minegui.view.persistence;

import imgui.ImGui;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.ConfigRegistry;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleJsonSerializer;
import tytoo.minegui.view.View;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public final class ViewPersistenceManager {
    private static final long MIN_LAYOUT_SAVE_INTERVAL_NANOS = 500_000_000L;

    private final String namespace;
    private final NamespaceConfigService configService;
    private final ViewPersistenceAdapter adapter;
    private final ConcurrentMap<View, Entry> entries = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> slugUsage = new ConcurrentHashMap<>();
    private final Set<View> dirtyLayouts = new CopyOnWriteArraySet<>();
    private volatile boolean sharedLayoutLoaded;
    private volatile long lastLayoutFlushNanos;

    public ViewPersistenceManager(String namespace, NamespaceConfigService configService, ViewPersistenceAdapter adapter) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public void register(View view) {
        if (view == null) {
            return;
        }
        ensureEntry(view);
    }

    public void unregister(View view) {
        if (view == null) {
            return;
        }
        Entry entry = entries.remove(view);
        if (entry != null) {
            releaseSlug(entry);
        }
        dirtyLayouts.remove(view);
    }

    public void ensureSharedLayoutLoaded() {
        if (!sharedLayoutLoaded && canLoadLayouts()) {
            adapter.loadSharedLayout(namespace).ifPresent(ImGui::loadIniSettingsFromMemory);
            sharedLayoutLoaded = true;
        }
    }

    public void ensureLoaded(View view) {
        if (view == null || isConfigIgnored()) {
            return;
        }
        Entry entry = ensureEntry(view);
        if (entry == null) {
            return;
        }
        ensureSharedLayoutLoaded();

        if (!entry.layoutLoaded && canLoadLayouts() && view.isPersistentLayout()) {
            adapter.loadLayout(entry.request).ifPresent(ImGui::loadIniSettingsFromMemory);
            entry.layoutLoaded = true;
        }
        if (!entry.styleLoaded && canLoadStyleSnapshots() && view.isPersistentStyle()) {
            adapter.loadStyle(entry.request)
                    .ifPresent(json -> {
                        entry.styleDescriptor = StyleJsonSerializer.fromJson(json).orElse(null);
                        entry.lastStyleJson = json;
                    });
            entry.styleLoaded = true;
        }
    }

    public Optional<StyleDescriptor> styleSnapshot(View view) {
        Entry entry = entries.get(view);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entry.styleDescriptor);
    }

    public void saveLayoutNow(View view) {
        markLayoutDirty(view, true);
        flushLayouts(true);
    }

    public void saveStyleSnapshot(View view, StyleDescriptor descriptor, boolean force) {
        if (view == null || descriptor == null) {
            return;
        }
        if (!force) {
            if (!view.isPersistentStyle()) {
                return;
            }
            if (!canSaveStyleSnapshots()) {
                return;
            }
        }
        Entry entry = ensureEntry(view);
        if (entry == null) {
            return;
        }
        String json = StyleJsonSerializer.toJson(null, descriptor);
        if (!force && json != null && json.equals(entry.lastStyleJson)) {
            return;
        }
        if (json == null) {
            MineGuiCore.LOGGER.warn("Failed to serialize style snapshot for view {}", view.getId());
            return;
        }
        adapter.saveStyle(entry.request, new ViewStyleSnapshot(entry.request, json, false));
        entry.styleDescriptor = descriptor;
        entry.lastStyleJson = json;
    }

    public void deleteStyleSnapshot(View view) {
        if (view == null || !canSaveStyleSnapshots()) {
            return;
        }
        Entry entry = ensureEntry(view);
        if (entry == null) {
            return;
        }
        adapter.saveStyle(entry.request, ViewStyleSnapshot.deleted(entry.request));
        entry.styleDescriptor = null;
        entry.lastStyleJson = null;
    }

    public void markLayoutDirty(View view, boolean force) {
        if (view == null) {
            return;
        }
        if (!force) {
            if (!view.isPersistentLayout()) {
                return;
            }
            if (!canSaveLayouts()) {
                return;
            }
        }
        Entry entry = ensureEntry(view);
        if (entry == null) {
            return;
        }
        dirtyLayouts.add(view);
    }

    public void flushLayouts() {
        flushLayouts(false);
    }

    private void flushLayouts(boolean force) {
        if (dirtyLayouts.isEmpty() || isConfigIgnored() || !canSaveLayouts()) {
            return;
        }
        long now = System.nanoTime();
        if (!force && now - lastLayoutFlushNanos < MIN_LAYOUT_SAVE_INTERVAL_NANOS) {
            return;
        }
        lastLayoutFlushNanos = now;
        String payload = ImGui.saveIniSettingsToMemory();
        Set<View> dirtySnapshot = new HashSet<>(dirtyLayouts);
        Set<View> registeredViews = new HashSet<>(entries.keySet());
        LayoutSplit split = splitLayouts(payload, registeredViews);
        for (Map.Entry<View, StringBuilder> entry : split.perView.entrySet()) {
            if (!dirtySnapshot.contains(entry.getKey())) {
                continue;
            }
            if (entry.getValue().isEmpty()) {
                continue;
            }
            View view = entry.getKey();
            Entry state = entries.get(view);
            if (state == null) {
                continue;
            }
            adapter.saveLayout(state.request, entry.getValue().toString());
        }
        if (!split.shared().isEmpty()) {
            adapter.saveSharedLayout(namespace, split.shared().toString());
        }
        dirtyLayouts.removeAll(dirtySnapshot);
    }

    private LayoutSplit splitLayouts(String payload, Collection<View> knownViews) {
        Map<View, StringBuilder> perView = new ConcurrentHashMap<>();
        StringBuilder shared = new StringBuilder();
        List<View> viewList = new ArrayList<>(knownViews);
        StringBuilder currentBuffer = shared;
        String[] lines = payload.split("\\R");
        for (String line : lines) {
            if (line.startsWith("[")) {
                View matched = matchView(line, viewList);
                currentBuffer = matched != null
                        ? perView.computeIfAbsent(matched, k -> new StringBuilder())
                        : shared;
            }
            currentBuffer.append(line).append('\n');
        }
        return new LayoutSplit(perView, shared);
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

    private Entry ensureEntry(View view) {
        if (view == null) {
            return null;
        }
        Entry existing = entries.get(view);
        if (existing != null && existing.request.viewId().equals(view.getId())) {
            return existing;
        }
        if (existing != null) {
            releaseSlug(existing);
        }
        Entry created = createEntry(view);
        entries.put(view, created);
        return created;
    }

    private void releaseSlug(Entry entry) {
        AtomicInteger counter = slugUsage.get(entry.baseSlug);
        if (counter != null && counter.decrementAndGet() <= 0) {
            slugUsage.remove(entry.baseSlug, counter);
        }
    }

    private Entry createEntry(View view) {
        String viewId = view.getId();
        String base = ViewSlug.fromId(viewId);
        AtomicInteger counter = slugUsage.computeIfAbsent(base, key -> new AtomicInteger(0));
        int value = counter.incrementAndGet();
        String slug = value == 1 ? base : base + "_" + value;
        return new Entry(new ViewPersistenceRequest(namespace, viewId, slug), base);
    }

    private boolean canLoadLayouts() {
        return configService.shouldLoad(ConfigFeature.VIEW_LAYOUTS);
    }

    private boolean canSaveLayouts() {
        return configService.shouldSave(ConfigFeature.VIEW_LAYOUTS);
    }

    private boolean canLoadStyleSnapshots() {
        return configService.shouldLoad(ConfigFeature.VIEW_STYLE_SNAPSHOTS);
    }

    private boolean canSaveStyleSnapshots() {
        return configService.shouldSave(ConfigFeature.VIEW_STYLE_SNAPSHOTS);
    }

    private boolean isConfigIgnored() {
        return configService.isConfigIgnored() || ConfigRegistry.get(namespace).isConfigIgnored();
    }

    private static final class Entry {
        private final ViewPersistenceRequest request;
        private final String baseSlug;
        private volatile boolean layoutLoaded;
        private volatile boolean styleLoaded;
        private volatile StyleDescriptor styleDescriptor;
        private volatile String lastStyleJson;

        private Entry(ViewPersistenceRequest request, String baseSlug) {
            this.request = request;
            this.baseSlug = baseSlug;
        }
    }

    private record LayoutSplit(Map<View, StringBuilder> perView, StringBuilder shared) {
    }
}
