package tytoo.minegui.config;

import tytoo.minegui.util.ResourceId;

import java.util.Objects;

public final class GlobalConfigNamespaceConfigStore implements NamespaceConfigStore {
    @Override
    public NamespaceConfig load(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        GlobalConfigService service = ConfigRegistry.get(namespace);
        GlobalConfig config = service.config();
        String configuredStyle = config.getGlobalStyleKey();
        ResourceId styleKey = (configuredStyle == null || configuredStyle.isBlank())
                ? null
                : ResourceId.tryParse(configuredStyle);
        return new NamespaceConfig(
                namespace,
                config.isViewportEnabled(),
                config.isDockspaceEnabled(),
                config.getGlobalScale(),
                config.getConfigPath(),
                config.getViewSavesPath(),
                styleKey
        );
    }

    @Override
    public void save(NamespaceConfig config) {
        Objects.requireNonNull(config, "config");
        GlobalConfigService service = ConfigRegistry.get(config.namespace());
        GlobalConfig globalConfig = service.config();
        globalConfig.setViewport(config.viewportEnabled());
        globalConfig.setDockspace(config.dockspaceEnabled());
        globalConfig.setGlobalScale(config.globalScale());
        globalConfig.setConfigPath(config.configPath());
        globalConfig.setViewSavesPath(config.viewSavesPath());
        ResourceId styleKey = config.globalStyleKey();
        globalConfig.setGlobalStyleKey(styleKey != null ? styleKey.toString() : null);
        service.save();
    }
}
