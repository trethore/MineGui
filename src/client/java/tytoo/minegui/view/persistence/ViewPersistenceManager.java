package tytoo.minegui.view.persistence;

import imgui.ImGui;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.GlobalConfigManager;
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
        entries.computeIfAbsent(view, this::createEntry);
    }

    public void unregister(View view) {
        if (view == null) {
            return;
        }
        Entry entry = entries.remove(view);
        if (entry != null) {
            AtomicInteger counter = slugUsage.get(entry.baseSlug);
            if (counter != null && counter.decrementAndGet() <= 0) {
                slugUsage.remove(entry.baseSlug, counter);
            }
        }
        dirtyLayouts.remove(view);
    }

    public void ensureLoaded(View view) {
        if (view == null || isConfigIgnored()) {
            return;
        }
        Entry entry = entries.get(view);
        if (entry == null) {
            entry = createEntry(view);
            entries.put(view, entry);
        }
        if (!sharedLayoutLoaded && canLoadLayouts()) {
            adapter.loadSharedLayout(namespace).ifPresent(ImGui::loadIniSettingsFromMemory);
            sharedLayoutLoaded = true;
        }
        if (!entry.layoutLoaded && canLoadLayouts() && view.isPersistentLayout()) {
            adapter.loadLayout(entry.request).ifPresent(ImGui::loadIniSettingsFromMemory);
            entry.layoutLoaded = true;
        }
        if (!entry.styleLoaded && canLoadStyleSnapshots() && view.isPersistentStyle()) {
            Entry target = entry;
            adapter.loadStyle(entry.request)
                    .ifPresent(json -> {
                        target.styleDescriptor = StyleJsonSerializer.fromJson(json).orElse(null);
                        target.lastStyleJson = json;
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
        Entry entry = entries.get(view);
        if (entry == null) {
            entry = createEntry(view);
            entries.put(view, entry);
        }
        String json = StyleJsonSerializer.toJson(namespace, view.getId(), null, descriptor);
        if (!force && json != null && json.equals(entry.lastStyleJson)) {
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
        Entry entry = entries.get(view);
        if (entry == null) {
            return;
        }
        adapter.saveStyle(entry.request, ViewStyleSnapshot.deleted(entry.request));
        entry.styleDescriptor = null;
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
        Entry entry = entries.get(view);
        if (entry == null) {
            entry = createEntry(view);
            entries.put(view, entry);
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
        LayoutSplit split = splitLayouts(payload, dirtyLayouts);
        for (Map.Entry<View, StringBuilder> entry : split.perView.entrySet()) {
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
        dirtyLayouts.clear();
    }

    private LayoutSplit splitLayouts(String payload, Collection<View> targetViews) {
        Map<View, StringBuilder> perView = new ConcurrentHashMap<>();
        StringBuilder shared = new StringBuilder();
        List<View> viewList = new ArrayList<>(targetViews);
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
            if (windowName.contains("##" + view.getId())) {
                return view;
            }
        }
        return null;
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
        return configService.isConfigIgnored() || GlobalConfigManager.isConfigIgnored(namespace);
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
