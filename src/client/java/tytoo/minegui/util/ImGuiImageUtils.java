package tytoo.minegui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ImGuiImageUtils {
    private static final Map<Identifier, TextureEntry> CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<float[]> COLOR_BUFFER = ThreadLocal.withInitial(() -> new float[4]);

    private ImGuiImageUtils() {
    }

    public static int textureId(Identifier identifier) {
        return ensureTexture(identifier).textureId();
    }

    public static TextureInfo getTextureInfo(Identifier identifier) {
        TextureEntry entry = ensureTexture(identifier);
        return entry.info();
    }

    public static void drawImage(Identifier identifier, float x1, float y1, float x2, float y2,
                                 int rotation, boolean parity, float u0, float v0, float u1, float v1, float[] color) {
        TextureEntry entry = ensureTexture(identifier);
        float[][] uvs = computeUvs(rotation, parity, u0, v0, u1, v1);
        float[] rgba = COLOR_BUFFER.get();
        Arrays.fill(rgba, 0.0f);
        int components = color != null ? color.length : 0;
        ColorUtils.toRgba(color, components, rgba);
        int tint = ImColor.rgba(rgba[0], rgba[1], rgba[2], rgba[3]);
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addImageQuad(
                entry.textureId(),
                x1, y1,
                x2, y1,
                x2, y2,
                x1, y2,
                uvs[0][0], uvs[0][1],
                uvs[1][0], uvs[1][1],
                uvs[2][0], uvs[2][1],
                uvs[3][0], uvs[3][1],
                tint
        );
    }

    public static void drawImage(Identifier identifier, float x1, float y1, float x2, float y2,
                                 int rotation, boolean parity, float[] color) {
        drawImage(identifier, x1, y1, x2, y2, rotation, parity, 0.0f, 0.0f, 1.0f, 1.0f, color);
    }

    public static void drawImage(Identifier identifier, float x1, float y1, float x2, float y2,
                                 int rotation, boolean parity, float u0, float v0, float u1, float v1, int tint) {
        TextureEntry entry = ensureTexture(identifier);
        float[][] uvs = computeUvs(rotation, parity, u0, v0, u1, v1);
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addImageQuad(
                entry.textureId(),
                x1, y1,
                x2, y1,
                x2, y2,
                x1, y2,
                uvs[0][0], uvs[0][1],
                uvs[1][0], uvs[1][1],
                uvs[2][0], uvs[2][1],
                uvs[3][0], uvs[3][1],
                tint
        );
    }

    public static void drawImage(Identifier identifier, float x1, float y1, float x2, float y2, int rotation, boolean parity, int tint) {
        drawImage(identifier, x1, y1, x2, y2, rotation, parity, 0.0f, 0.0f, 1.0f, 1.0f, tint);
    }

    public static void invalidateAll() {
        if (RenderSystem.isOnRenderThreadOrInit()) {
            flush();
        } else {
            RenderSystem.recordRenderCall(ImGuiImageUtils::flush);
        }
    }

    private static TextureEntry ensureTexture(Identifier identifier) {
        Objects.requireNonNull(identifier, "identifier");
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            throw new IllegalStateException("ImGui images can only be used on the render thread");
        }
        return CACHE.computeIfAbsent(identifier, ImGuiImageUtils::loadTexture);
    }

    private static TextureEntry loadTexture(Identifier identifier) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            throw new IllegalStateException("Minecraft client is not available");
        }
        ResourceManager resourceManager = client.getResourceManager();
        Optional<Resource> resource = resourceManager.getResource(identifier);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Texture not found: " + identifier);
        }
        Resource res = resource.get();
        try (InputStream stream = res.getInputStream()) {
            NativeImage image = NativeImage.read(stream);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            texture.setFilter(false, false);
            texture.bindTexture();
            int glId = texture.getGlId();
            TextureInfo info = new TextureInfo(glId, image.getWidth(), image.getHeight());
            return new TextureEntry(texture, info);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load texture: " + identifier, e);
        }
    }

    private static float[][] computeUvs(int rotation, boolean parity, float u0, float v0, float u1, float v1) {
        float[][] coords = new float[][]{
                {u0, v0},
                {u1, v0},
                {u1, v1},
                {u0, v1}
        };
        int steps = Math.floorMod(rotation, 4);
        for (int i = 0; i < steps; i++) {
            float[] last = coords[3];
            coords[3] = coords[2];
            coords[2] = coords[1];
            coords[1] = coords[0];
            coords[0] = last;
        }
        if (parity) {
            float[] tmp = coords[1];
            coords[1] = coords[0];
            coords[0] = tmp;
            tmp = coords[3];
            coords[3] = coords[2];
            coords[2] = tmp;
        }
        return coords;
    }

    private static void flush() {
        CACHE.values().forEach(TextureEntry::close);
        CACHE.clear();
    }

    public record TextureInfo(int textureId, int width, int height) {
    }

    private record TextureEntry(NativeImageBackedTexture texture, TextureInfo info) {
        int textureId() {
            return info.textureId();
        }

        public TextureInfo info() {
            return info;
        }

        void close() {
            texture.close();
        }
    }
}
