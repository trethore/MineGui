package tytoo.minegui.config;

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
            // Force structure: {root}/{namespace}/global_config.json
            return root.resolve(request.namespace()).resolve("global_config.json").normalize();
        }

        @Override
        public Path resolveViewSavesDirectory(ConfigPathRequest request) {
            // Force structure: {root}/{namespace}/views/
            return root.resolve(request.namespace()).resolve("views").normalize();
        }
    }

    private static final class SandboxedConfigPathStrategy implements ConfigPathStrategy {
        @Override
        public Path resolveConfigFile(ConfigPathRequest request) {
            return request.defaultConfigFile();
        }

        @Override
        public Path resolveViewSavesDirectory(ConfigPathRequest request) {
            return request.defaultViewSavesDirectory();
        }

        @Override
        public ConfigPathValidationResult validateConfigFile(ConfigPathRequest request, Path resolved) {
            return validateResolvedPath(request, resolved, "config path");
        }

        @Override
        public ConfigPathValidationResult validateViewSavesDirectory(ConfigPathRequest request, Path resolved) {
            return validateResolvedPath(request, resolved, "view saves path");
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
    }
}
