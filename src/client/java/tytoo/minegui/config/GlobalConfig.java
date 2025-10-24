package tytoo.minegui.config;

import java.util.HashMap;
import java.util.Map;

public final class GlobalConfig {
    private static final String DEFAULT_CONFIG_PATH = "global_config.json";
    private static final String DEFAULT_VIEW_SAVES_PATH = "views";

    private boolean viewport = true;
    private boolean dockspace = true;
    private String configPath = DEFAULT_CONFIG_PATH;
    private String viewSavesPath = DEFAULT_VIEW_SAVES_PATH;
    private String globalStyleKey;
    private Map<String, String> viewStyles = new HashMap<>();

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

    public String getGlobalStyleKey() {
        return globalStyleKey;
    }

    public void setGlobalStyleKey(String globalStyleKey) {
        this.globalStyleKey = (globalStyleKey == null || globalStyleKey.isBlank()) ? null : globalStyleKey;
    }

    public Map<String, String> getViewStyles() {
        if (viewStyles == null) {
            viewStyles = new HashMap<>();
        }
        return viewStyles;
    }

    public void setViewStyles(Map<String, String> viewStyles) {
        if (viewStyles == null) {
            this.viewStyles = new HashMap<>();
            return;
        }
        this.viewStyles = new HashMap<>(viewStyles);
    }
}
