package tytoo.minegui.config;

import lombok.Getter;
import lombok.Setter;

public final class GlobalConfig {
    private static final String DEFAULT_CONFIG_PATH = "global_config.json";
    private static final String DEFAULT_VIEW_SAVES_PATH = "";

    @Setter
    private boolean viewport = true;
    @Setter
    private boolean dockspace = true;
    @Getter
    private float globalScale = 1.0f;
    @Setter
    @Getter
    private String configPath = DEFAULT_CONFIG_PATH;
    @Setter
    @Getter
    private String viewSavesPath = DEFAULT_VIEW_SAVES_PATH;
    @Getter
    private String globalStyleKey;

    public GlobalConfig() {
    }

    public static String getDefaultViewSavesPath() {
        return DEFAULT_VIEW_SAVES_PATH;
    }

    public boolean isViewportEnabled() {
        return viewport;
    }

    public boolean isDockspaceEnabled() {
        return dockspace;
    }

    public void setGlobalScale(float globalScale) {
        if (!Float.isFinite(globalScale) || globalScale <= 0.0f) {
            this.globalScale = 1.0f;
            return;
        }
        this.globalScale = globalScale;
    }

    public void setGlobalStyleKey(String globalStyleKey) {
        this.globalStyleKey = (globalStyleKey == null || globalStyleKey.isBlank()) ? null : globalStyleKey;
    }
}
