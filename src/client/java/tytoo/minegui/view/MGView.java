package tytoo.minegui.view;

public abstract class MGView {
    private boolean visible;

    protected MGView() {
        this.visible = true;
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
        this.visible = visible;
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public void toggle() {
        setVisible(!visible);
    }
}
