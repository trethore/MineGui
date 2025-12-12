package tytoo.minegui.view;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.util.MinecraftIdentifiers;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui.view.cursor.CursorPolicy;

public abstract class View {
    @Getter
    private boolean visible;
    @Getter
    private String id;
    @Getter
    @Setter
    private ResourceId styleKey;
    @Getter
    private CursorPolicy cursorPolicy;
    @Getter
    @Setter
    private boolean persistentLayout = true;
    @Getter
    @Setter
    private boolean persistentStyle = true;
    private boolean cursorPolicyExplicit;

    protected View(String id) {
        initializeView(id);
    }

    protected View() {
        this(null);
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
        return base + "##" + id;
    }

    public void attach() {
    }

    public void detach() {
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
        }
    }

    public void setId(String id) {
        this.id = normalizeId(id);
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

    public View useStyle(ResourceId key) {
        setStyleKey(key);
        return this;
    }

    public View useStyle(Identifier identifier) {
        if (identifier == null) {
            setStyleKey(null);
            return this;
        }
        return useStyle(MinecraftIdentifiers.fromMinecraft(identifier));
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

    protected final void renderSection(ViewSection section) {
        if (section == null) {
            return;
        }
        section.render(this);
    }

    private void initializeView(String requestedId) {
        this.id = normalizeId(requestedId);
        this.cursorPolicy = CursorPolicies.empty();
        this.cursorPolicyExplicit = false;
    }

    private String normalizeId(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return deriveDefaultId();
        }
        return candidate;
    }
}
