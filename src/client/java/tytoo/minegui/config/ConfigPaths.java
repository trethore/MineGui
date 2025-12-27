package tytoo.minegui.config;

import java.nio.file.Path;

final class ConfigPaths {

    private ConfigPaths() {
    }

    static String relativizeToRoot(Path path, Path root) {
        if (path == null) {
            return null;
        }
        Path normalized = path.normalize();
        if (normalized.startsWith(root)) {
            return root.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString().replace('\\', '/');
    }

    static String sanitizeStoredPath(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replace('\\', '/');
    }
}
