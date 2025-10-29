package tytoo.minegui.config;

import java.nio.file.Path;

public interface ConfigPathStrategy {
    Path resolveConfigFile(ConfigPathRequest request);

    Path resolveViewSavesDirectory(ConfigPathRequest request);

    default ConfigPathValidationResult validateConfigFile(ConfigPathRequest request, Path resolved) {
        return ConfigPathValidationResult.allowed(resolved);
    }

    default ConfigPathValidationResult validateViewSavesDirectory(ConfigPathRequest request, Path resolved) {
        return ConfigPathValidationResult.allowed(resolved);
    }
}
