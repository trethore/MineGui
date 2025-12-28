package tytoo.minegui.config;

import java.nio.file.Path;

@Deprecated(forRemoval = true)
public class ConfigService extends GlobalConfigService {
    ConfigService(String namespace, Path configRoot, ConfigPathStrategy defaultStrategy) {
        super(namespace, configRoot, defaultStrategy);
    }
}
