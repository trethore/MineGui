package tytoo.minegui.config;

import tytoo.minegui.MineGuiCore;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public final class ConfigPathStrategies {
    private static final ConfigPathStrategy SANDBOXED = new SandboxedConfigPathStrategy();

    private ConfigPathStrategies() {
    }

    public static ConfigPathStrategy sandboxed() {
        return SANDBOXED;
    }

    private static final class SandboxedConfigPathStrategy implements ConfigPathStrategy {
        @Override
        public Path resolveConfigFile(ConfigPathRequest request) {
            return resolveTarget(request.requestedConfigPath(), request.defaultConfigFile(), request, "config path");
        }

        @Override
        public Path resolveViewSavesDirectory(ConfigPathRequest request) {
            return resolveTarget(request.requestedViewSavesPath(), request.defaultViewSavesDirectory(), request, "view saves path");
        }

        @Override
        public ConfigPathValidationResult validateConfigFile(ConfigPathRequest request, Path resolved) {
            return validateResolvedPath(request, resolved, "config path");
        }

        @Override
        public ConfigPathValidationResult validateViewSavesDirectory(ConfigPathRequest request, Path resolved) {
            return validateResolvedPath(request, resolved, "view saves path");
        }

        private Path resolveTarget(String configuredPath, Path defaultPath, ConfigPathRequest request, String label) {
            if (configuredPath == null || configuredPath.isBlank()) {
                return defaultPath;
            }
            Path candidate;
            try {
                candidate = Path.of(configuredPath);
            } catch (InvalidPathException e) {
                MineGuiCore.LOGGER.warn("Invalid {} '{}' for namespace '{}', using default", label, configuredPath, request.namespace());
                return defaultPath;
            }
            if (candidate.isAbsolute()) {
                return candidate.normalize();
            }
            if (candidate.getNameCount() == 1 && isSimpleName(candidate)) {
                return request.baseDirectory().resolve(candidate).normalize();
            }
            return request.configRoot().resolve(candidate).normalize();
        }

        private ConfigPathValidationResult validateResolvedPath(ConfigPathRequest request, Path resolved, String label) {
            if (resolved == null) {
                return ConfigPathValidationResult.rejected("Missing " + label + " for namespace '" + request.namespace() + "', using default");
            }
            Path normalized = resolved.normalize();
            if (!normalized.startsWith(request.namespaceRoot())) {
                return ConfigPathValidationResult.rejected(label + " '" + normalized + "' resolves outside of the MineGui config directory; using default");
            }
            return ConfigPathValidationResult.allowed(normalized);
        }

        private boolean isSimpleName(Path path) {
            if (path == null) {
                return false;
            }
            if (path.getNameCount() != 1) {
                return false;
            }
            String name = path.getFileName().toString();
            return !".".equals(name) && !"..".equals(name);
        }
    }
}
