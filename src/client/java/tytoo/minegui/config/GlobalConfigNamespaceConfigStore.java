package tytoo.minegui.config;

import tytoo.minegui.util.ResourceId;

import java.util.Map;
import java.util.Objects;

public final class GlobalConfigNamespaceConfigStore implements NamespaceConfigStore {
    @Override
    public NamespaceConfig load(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        GlobalConfig config = GlobalConfigManager.getConfig(namespace);
        String configuredStyle = config.getGlobalStyleKey();
        ResourceId styleKey = (configuredStyle == null || configuredStyle.isBlank())
                ? null
                : ResourceId.tryParse(configuredStyle);
        Map<String, String> viewStyles = Map.copyOf(config.getViewStyles());
        return new NamespaceConfig(
                namespace,
                config.isViewportEnabled(),
                config.isDockspaceEnabled(),
                config.getGlobalScale(),
                config.getConfigPath(),
                config.getViewSavesPath(),
                styleKey,
                viewStyles
        );
    }

    @Override
    public void save(NamespaceConfig config) {
        Objects.requireNonNull(config, "config");
        GlobalConfig globalConfig = GlobalConfigManager.getConfig(config.namespace());
        globalConfig.setViewport(config.viewportEnabled());
        globalConfig.setDockspace(config.dockspaceEnabled());
        globalConfig.setGlobalScale(config.globalScale());
        globalConfig.setConfigPath(config.configPath());
        globalConfig.setViewSavesPath(config.viewSavesPath());
        ResourceId styleKey = config.globalStyleKey();
        globalConfig.setGlobalStyleKey(styleKey != null ? styleKey.toString() : null);
        globalConfig.setViewStyles(config.viewStyles());
        GlobalConfigManager.save(config.namespace());
    }
}
