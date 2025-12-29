package tytoo.minegui.imgui;

import imgui.ImGui;
import net.fabricmc.loader.api.FabricLoader;
import tytoo.minegui.MineGuiCore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class GlobalLayoutManager {

    private static final String GLOBAL_INI_FILENAME = "_global.ini";
    private static final long MIN_SAVE_INTERVAL_NANOS = 500_000_000L;

    private static volatile boolean loaded;
    private static volatile boolean dirty;
    private static volatile long lastSaveNanos;

    private GlobalLayoutManager() {
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        Path globalPath = getGlobalIniPath();
        if (Files.exists(globalPath)) {
            try {
                String content = Files.readString(globalPath, StandardCharsets.UTF_8);
                ImGui.loadIniSettingsFromMemory(content);
            } catch (IOException e) {
                MineGuiCore.LOGGER.warn("Failed to load global layout from {}", globalPath, e);
            }
        }
        loaded = true;
    }

    public static void markDirty() {
        dirty = true;
    }

    public static void flush() {
        flush(false);
    }

    public static void flush(boolean force) {
        if (!dirty) {
            return;
        }
        long now = System.nanoTime();
        if (!force && now - lastSaveNanos < MIN_SAVE_INTERVAL_NANOS) {
            return;
        }
        lastSaveNanos = now;

        String payload = ImGui.saveIniSettingsToMemory();
        String filtered = filterGlobalSections(payload);

        if (filtered.isBlank()) {
            dirty = false;
            return;
        }

        Path globalPath = getGlobalIniPath();
        try {
            Files.createDirectories(globalPath.getParent());
            Files.writeString(globalPath, filtered, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to save global layout to {}", globalPath, e);
        }
        dirty = false;
    }

    public static void reset() {
        loaded = false;
        dirty = false;
        lastSaveNanos = 0;
    }

    private static String filterGlobalSections(String payload) {
        StringBuilder result = new StringBuilder();
        StringBuilder currentSection = new StringBuilder();
        boolean includeCurrentSection = false;

        String[] lines = payload.split("\\R");
        for (String line : lines) {
            if (line.startsWith("[")) {
                if (includeCurrentSection && !currentSection.isEmpty()) {
                    result.append(currentSection);
                }
                currentSection.setLength(0);
                includeCurrentSection = shouldIncludeSection(line);
            }
            currentSection.append(line).append('\n');
        }

        if (includeCurrentSection && !currentSection.isEmpty()) {
            result.append(currentSection);
        }

        return result.toString();
    }

    private static boolean shouldIncludeSection(String header) {
        if (header.startsWith("[Docking]")) {
            return true;
        }
        if (header.startsWith("[Window][")) {
            String windowName = extractWindowName(header);
            if (windowName == null) {
                return false;
            }
            return !windowName.contains("##");
        }
        return false;
    }

    private static String extractWindowName(String header) {
        int start = header.indexOf("][");
        int end = header.lastIndexOf(']');
        if (start < 0 || end <= start + 2) {
            return null;
        }
        return header.substring(start + 2, end);
    }

    private static Path getGlobalIniPath() {
        return FabricLoader.getInstance().getConfigDir()
                .resolve(MineGuiCore.ID)
                .resolve(GLOBAL_INI_FILENAME);
    }
}
