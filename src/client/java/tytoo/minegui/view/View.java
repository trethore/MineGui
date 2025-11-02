package tytoo.minegui.view;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

public abstract class View {
    @Getter
    private boolean visible;
    @Getter
    private String id;
    @Getter
    @Setter
    private boolean shouldSave;
    @Getter
    @Setter
    private Identifier styleKey;
    @Getter
    private String namespace;
    private ViewSaveManager viewSaveManager;
    @Getter
    private CursorPolicy cursorPolicy;
    private boolean cursorPolicyExplicit;

    protected View() {
        this.id = deriveDefaultId();
        this.cursorPolicy = CursorPolicies.empty();
        this.cursorPolicyExplicit = false;
    }

    protected View(String id) {
        this();
        setId(id);
    }

    protected View(String id, boolean shouldSave) {
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

    public StyleDelta configureStyleDelta() {
        return null;
    }

    public StyleDescriptor configureBaseStyle(StyleDescriptor descriptor) {
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

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            this.id = deriveDefaultId();
            return;
        }
        this.id = id;
    }

    public void setCursorPolicy(CursorPolicy cursorPolicy) {
        boolean explicit = cursorPolicy != null;
        updateCursorPolicy(cursorPolicy, explicit);
    }

    public boolean hasExplicitCursorPolicy() {
        return cursorPolicyExplicit;
    }

    public void applyDefaultCursorPolicy(CursorPolicy defaultPolicy) {
        if (cursorPolicyExplicit) {
            return;
        }
        updateCursorPolicy(defaultPolicy, false);
    }

    private void updateCursorPolicy(CursorPolicy nextPolicy, boolean explicit) {
        CursorPolicy resolved = nextPolicy != null ? nextPolicy : CursorPolicies.empty();
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
}
