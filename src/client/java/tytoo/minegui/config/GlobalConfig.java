package tytoo.minegui.config;

import lombok.Getter;
import lombok.Setter;

public final class GlobalConfig {
    @Setter
    private boolean viewport = true;
    @Setter
    private boolean dockspace = true;
    @Getter
    private float globalScale = 1.0f;
    @Getter
    private String globalStyleKey;

    public GlobalConfig() {
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
