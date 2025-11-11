package tytoo.minegui.view;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.manager.ViewSaveManager;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.util.MinecraftIdentifiers;
import tytoo.minegui.util.NamespaceIds;
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
    private boolean persistent = true;
    @Getter
    @Setter
    private ResourceId styleKey;
    @Getter
    private String namespace;
    private ViewSaveManager viewSaveManager;
    @Getter
    private CursorPolicy cursorPolicy;
    private boolean cursorPolicyExplicit;
    private boolean layoutNamespaceWarningLogged;
    private boolean layoutContextWarningLogged;
    private LayoutApi layoutHandle;

    protected View(String namespace, String path, boolean persistent) {
        initializeView(namespacedId(namespace, path), persistent);
    }

    protected View(String namespace, String path) {
        this(namespace, path, true);
    }

    protected View(String id, boolean persistent) {
        initializeView(id, persistent);
    }

    protected View(String id) {
        this(id, true);
    }

    protected View() {
        this(null, true);
    }

    public static String namespacedId(String namespace, String path) {
        return NamespaceIds.make(namespace, path);
    }

    public final void render() {
        if (!visible) {
            return;
        }
        LayoutApi layout = layout();
        if (layout == null) {
            return;
        }
        renderView(layout);
    }

    protected abstract void renderView(LayoutApi layout);

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
        this.layoutHandle = null;
        resetLayoutWarnings();
    }

    public void detach() {
        this.namespace = null;
        this.viewSaveManager = null;
        this.layoutHandle = null;
        resetLayoutWarnings();
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
            if (persistent && viewSaveManager != null) {
                viewSaveManager.requestSave();
            }
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

    public View persistent(boolean persistent) {
        setPersistent(persistent);
        return this;
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
        LayoutApi layout = layout();
        if (layout == null) {
            return;
        }
        section.render(this, layout);
    }

    protected final void renderSection(ViewSection section, LayoutApi layout) {
        if (section == null || layout == null) {
            return;
        }
        section.render(this, layout);
    }

    public final LayoutApi layout() {
        LayoutApi cached = layoutHandle;
        if (cached != null) {
            return cached;
        }
        LayoutApi resolved = resolveLayout();
        if (resolved != null) {
            layoutHandle = resolved;
        }
        return resolved;
    }

    private LayoutApi resolveLayout() {
        String currentNamespace = namespace;
        if (currentNamespace == null || currentNamespace.isBlank()) {
            if (!layoutNamespaceWarningLogged) {
                MineGuiCore.LOGGER.warn("View '{}' requested the layout API before attaching to a namespace; returning null.", getClass().getName());
                layoutNamespaceWarningLogged = true;
            }
            return null;
        }
        MineGuiNamespaceContext context = MineGuiNamespaces.get(currentNamespace);
        if (context == null) {
            if (!layoutContextWarningLogged) {
                MineGuiCore.LOGGER.warn("View '{}' could not resolve layout API because namespace '{}' is not registered.", getClass().getName(), currentNamespace);
                layoutContextWarningLogged = true;
            }
            return null;
        }
        layoutNamespaceWarningLogged = false;
        layoutContextWarningLogged = false;
        return context.layout();
    }

    private void resetLayoutWarnings() {
        layoutNamespaceWarningLogged = false;
        layoutContextWarningLogged = false;
    }

    private void initializeView(String requestedId, boolean persistent) {
        this.id = normalizeId(requestedId);
        this.cursorPolicy = CursorPolicies.empty();
        this.cursorPolicyExplicit = false;
        this.persistent = persistent;
    }

    private String normalizeId(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return deriveDefaultId();
        }
        return candidate;
    }
}
