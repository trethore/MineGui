package tytoo.minegui.runtime.config;

import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;

import java.nio.file.Path;

public record NamespaceConfigAccess(String namespace) {

    public GlobalConfig get() {
        return GlobalConfigManager.getConfig(namespace);
    }

    public void load() {
        GlobalConfigManager.load(namespace);
    }

    public void save() {
        GlobalConfigManager.save(namespace);
    }

    public void reset() {
        GlobalConfigManager.reset(namespace);
    }

    public Path configPath() {
        return GlobalConfigManager.getActiveConfigPath(namespace);
    }

    public Path viewSavesPath() {
        return GlobalConfigManager.getViewSavesDirectory(namespace);
    }
}
