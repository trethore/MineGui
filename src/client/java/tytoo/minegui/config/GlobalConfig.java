package tytoo.minegui.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public final class GlobalConfig {
    private static final String DEFAULT_CONFIG_PATH = "global_config.json";
    private static final String DEFAULT_VIEW_SAVES_PATH = "views";

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
    private Map<String, String> viewStyles = new HashMap<>();

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
