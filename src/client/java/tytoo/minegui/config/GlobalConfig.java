package tytoo.minegui.config;

import tytoo.minegui.MineGuiCore;

public final class GlobalConfig {
    private static final String DEFAULT_CONFIG_PATH = MineGuiCore.CONFIG_DIR.resolve("global_config.json").toString();

    private boolean viewport = true;
    private boolean dockspace = true;
    private String configPath = DEFAULT_CONFIG_PATH;

    public GlobalConfig() {
    }

    public boolean isViewportEnabled() {
        return viewport;
    }

    public void setViewport(boolean viewport) {
        this.viewport = viewport;
    }

    public boolean isDockspaceEnabled() {
        return dockspace;
    }

    public void setDockspace(boolean dockspace) {
        this.dockspace = dockspace;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
