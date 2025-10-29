package tytoo.minegui.config;

import java.nio.file.Path;

public record ConfigPathResolution(Path configFile, Path viewSavesDirectory) {
}
