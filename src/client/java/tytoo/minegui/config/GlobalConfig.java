package tytoo.minegui.config;

import tytoo.minegui.MineGuiCore;

public final class GlobalConfig {
    private static final String DEFAULT_CONFIG_PATH = MineGuiCore.CONFIG_DIR.resolve("global_config.json").toString();
    private static final String DEFAULT_VIEW_SAVES_PATH = "views";

    private boolean viewport = true;
    private boolean dockspace = true;
    private String configPath = DEFAULT_CONFIG_PATH;
    private String viewSavesPath = DEFAULT_VIEW_SAVES_PATH;

    public GlobalConfig() {
    }

    public static String getDefaultViewSavesPath() {
        return DEFAULT_VIEW_SAVES_PATH;
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

    public String getViewSavesPath() {
        return viewSavesPath;
    }

    public void setViewSavesPath(String viewSavesPath) {
        this.viewSavesPath = viewSavesPath;
    }
}
