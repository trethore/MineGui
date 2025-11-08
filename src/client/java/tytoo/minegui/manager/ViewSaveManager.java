package tytoo.minegui.manager;

import imgui.ImGui;
import imgui.ImGuiIO;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.ConfigFeature;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.persistence.FileViewPersistenceAdapter;
import tytoo.minegui.persistence.ViewPersistenceAdapter;
import tytoo.minegui.persistence.ViewPersistenceRequest;
import tytoo.minegui.persistence.ViewStyleSnapshot;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.runtime.config.NamespaceConfigService;
import tytoo.minegui.style.StyleJsonSerializer;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ViewSaveManager {
    private static final Map<String, ViewSaveManager> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<String, ViewPersistenceAdapter> ADAPTERS = new ConcurrentHashMap<>();
    private static final ViewPersistenceAdapter DEFAULT_ADAPTER = new FileViewPersistenceAdapter();
    private static final Pattern WINDOW_HEADER_PATTERN = Pattern.compile("^\\[[^]]+]\\[(?<name>[^]]+)]$");

    private final String namespace;
    private final Map<View, ViewEntry> entries = new ConcurrentHashMap<>();
    private final StyleManager styleManager;
    private volatile ViewPersistenceAdapter adapter;
    private volatile boolean forceSave;

    private ViewSaveManager(String namespace) {
        this.namespace = namespace;
        this.adapter = resolveAdapter(namespace);
        this.styleManager = StyleManager.get(namespace);
    }

    public static ViewSaveManager get(String namespace) {
        return INSTANCES.computeIfAbsent(namespace, ViewSaveManager::new);
    }

    public static ViewSaveManager getInstance() {
        return get(GlobalConfigManager.getDefaultNamespace());
    }

    public static void setAdapter(String namespace, ViewPersistenceAdapter adapter) {
        String normalized = normalizeNamespace(namespace);
        if (adapter == null) {
            ADAPTERS.remove(normalized);
        } else {
            ADAPTERS.put(normalized, adapter);
        }
        ViewSaveManager instance = INSTANCES.get(normalized);
        if (instance != null) {
            instance.updateAdapter(adapter);
        }
    }

    public static ViewPersistenceAdapter getAdapter(String namespace) {
        return ADAPTERS.get(normalizeNamespace(namespace));
    }

    private static ViewPersistenceAdapter resolveAdapter(String namespace) {
        ViewPersistenceAdapter adapter = ADAPTERS.get(normalizeNamespace(namespace));
        return adapter != null ? adapter : DEFAULT_ADAPTER;
    }

    private static String normalizeNamespace(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        String trimmed = namespace.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("namespace cannot be blank");
        }
        return trimmed;
    }

    public String namespace() {
        return namespace;
    }

    public void register(View view) {
        if (view == null) {
            return;
        }
        entries.computeIfAbsent(view, unused -> new ViewEntry());
    }

    public void unregister(View view) {
        if (view == null) {
            return;
        }
        entries.remove(view);
    }

    public void prepareView(View view) {
        if (view == null) {
            return;
        }
        if (!view.isPersistent()) {
            entries.remove(view);
            return;
        }
        ViewEntry entry = entries.computeIfAbsent(view, unused -> new ViewEntry());
        NamespaceConfigService configService = configService();
        boolean loadLayouts = configService != null
                ? configService.shouldLoad(ConfigFeature.VIEW_LAYOUTS)
                : GlobalConfigManager.shouldLoadFeature(namespace, ConfigFeature.VIEW_LAYOUTS);
        boolean loadStyleSnapshots = configService != null
                ? configService.shouldLoad(ConfigFeature.VIEW_STYLE_SNAPSHOTS)
                : GlobalConfigManager.shouldLoadFeature(namespace, ConfigFeature.VIEW_STYLE_SNAPSHOTS);
        String currentId = view.getId();
        String scopedId = scopedId(view);
        ViewPersistenceRequest request = new ViewPersistenceRequest(namespace, persistenceViewId(currentId), scopedId);
        if (!Objects.equals(entry.loadedId, currentId) || !Objects.equals(entry.loadedScopedId, scopedId)) {
            entry.loaded = false;
            entry.persistedStyleSnapshotJson = loadStyleSnapshots ? adapter.loadStyleSnapshot(request).orElse(null) : null;
            entry.styleSnapshotJson = null;
            entry.descriptorDirty = false;
            entry.loadedScopedId = null;
        }
        entry.request = request;
        if (entry.loaded) {
            return;
        }
        entry.loadedId = currentId;
        entry.loadedScopedId = scopedId;
        if (loadLayouts) {
            adapter.loadLayout(request).ifPresent(ImGui::loadIniSettingsFromMemory);
        }
        entry.loaded = true;
        restoreViewStyle(view, entry);
    }

    public void requestSave() {
        forceSave = true;
    }

    public void flush() {
        persistViewStyles();
        persistStyleDescriptors();
        forceSave = false;
    }

    public int exportStyles(boolean forceRewrite) {
        if (forceRewrite) {
            for (Map.Entry<View, ViewEntry> entry : entries.entrySet()) {
                View view = entry.getKey();
                ViewEntry state = entry.getValue();
                if (view == null || state == null || !view.isPersistent()) {
                    continue;
                }
                if (state.styleSnapshotJson != null) {
                    state.descriptorDirty = true;
                }
            }
        }
        return persistStyleDescriptors();
    }

    public void onFrameRendered() {
        if (entries.isEmpty()) {
            persistViewStyles();
            persistStyleDescriptors();
            forceSave = false;
            return;
        }
        ImGuiIO io = ImGui.getIO();
        boolean shouldPersistIni = forceSave || io.getWantSaveIniSettings();
        if (shouldPersistIni) {
            String iniContent = ImGui.saveIniSettingsToMemory();
            persistEntries(iniContent);
            forceSave = false;
        }
        persistViewStyles();
        persistStyleDescriptors();
    }

    private void persistEntries(String iniContent) {
        if (iniContent == null || iniContent.isEmpty()) {
            return;
        }
        NamespaceConfigService configService = configService();
        boolean shouldSaveLayouts = configService != null
                ? configService.shouldSave(ConfigFeature.VIEW_LAYOUTS)
                : GlobalConfigManager.shouldSaveFeature(namespace, ConfigFeature.VIEW_LAYOUTS);
        if (!shouldSaveLayouts) {
            return;
        }
        Map<String, Map.Entry<View, ViewEntry>> activeEntries = entries.entrySet().stream()
                .filter(entry -> entry.getKey().isPersistent())
                .collect(Collectors.toMap(entry -> scopedId(entry.getKey()), entry -> entry, (first, second) -> first));
        if (activeEntries.isEmpty()) {
            return;
        }
        Map<String, StringBuilder> bufferedSections = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(iniContent))) {
            String line;
            StringBuilder sectionBuffer = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    String identifiedId = extractViewId(line);
                    if (identifiedId != null && activeEntries.containsKey(identifiedId)) {
                        sectionBuffer = bufferedSections.computeIfAbsent(identifiedId, key -> new StringBuilder());
                        if (!sectionBuffer.isEmpty() && sectionBuffer.charAt(sectionBuffer.length() - 1) != '\n') {
                            sectionBuffer.append('\n');
                        }
                        sectionBuffer.append(line).append('\n');
                    } else {
                        sectionBuffer = null;
                    }
                    continue;
                }
                if (sectionBuffer != null) {
                    sectionBuffer.append(line).append('\n');
                }
            }
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to parse ImGui ini data for {}", namespace, e);
            return;
        }
        for (Map.Entry<String, StringBuilder> entry : bufferedSections.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            Map.Entry<View, ViewEntry> active = activeEntries.get(entry.getKey());
            if (active == null) {
                continue;
            }
            View view = active.getKey();
            ViewEntry viewEntry = active.getValue();
            ViewPersistenceRequest request = ensureRequest(view, viewEntry);
            adapter.saveLayout(request, entry.getValue().toString());
        }
    }

    public void captureViewStyle(View view) {
        if (view == null || !view.isPersistent()) {
            return;
        }
        ViewEntry entry = entries.get(view);
        if (entry == null) {
            return;
        }
        String currentKey = normalizeStyleKey(view.getStyleKey());
        if (!Objects.equals(entry.pendingStyleKey, currentKey)) {
            entry.pendingStyleKey = currentKey;
            entry.styleDirty = true;
        }
        styleManager.getEffectiveDescriptor().ifPresentOrElse(descriptor -> {
            String snapshot = StyleJsonSerializer.toJson(namespace, view.getId(), view.getStyleKey(), descriptor);
            if (!Objects.equals(entry.styleSnapshotJson, snapshot)) {
                entry.styleSnapshotJson = snapshot;
                entry.descriptorDirty = !Objects.equals(snapshot, entry.persistedStyleSnapshotJson);
            }
        }, () -> {
            if (entry.styleSnapshotJson != null) {
                entry.styleSnapshotJson = null;
                entry.descriptorDirty = entry.persistedStyleSnapshotJson != null;
            }
        });
    }

    private void restoreViewStyle(View view, ViewEntry entry) {
        NamespaceConfigService configService = configService();
        boolean configIgnored = configService != null ? configService.isConfigIgnored() : GlobalConfigManager.isConfigIgnored(namespace);
        if (configIgnored) {
            entry.pendingStyleKey = normalizeStyleKey(view.getStyleKey());
            entry.styleDirty = false;
            return;
        }
        boolean shouldLoadStyles = configService != null
                ? configService.shouldLoad(ConfigFeature.STYLE_REFERENCES)
                : GlobalConfigManager.shouldLoadFeature(namespace, ConfigFeature.STYLE_REFERENCES);
        if (!shouldLoadStyles) {
            entry.pendingStyleKey = normalizeStyleKey(view.getStyleKey());
            entry.styleDirty = false;
            return;
        }
        Map<String, String> styles = configService != null
                ? configService.current().viewStyles()
                : GlobalConfigManager.getConfig(namespace).getViewStyles();
        String saved = styles.get(view.getId());
        if (saved != null && saved.isBlank()) {
            saved = null;
        }
        String current = normalizeStyleKey(view.getStyleKey());
        if (saved != null && !Objects.equals(saved, current)) {
            ResourceId identifier = ResourceId.tryParse(saved);
            if (identifier != null) {
                view.setStyleKey(identifier);
                current = identifier.toString();
            }
        }
        entry.pendingStyleKey = current;
        entry.styleDirty = false;
    }

    private void persistViewStyles() {
        NamespaceConfigService configService = configService();
        boolean configIgnored = configService != null ? configService.isConfigIgnored() : GlobalConfigManager.isConfigIgnored(namespace);
        if (configIgnored) {
            return;
        }
        boolean shouldSaveStyles = configService != null
                ? configService.shouldSave(ConfigFeature.STYLE_REFERENCES)
                : GlobalConfigManager.shouldSaveFeature(namespace, ConfigFeature.STYLE_REFERENCES);
        if (!shouldSaveStyles) {
            return;
        }
        boolean changed = false;
        Map<String, String> styles = configService != null
                ? new HashMap<>(configService.current().viewStyles())
                : GlobalConfigManager.getConfig(namespace).getViewStyles();
        for (Map.Entry<View, ViewEntry> entry : entries.entrySet()) {
            View view = entry.getKey();
            ViewEntry state = entry.getValue();
            if (view == null || !view.isPersistent() || state == null || !state.styleDirty) {
                continue;
            }
            state.styleDirty = false;
            String key = state.pendingStyleKey;
            String viewId = view.getId();
            if (key == null || key.isBlank()) {
                if (styles.remove(viewId) != null) {
                    changed = true;
                }
                continue;
            }
            if (!key.equals(styles.get(viewId))) {
                styles.put(viewId, key);
                changed = true;
            }
        }
        if (changed) {
            if (configService != null) {
                Map<String, String> nextStyles = new HashMap<>(styles);
                configService.update(cfg -> cfg.withViewStyles(nextStyles));
            } else {
                GlobalConfigManager.save(namespace);
            }
        }
    }

    private int persistStyleDescriptors() {
        int exported = 0;
        for (Map.Entry<View, ViewEntry> entry : entries.entrySet()) {
            View view = entry.getKey();
            ViewEntry state = entry.getValue();
            if (view == null || state == null || !view.isPersistent() || !state.descriptorDirty) {
                continue;
            }
            ViewPersistenceRequest request = ensureRequest(view, state);
            ViewStyleSnapshot snapshot;
            if (state.styleSnapshotJson == null || state.styleSnapshotJson.isBlank()) {
                snapshot = ViewStyleSnapshot.deleted(request);
            } else {
                snapshot = ViewStyleSnapshot.present(request, state.styleSnapshotJson);
            }
            boolean persisted = adapter.storeStyleSnapshot(snapshot);
            if (!persisted) {
                continue;
            }
            if (snapshot.deleted()) {
                state.persistedStyleSnapshotJson = null;
            } else {
                state.persistedStyleSnapshotJson = state.styleSnapshotJson;
                exported++;
            }
            state.descriptorDirty = false;
        }
        return exported;
    }

    private String normalizeStyleKey(ResourceId identifier) {
        if (identifier == null) {
            return null;
        }
        return identifier.toString();
    }

    private String scopedId(View view) {
        String viewNamespace = view.getNamespace();
        String viewId = persistenceViewId(view.getId());
        if (viewNamespace == null || viewNamespace.isBlank()) {
            return viewId;
        }
        return viewNamespace + "/" + viewId;
    }

    private String extractViewId(String headerLine) {
        Matcher matcher = WINDOW_HEADER_PATTERN.matcher(headerLine);
        if (!matcher.matches()) {
            return null;
        }
        String name = matcher.group("name");
        int markerIndex = name.lastIndexOf("##");
        if (markerIndex < 0 || markerIndex + 2 >= name.length()) {
            return null;
        }
        return name.substring(markerIndex + 2);
    }

    private ViewPersistenceRequest ensureRequest(View view, ViewEntry entry) {
        String scopedId = scopedId(view);
        String viewId = persistenceViewId(view.getId());
        ViewPersistenceRequest current = entry.request;
        if (current != null && Objects.equals(current.viewId(), viewId) && Objects.equals(current.scopedId(), scopedId)) {
            return current;
        }
        ViewPersistenceRequest request = new ViewPersistenceRequest(namespace, viewId, scopedId);
        entry.request = request;
        return request;
    }

    private String persistenceViewId(String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return "view";
        }
        return viewId;
    }

    private void updateAdapter(ViewPersistenceAdapter adapter) {
        this.adapter = adapter != null ? adapter : DEFAULT_ADAPTER;
    }

    private NamespaceConfigService configService() {
        MineGuiNamespaceContext context = MineGuiNamespaces.get(namespace);
        return context != null ? context.config() : null;
    }

    private static final class ViewEntry {
        private boolean loaded;
        private String loadedId;
        private String loadedScopedId;
        private ViewPersistenceRequest request;
        private String pendingStyleKey;
        private boolean styleDirty;
        private String styleSnapshotJson;
        private String persistedStyleSnapshotJson;
        private boolean descriptorDirty;
    }
}
