package tytoo.minegui.component.components.display;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ColorUtils;
import tytoo.minegui.utils.ImGuiImageUtils;

import java.util.Objects;

public final class MGImage extends MGComponent<MGImage>
        implements Sizable<MGImage>, Stateful<Identifier, MGImage> {

    private static final ComponentPool<MGImage> POOL = new ComponentPool<>(MGImage::new, MGImage::prepare);
    private final float[] tintColor = new float[4];
    private final float[] borderColor = new float[4];
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
    private float borderThickness;
    private boolean loggedFailure;
    @Nullable
    private Identifier failureId;

    private MGImage() {
        prepare();
    }

    public static MGImage of(Identifier identifier) {
        MGImage image = POOL.acquire();
        image.identifier = identifier;
        return image;
    }

    public static MGImage of(String namespace, String path) {
        return of(Identifier.of(namespace, path));
    }

    public static MGImage of(State<Identifier> state) {
        MGImage image = POOL.acquire();
        image.setState(state);
        return image;
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

    private static int toImColor(float[] color) {
        float r = ColorUtils.clampUnit(color[0]);
        float g = ColorUtils.clampUnit(color[1]);
        float b = ColorUtils.clampUnit(color[2]);
        float a = ColorUtils.clampUnit(color[3]);
        return ImColor.rgba(r, g, b, a);
    }

    private void prepare() {
        identifier = null;
        identifierState = null;
        setColor(tintColor, 1.0f, 1.0f, 1.0f, 1.0f);
        setColor(borderColor, 0.0f, 0.0f, 0.0f, 0.0f);
        uv0x = 0.0f;
        uv0y = 0.0f;
        uv1x = 1.0f;
        uv1y = 1.0f;
        rotation = 0;
        parity = false;
        borderThickness = Float.NaN;
        loggedFailure = false;
        failureId = null;
    }

    public MGImage identifier(Identifier identifier) {
        this.identifier = identifier;
        return self();
    }

    public MGImage identifier(String namespace, String path) {
        return identifier(Identifier.of(namespace, path));
    }

    public MGImage rotation(int rotation) {
        this.rotation = Math.floorMod(rotation, 4);
        return self();
    }

    public MGImage parity(boolean parity) {
        this.parity = parity;
        return self();
    }

    public MGImage tint(float r, float g, float b, float a) {
        setColor(tintColor, r, g, b, a);
        return self();
    }

    public MGImage tint(float[] rgba) {
        if (rgba == null) {
            return resetTint();
        }
        setColor(tintColor, value(rgba, 0, 1.0f), value(rgba, 1, 1.0f), value(rgba, 2, 1.0f), value(rgba, 3, 1.0f));
        return self();
    }

    public MGImage tint(int rgba) {
        ColorUtils.unpackRgba(rgba, tintColor);
        return self();
    }

    public MGImage resetTint() {
        setColor(tintColor, 1.0f, 1.0f, 1.0f, 1.0f);
        return self();
    }

    public MGImage borderColor(float r, float g, float b, float a) {
        setColor(borderColor, r, g, b, a);
        return self();
    }

    public MGImage borderColor(float[] rgba) {
        if (rgba == null) {
            return resetBorder();
        }
        setColor(borderColor, value(rgba, 0, 0.0f), value(rgba, 1, 0.0f), value(rgba, 2, 0.0f), value(rgba, 3, 0.0f));
        return self();
    }

    public MGImage borderColor(int rgba) {
        ColorUtils.unpackRgba(rgba, borderColor);
        return self();
    }

    public MGImage resetBorder() {
        setColor(borderColor, 0.0f, 0.0f, 0.0f, 0.0f);
        return self();
    }

    public MGImage borderThickness(float thickness) {
        borderThickness = thickness;
        return self();
    }

    public MGImage uv(float u0, float v0, float u1, float v1) {
        uv0x = u0;
        uv0y = v0;
        uv1x = u1;
        uv1y = v1;
        return self();
    }

    public MGImage uv0(float u, float v) {
        uv0x = u;
        uv0y = v;
        return self();
    }

    public MGImage uv1(float u, float v) {
        uv1x = u;
        uv1y = v;
        return self();
    }

    public MGImage resetUv() {
        uv0x = 0.0f;
        uv0y = 0.0f;
        uv1x = 1.0f;
        uv1y = 1.0f;
        return self();
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
            if (!loggedFailure || !Objects.equals(failureId, resolved)) {
                MineGuiCore.LOGGER.warn("Failed to load texture {} for MGImage", resolved, exception);
                loggedFailure = true;
                failureId = resolved;
            }
            return;
        }

        float baselineWidth = Math.max(1.0f, info.width());
        float baselineHeight = Math.max(1.0f, info.height());

        withLayout(baselineWidth, baselineHeight, (width, height) -> {
            ImVec2 cursor = ImGui.getCursorScreenPos();
            float x1 = cursor.x;
            float y1 = cursor.y;
            float x2 = x1 + width;
            float y2 = y1 + height;

            int tint = toImColor(tintColor);
            ImGuiImageUtils.drawImage(resolved, x1, y1, x2, y2, rotation, parity, uv0x, uv0y, uv1x, uv1y, tint);

            float borderAlpha = ColorUtils.clampUnit(borderColor[3]);
            float thickness = resolveBorderThickness();
            if (borderAlpha > 0.0f && thickness > 0.0f) {
                int border = toImColor(borderColor);
                ImGui.getWindowDrawList().addRect(x1, y1, x2, y2, border, 0.0f, 0, thickness);
            }

            ImGui.dummy(width, height);
        });

        renderChildren();
    }

    private float resolveBorderThickness() {
        if (Float.isNaN(borderThickness)) {
            return ImGui.getStyle().getFrameBorderSize();
        }
        return borderThickness;
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
}
