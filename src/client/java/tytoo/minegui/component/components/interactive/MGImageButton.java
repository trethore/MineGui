package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Clickable;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ColorUtils;
import tytoo.minegui.utils.ImGuiImageUtils;

public final class MGImageButton extends MGComponent<MGImageButton>
        implements Sizable<MGImageButton>, Stateful<Identifier, MGImageButton>, Disableable<MGImageButton>, Clickable<MGImageButton> {

    private static final ComponentPool<MGImageButton> POOL = new ComponentPool<>(MGImageButton::new, MGImageButton::prepare);
    private final float[] tintColor = new float[4];
    private final float[] backgroundColor = new float[4];
    @Nullable
    private Identifier identifier;
    @Nullable
    private State<Identifier> identifierState;
    private float uv0x;
    private float uv0y;
    private float uv1x;
    private float uv1y;
    private int rotation;
    private boolean parity;
    private int framePadding;
    private boolean disabled;
    @Nullable
    private Runnable onClick;
    private boolean loggedFailure;
    @Nullable
    private Identifier failureId;

    private MGImageButton() {
        prepare();
    }

    public static MGImageButton of(Identifier identifier) {
        MGImageButton button = POOL.acquire();
        button.identifier = identifier;
        return button;
    }

    public static MGImageButton of(String namespace, String path) {
        return of(Identifier.of(namespace, path));
    }

    public static MGImageButton of(State<Identifier> state) {
        MGImageButton button = POOL.acquire();
        button.setState(state);
        return button;
    }

    private static void setColor(float[] target, float r, float g, float b, float a) {
        target[0] = ColorUtils.clampUnit(r);
        target[1] = ColorUtils.clampUnit(g);
        target[2] = ColorUtils.clampUnit(b);
        target[3] = ColorUtils.clampUnit(a);
    }

    private static float value(float[] array, int index, float fallback) {
        if (array == null || index < 0 || index >= array.length) {
            return fallback;
        }
        return array[index];
    }

    private void prepare() {
        identifier = null;
        identifierState = null;
        setColor(tintColor, 1.0f, 1.0f, 1.0f, 1.0f);
        setColor(backgroundColor, 0.0f, 0.0f, 0.0f, 0.0f);
        uv0x = 0.0f;
        uv0y = 0.0f;
        uv1x = 1.0f;
        uv1y = 1.0f;
        rotation = 0;
        parity = false;
        framePadding = -1;
        disabled = false;
        onClick = null;
        loggedFailure = false;
        failureId = null;
    }

    public MGImageButton identifier(Identifier identifier) {
        this.identifier = identifier;
        return self();
    }

    public MGImageButton identifier(String namespace, String path) {
        return identifier(Identifier.of(namespace, path));
    }

    public MGImageButton rotation(int rotation) {
        this.rotation = Math.floorMod(rotation, 4);
        return self();
    }

    public MGImageButton parity(boolean parity) {
        this.parity = parity;
        return self();
    }

    public MGImageButton uv(float u0, float v0, float u1, float v1) {
        uv0x = u0;
        uv0y = v0;
        uv1x = u1;
        uv1y = v1;
        return self();
    }

    public MGImageButton uv0(float u, float v) {
        uv0x = u;
        uv0y = v;
        return self();
    }

    public MGImageButton uv1(float u, float v) {
        uv1x = u;
        uv1y = v;
        return self();
    }

    public MGImageButton resetUv() {
        uv0x = 0.0f;
        uv0y = 0.0f;
        uv1x = 1.0f;
        uv1y = 1.0f;
        return self();
    }

    public MGImageButton tint(float r, float g, float b, float a) {
        setColor(tintColor, r, g, b, a);
        return self();
    }

    public MGImageButton tint(float[] rgba) {
        if (rgba == null) {
            return resetTint();
        }
        setColor(tintColor, value(rgba, 0, 1.0f), value(rgba, 1, 1.0f), value(rgba, 2, 1.0f), value(rgba, 3, 1.0f));
        return self();
    }

    public MGImageButton tint(int rgba) {
        ColorUtils.unpackRgba(rgba, tintColor);
        return self();
    }

    public MGImageButton resetTint() {
        setColor(tintColor, 1.0f, 1.0f, 1.0f, 1.0f);
        return self();
    }

    public MGImageButton backgroundColor(float r, float g, float b, float a) {
        setColor(backgroundColor, r, g, b, a);
        return self();
    }

    public MGImageButton backgroundColor(float[] rgba) {
        if (rgba == null) {
            return resetBackground();
        }
        setColor(backgroundColor, value(rgba, 0, 0.0f), value(rgba, 1, 0.0f), value(rgba, 2, 0.0f), value(rgba, 3, 0.0f));
        return self();
    }

    public MGImageButton backgroundColor(int rgba) {
        ColorUtils.unpackRgba(rgba, backgroundColor);
        return self();
    }

    public MGImageButton resetBackground() {
        setColor(backgroundColor, 0.0f, 0.0f, 0.0f, 0.0f);
        return self();
    }

    public MGImageButton framePadding(int padding) {
        framePadding = padding;
        return self();
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }

    @Override
    public void setOnClick(@Nullable Runnable action) {
        onClick = action;
    }

    @Override
    @Nullable
    public State<Identifier> getState() {
        return identifierState;
    }

    @Override
    public void setState(@Nullable State<Identifier> state) {
        identifierState = state;
    }

    @Override
    protected void renderComponent() {
        Identifier resolved = resolveIdentifier();
        if (resolved == null) {
            return;
        }
        ImGuiImageUtils.TextureInfo info;
        try {
            info = ImGuiImageUtils.getTextureInfo(resolved);
            loggedFailure = false;
            failureId = null;
        } catch (RuntimeException exception) {
            if (!loggedFailure || failureId == null || !failureId.equals(resolved)) {
                MineGuiCore.LOGGER.warn("Failed to load texture {} for MGImageButton", resolved, exception);
                loggedFailure = true;
                failureId = resolved;
            }
            return;
        }

        float baselineWidth = Math.max(1.0f, info.width());
        float baselineHeight = Math.max(1.0f, info.height());

        withLayout(baselineWidth, baselineHeight, (width, height) -> {
            if (width <= 0.0f || height <= 0.0f) {
                ImGui.dummy(width, height);
                return;
            }
            if (disabled) {
                ImGui.beginDisabled(true);
            }
            int textureId = ImGuiImageUtils.textureId(resolved);
            float useUv0x = uv0x;
            float useUv0y = uv0y;
            float useUv1x = uv1x;
            float useUv1y = uv1y;
            boolean pressed;
            try {
                pressed = ImGui.imageButton(
                        textureId,
                        width,
                        height,
                        useUv0x,
                        useUv0y,
                        useUv1x,
                        useUv1y,
                        framePadding,
                        backgroundColor[0],
                        backgroundColor[1],
                        backgroundColor[2],
                        backgroundColor[3],
                        0.0f,
                        0.0f,
                        0.0f,
                        0.0f
                );
            } finally {
                if (disabled) {
                    ImGui.endDisabled();
                }
            }
            ImVec2 min = ImGui.getItemRectMin();
            ImVec2 max = ImGui.getItemRectMax();
            float pad = framePadding >= 0 ? framePadding : ImGui.getStyle().getFramePaddingX();
            float padY = framePadding >= 0 ? framePadding : ImGui.getStyle().getFramePaddingY();
            float contentX1 = min.x + pad;
            float contentY1 = min.y + padY;
            float contentX2 = max.x - pad;
            float contentY2 = max.y - padY;
            if (contentX2 < contentX1) {
                contentX2 = contentX1;
            }
            if (contentY2 < contentY1) {
                contentY2 = contentY1;
            }
            ImGuiImageUtils.drawImage(resolved, contentX1, contentY1, contentX2, contentY2, rotation, parity, useUv0x, useUv0y, useUv1x, useUv1y, tintColor);
            if (pressed && !disabled) {
                performClick();
            }
        });
    }

    @Nullable
    private Identifier resolveIdentifier() {
        if (identifierState != null) {
            Identifier stateValue = identifierState.get();
            if (stateValue != null) {
                return stateValue;
            }
        }
        return identifier;
    }

    @Override
    public MGImageButton self() {
        return this;
    }
}
