package tytoo.minegui.manager;

import imgui.ImGui;
import imgui.ImGuiIO;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.view.MGView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ViewSaveManager {
    private static final ViewSaveManager INSTANCE = new ViewSaveManager();
    private static final Pattern WINDOW_HEADER_PATTERN = Pattern.compile("^\\[[^]]+]\\[(?<name>[^]]+)]$");
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

    private final Map<MGView, ViewEntry> entries = new ConcurrentHashMap<>();
    private volatile boolean forceSave;

    private ViewSaveManager() {
    }

    public static ViewSaveManager getInstance() {
        return INSTANCE;
    }

    public void register(MGView view) {
        if (view == null) {
            return;
        }
        entries.computeIfAbsent(view, unused -> new ViewEntry());
    }

    public void unregister(MGView view) {
        if (view == null) {
            return;
        }
        entries.remove(view);
    }

    public void prepareView(MGView view) {
        if (view == null) {
            return;
        }
        if (!view.isSavable()) {
            entries.remove(view);
            return;
        }
        ViewEntry entry = entries.computeIfAbsent(view, unused -> new ViewEntry());
        String currentId = view.getId();
        Path targetPath = resolvePath(currentId);
        if (!Objects.equals(entry.loadedId, currentId) || !Objects.equals(entry.loadedPath, targetPath)) {
            entry.loaded = false;
        }
        if (entry.loaded) {
            return;
        }
        entry.loadedId = currentId;
        entry.loadedPath = targetPath;
        ensureParentDirectory(targetPath);
        if (Files.exists(targetPath)) {
            ImGui.loadIniSettingsFromDisk(targetPath.toString());
        }
        entry.loaded = true;
    }

    public void requestSave() {
        forceSave = true;
    }

    public void onFrameRendered() {
        if (entries.isEmpty()) {
            forceSave = false;
            return;
        }
        ImGuiIO io = ImGui.getIO();
        if (!forceSave && !io.getWantSaveIniSettings()) {
            return;
        }
        String iniContent = ImGui.saveIniSettingsToMemory();
        persistEntries(iniContent);
        forceSave = false;
    }

    private void persistEntries(String iniContent) {
        if (iniContent == null || iniContent.isEmpty()) {
            return;
        }
        Map<String, ViewEntry> activeEntries = entries.entrySet().stream()
                .filter(entry -> entry.getKey().isSavable())
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (first, second) -> first));
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
            MineGuiCore.LOGGER.error("Failed to parse ImGui ini data", e);
            return;
        }
        for (Map.Entry<String, StringBuilder> entry : bufferedSections.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            ViewEntry viewEntry = activeEntries.get(entry.getKey());
            Path targetPath = viewEntry != null && viewEntry.loadedPath != null
                    ? viewEntry.loadedPath
                    : resolvePath(entry.getKey());
            ensureParentDirectory(targetPath);
            try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                writer.write(entry.getValue().toString());
            } catch (IOException e) {
                MineGuiCore.LOGGER.error("Failed to write view ini for {}", entry.getKey(), e);
            }
        }
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

    private Path resolvePath(String viewId) {
        Path directory = GlobalConfigManager.getViewSavesDirectory();
        String sanitized = sanitizeId(viewId);
        return directory.resolve(sanitized + ".ini");
    }

    private String sanitizeId(String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return "view";
        }
        String replaced = INVALID_FILENAME_CHARS.matcher(viewId).replaceAll("_");
        if (replaced.isEmpty()) {
            return "view";
        }
        return replaced;
    }

    private void ensureParentDirectory(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to ensure view ini directory {}", parent, e);
        }
    }

    private static final class ViewEntry {
        private boolean loaded;
        private String loadedId;
        private Path loadedPath;
    }
}
