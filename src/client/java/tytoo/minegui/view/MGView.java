package tytoo.minegui.view;

import net.minecraft.util.Identifier;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.style.MGStyleDelta;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.view.cursor.MGCursorPolicies;
import tytoo.minegui.view.cursor.MGCursorPolicy;

public abstract class MGView {
    private boolean visible;
    private String id;
    private boolean shouldSave;
    private Identifier styleKey;
    private String namespace;
    private ViewSaveManager viewSaveManager;
    private MGCursorPolicy cursorPolicy;
    private boolean cursorPolicyExplicit;

    protected MGView() {
        this.id = deriveDefaultId();
        this.cursorPolicy = MGCursorPolicies.empty();
        this.cursorPolicyExplicit = false;
    }

    protected MGView(String id) {
        this();
        setId(id);
    }

    protected MGView(String id, boolean shouldSave) {
        this(id);
        setShouldSave(shouldSave);
    }

    public final void render() {
        if (!visible) {
            return;
        }
        renderView();
    }

    protected abstract void renderView();

    public MGStyleDelta configureStyleDelta() {
        return null;
    }

    public MGStyleDescriptor configureBaseStyle(MGStyleDescriptor descriptor) {
        return descriptor;
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

    protected String deriveDefaultId() {
        return getClass().getName();
    }

    public String scopedWindowTitle(String displayTitle) {
        String base = displayTitle != null ? displayTitle : "";
        if (base.contains("##")) {
            return base;
        }
        String scopeId = id;
        if (namespace != null && !namespace.isBlank()) {
            scopeId = namespace + "/" + id;
        }
        return base + "##" + scopeId;
    }

    public void attach(String namespace, ViewSaveManager saveManager) {
        this.namespace = namespace;
        this.viewSaveManager = saveManager;
    }

    public void detach() {
        this.namespace = null;
        this.viewSaveManager = null;
    }

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
            cursorPolicy.onOpen(this);
        } else {
            cursorPolicy.onClose(this);
            onClose();
            if (shouldSave && viewSaveManager != null) {
                viewSaveManager.requestSave();
            }
        }
    }

    public Identifier getStyleKey() {
        return styleKey;
    }

    public void setStyleKey(Identifier styleKey) {
        this.styleKey = styleKey;
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

    public boolean isShouldSave() {
        return shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public String getNamespace() {
        return namespace;
    }

    public MGCursorPolicy getCursorPolicy() {
        return cursorPolicy;
    }

    public void setCursorPolicy(MGCursorPolicy cursorPolicy) {
        boolean explicit = cursorPolicy != null;
        MGCursorPolicy resolved = explicit ? cursorPolicy : MGCursorPolicies.empty();
        if (this.cursorPolicy == resolved && this.cursorPolicyExplicit == explicit) {
            return;
        }
        if (visible) {
            this.cursorPolicy.onClose(this);
        }
        this.cursorPolicy = resolved;
        this.cursorPolicyExplicit = explicit;
        if (visible) {
            this.cursorPolicy.onOpen(this);
        }
    }

    public boolean hasExplicitCursorPolicy() {
        return cursorPolicyExplicit;
    }

    public void applyDefaultCursorPolicy(MGCursorPolicy defaultPolicy) {
        if (cursorPolicyExplicit) {
            return;
        }
        MGCursorPolicy resolved = defaultPolicy != null ? defaultPolicy : MGCursorPolicies.empty();
        if (this.cursorPolicy == resolved) {
            return;
        }
        if (visible) {
            this.cursorPolicy.onClose(this);
        }
        this.cursorPolicy = resolved;
        if (visible) {
            this.cursorPolicy.onOpen(this);
        }
    }
}
