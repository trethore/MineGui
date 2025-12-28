package tytoo.minegui.config;

import tytoo.minegui.MineGuiCore;

import java.nio.file.Path;

final class ConfigPathResolver {
    private final Path configRoot;
    private final Path namespaceRoot;
    private final ConfigPathStrategy defaultStrategy;

    ConfigPathResolver(Path configRoot, Path namespaceRoot, ConfigPathStrategy defaultStrategy) {
        this.configRoot = configRoot;
        this.namespaceRoot = namespaceRoot;
        this.defaultStrategy = defaultStrategy;
    }

    ConfigPathResolution resolvePaths(ConfigState state, GlobalConfig target) {
        ConfigPathRequest request = buildRequest(state);
        Path configPath = resolvePathWithFallback(state, request, ConfigPathKind.CONFIG_FILE);
        Path viewPath = resolvePathWithFallback(state, request, ConfigPathKind.VIEW_SAVES_DIRECTORY);
        return new ConfigPathResolution(configPath, viewPath);
    }

    void applyResolvedPaths(ConfigState state, GlobalConfig target, ConfigPathResolution resolution) {
        // No-op as paths are no longer stored in GlobalConfig
    }

    Path resolveConfigPath(ConfigState state, GlobalConfig target) {
        ConfigPathResolution resolution = resolvePaths(state, target);
        return resolution.configFile();
    }

    Path resolveViewSavesPath(ConfigState state, GlobalConfig target) {
        ConfigPathResolution resolution = resolvePaths(state, target);
        return resolution.viewSavesDirectory();
    }

    private ConfigPathRequest buildRequest(ConfigState state) {
        return new ConfigPathRequest(
                state.namespace(),
                configRoot,
                namespaceRoot,
                state.baseDirectory(),
                state.defaultConfigFile(),
                state.defaultViewSavesDir()
        );
    }

    private Path resolvePathWithFallback(ConfigState state, ConfigPathRequest request, ConfigPathKind kind) {
        ConfigPathStrategy strategy = state.strategy() != null ? state.strategy() : defaultStrategy;
        Path resolved = resolveWithStrategy(strategy, request, kind, state.namespace());
        if (resolved == null && strategy != defaultStrategy) {
            MineGuiCore.LOGGER.warn("Config path strategy for namespace '{}' returned an invalid {}; using sandboxed defaults.", state.namespace(), describe(kind));
            resolved = resolveWithStrategy(defaultStrategy, request, kind, state.namespace());
        }
        if (resolved == null) {
            resolved = kind == ConfigPathKind.CONFIG_FILE ? state.defaultConfigFile() : state.defaultViewSavesDir();
        }
        return resolved.normalize();
    }

    private Path resolveWithStrategy(ConfigPathStrategy strategy, ConfigPathRequest request, ConfigPathKind kind, String namespace) {
        if (strategy == null) {
            return null;
        }
        try {
            Path candidate = kind == ConfigPathKind.CONFIG_FILE
                    ? strategy.resolveConfigFile(request)
                    : strategy.resolveViewSavesDirectory(request);
            if (candidate == null) {
                return null;
            }
            Path normalized = candidate.normalize();
            ConfigPathValidationResult validation = kind == ConfigPathKind.CONFIG_FILE
                    ? strategy.validateConfigFile(request, normalized)
                    : strategy.validateViewSavesDirectory(request, normalized);
            String message = validation != null ? validation.message() : null;
            boolean hasMessage = message != null && !message.isBlank();
            if (validation == null) {
                return normalized;
            }
            if (!validation.allowed()) {
                if (hasMessage) {
                    MineGuiCore.LOGGER.warn(message);
                }
                return null;
            }
            Path resultPath = validation.path() != null ? validation.path().normalize() : null;
            if (resultPath == null) {
                return null;
            }
            if (hasMessage) {
                MineGuiCore.LOGGER.info(message);
            }
            return resultPath;
        } catch (RuntimeException e) {
            MineGuiCore.LOGGER.error("Config path strategy for namespace '{}' failed to resolve {}.", namespace, describe(kind), e);
            return null;
        }
    }

    private static String describe(ConfigPathKind kind) {
        if (kind == ConfigPathKind.CONFIG_FILE) {
            return "config file";
        }
        return "view saves directory";
    }

    private enum ConfigPathKind {
        CONFIG_FILE,
        VIEW_SAVES_DIRECTORY
    }
}
