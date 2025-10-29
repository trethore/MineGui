package tytoo.minegui.config;

import java.nio.file.Path;

public record ConfigPathRequest(
        String namespace,
        String requestedConfigPath,
        String requestedViewSavesPath,
        Path configRoot,
        Path namespaceRoot,
        Path baseDirectory,
        Path defaultConfigFile,
        Path defaultViewSavesDirectory
) {
}
