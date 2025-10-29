package tytoo.minegui.config;

import java.nio.file.Path;

public record ConfigPathValidationResult(boolean allowed, Path path, String message) {
    public static ConfigPathValidationResult allowed(Path path) {
        return new ConfigPathValidationResult(true, path, null);
    }

    public static ConfigPathValidationResult allowed(Path path, String message) {
        return new ConfigPathValidationResult(true, path, message);
    }

    public static ConfigPathValidationResult rejected(String message) {
        return new ConfigPathValidationResult(false, null, message);
    }
}
