package tytoo.minegui.view;

import tytoo.minegui.manager.ViewSaveManager;

public abstract class MGView {
    private boolean visible;
    private String id;
    private boolean savable;

    protected MGView() {
        this.id = deriveDefaultId();
    }

    protected MGView(String id) {
        this();
        setId(id);
    }

    protected MGView(String id, boolean savable) {
        this(id);
        setSavable(savable);
    }

    public final void render() {
        if (!visible) {
            return;
        }
        renderView();
    }

    protected abstract void renderView();

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;
        if (visible) {
            onOpen();
        } else {
            onClose();
            if (savable) {
                ViewSaveManager.getInstance().requestSave();
            }
        }
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public void toggleVisibility() {
        setVisible(!visible);
    }

    protected void onOpen() {
    }

    protected void onClose() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            this.id = deriveDefaultId();
            return;
        }
        this.id = id;
    }

    public boolean isSavable() {
        return savable;
    }

    public void setSavable(boolean savable) {
        this.savable = savable;
    }

    protected String deriveDefaultId() {
        return getClass().getName();
    }

    protected String scopedWindowTitle(String displayTitle) {
        String base = displayTitle != null ? displayTitle : "";
        if (base.contains("##")) {
            return base;
        }
        return base + "##" + id;
    }
}
