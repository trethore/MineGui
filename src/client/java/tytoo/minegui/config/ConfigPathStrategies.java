package tytoo.minegui.config;

import org.jetbrains.annotations.NotNull;
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

    public static ConfigPathStrategy root(Path root) {
        if (root == null) {
            return SANDBOXED;
        }
        return new FixedRootConfigPathStrategy(root);
    }

    private record FixedRootConfigPathStrategy(Path root) implements ConfigPathStrategy {
        private FixedRootConfigPathStrategy(Path root) {
            this.root = root.toAbsolutePath().normalize();
        }

        @Override
        public Path resolveConfigFile(ConfigPathRequest request) {
            return resolve(request.requestedConfigPath(), "global_config.json");
        }

        @Override
        public Path resolveViewSavesDirectory(ConfigPathRequest request) {
            return resolve(request.requestedViewSavesPath(), ".");
        }

        private Path resolve(String configured, String defaultValue) {
            Path fallback = root.resolve(defaultValue).normalize();
            if (configured == null || configured.isBlank()) {
                return fallback;
            }
            try {
                Path candidate = getCandidatePath(configured);
                if (!candidate.startsWith(root)) {
                    return fallback;
                }
                return candidate;
            } catch (InvalidPathException e) {
                return fallback;
            }
        }

        private @NotNull Path getCandidatePath(String configured) {
            Path candidate = Path.of(configured).normalize();
            if (!candidate.isAbsolute()) {
                String rootName = root.getFileName() != null ? root.getFileName().toString() : null;
                if (rootName != null && candidate.getNameCount() > 0 && rootName.equals(candidate.getName(0).toString())) {
                    candidate = candidate.getNameCount() == 1 ? Path.of("") : candidate.subpath(1, candidate.getNameCount());
                }
                candidate = root.resolve(candidate).normalize();
            }
            return candidate;
        }
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
            if (!normalized.startsWith(request.baseDirectory())) {
                return ConfigPathValidationResult.rejected(label + " '" + normalized + "' resolves outside of the namespace directory; using default");
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
