package tytoo.minegui.persistence;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.GlobalConfigManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

public final class FileViewPersistenceAdapter implements ViewPersistenceAdapter {
    private static final String LAYOUTS_FOLDER = "layouts";
    private static final String STYLES_FOLDER = "styles";

    @Override
    public Optional<String> loadLayout(ViewPersistenceRequest request) {
        Path hashed = ViewSavePaths.hashedLayout(request.namespace(), request.viewId());
        ensureParentDirectory(request, hashed);
        Path legacy = ViewSavePaths.legacyLayout(request.namespace(), request.viewId());
        Path source = Files.exists(hashed) ? hashed : legacy;
        if (!Files.exists(source)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(source, StandardCharsets.UTF_8));
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to read view ini for {} in {}", request.viewId(), request.namespace(), e);
            return Optional.empty();
        }
    }

    @Override
    public void saveLayout(ViewPersistenceRequest request, String iniContent) {
        if (iniContent == null || iniContent.isEmpty()) {
            return;
        }
        Path target = ViewSavePaths.hashedLayout(request.namespace(), request.viewId());
        ensureParentDirectory(request, target);
        try {
            Files.writeString(target, iniContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to write view ini for {} in {}", request.viewId(), request.namespace(), e);
            return;
        }
        cleanupLegacyLayout(request, target);
    }

    @Override
    public Optional<String> loadStyleSnapshot(ViewPersistenceRequest request) {
        Path hashed = ViewSavePaths.hashedStyle(request.namespace(), request.viewId());
        Path legacy = ViewSavePaths.legacyStyle(request.namespace(), request.viewId());
        Path source = Files.exists(hashed) ? hashed : legacy;
        if (!Files.exists(source)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(source, StandardCharsets.UTF_8));
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to read style json for {} in {}", request.viewId(), request.namespace(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean storeStyleSnapshot(ViewStyleSnapshot snapshot) {
        ViewPersistenceRequest request = snapshot.request();
        Path target = ViewSavePaths.hashedStyle(request.namespace(), request.viewId());
        ensureParentDirectory(request, target);
        if (snapshot.deleted()) {
            boolean deleted = deleteIfExists(request.namespace(), target);
            cleanupLegacyStyle(request, target);
            return deleted;
        }
        try {
            Files.writeString(target, snapshot.snapshotJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            cleanupLegacyStyle(request, target);
            return true;
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to write style json for {} in {}", request.viewId(), request.namespace(), e);
            return false;
        }
    }

    private void cleanupLegacyLayout(ViewPersistenceRequest request, Path currentPath) {
        Path legacy = ViewSavePaths.legacyLayout(request.namespace(), request.viewId());
        if (!legacy.equals(currentPath)) {
            deleteIfExists(request.namespace(), legacy);
        }
    }

    private void cleanupLegacyStyle(ViewPersistenceRequest request, Path currentPath) {
        Path legacy = ViewSavePaths.legacyStyle(request.namespace(), request.viewId());
        if (!legacy.equals(currentPath)) {
            deleteIfExists(request.namespace(), legacy);
        }
    }

    private void ensureParentDirectory(ViewPersistenceRequest request, Path target) {
        Path parent = target.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to ensure view save directory {} for {}", parent, request.namespace(), e);
        }
    }

    private boolean deleteIfExists(String namespace, Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to delete view save file {} for {}", path, namespace, e);
            return false;
        }
    }

    private static final class ViewSavePaths {
        private static final int HASH_PREFIX_LENGTH = 24;
        private static final int DISPLAY_MAX_LENGTH = 48;
        private static final HexFormat HEX = HexFormat.of();
        private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

        private ViewSavePaths() {
        }

        static Path hashedLayout(String namespace, String viewId) {
            Path base = GlobalConfigManager.getViewSavesDirectory(namespace).resolve(LAYOUTS_FOLDER);
            return base.resolve(hashedFileName(viewId, ".ini"));
        }

        static Path hashedStyle(String namespace, String viewId) {
            Path base = GlobalConfigManager.getViewSavesDirectory(namespace).resolve(STYLES_FOLDER);
            return base.resolve(hashedFileName(viewId, ".json"));
        }

        static Path legacyLayout(String namespace, String viewId) {
            Path base = GlobalConfigManager.getViewSavesDirectory(namespace);
            return base.resolve(legacyFileName(viewId, ".ini"));
        }

        static Path legacyStyle(String namespace, String viewId) {
            Path base = GlobalConfigManager.getViewSavesDirectory(namespace).resolve(STYLES_FOLDER);
            return base.resolve(legacyFileName(viewId, ".json"));
        }

        private static String hashedFileName(String viewId, String extension) {
            String normalizedId = normalizeId(viewId);
            String hashed = hash(normalizedId);
            String display = truncate(sanitize(normalizedId));
            if (display.isBlank()) {
                display = "view";
            }
            String hashSegment = hashed.length() > HASH_PREFIX_LENGTH ? hashed.substring(0, HASH_PREFIX_LENGTH) : hashed;
            return display + "-" + hashSegment + extension;
        }

        private static String legacyFileName(String viewId, String extension) {
            String sanitized = sanitize(viewId);
            if (sanitized.isBlank()) {
                sanitized = "view";
            }
            return sanitized + extension;
        }

        private static String normalizeId(String viewId) {
            if (viewId == null || viewId.isBlank()) {
                return "view";
            }
            return viewId;
        }

        private static String sanitize(String viewId) {
            String normalized = normalizeId(viewId);
            return INVALID_FILENAME_CHARS.matcher(normalized).replaceAll("_");
        }

        private static String truncate(String value) {
            if (value.length() <= DISPLAY_MAX_LENGTH) {
                return value;
            }
            return value.substring(0, DISPLAY_MAX_LENGTH);
        }

        private static String hash(String value) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
                return HEX.formatHex(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
        }
    }
}
