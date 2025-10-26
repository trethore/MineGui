package tytoo.minegui.style;

import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.MineGuiCore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MGFontLibrary {
    private static final MGFontLibrary INSTANCE = new MGFontLibrary();
    private static final Identifier DEFAULT_FONT_KEY = Identifier.of(MineGuiCore.ID, "default");

    private final ConcurrentHashMap<FontVariant, ImFont> loadedFonts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Identifier, FontDescriptor> fontDescriptors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Identifier, Boolean> warnedPostBuild = new ConcurrentHashMap<>();

    private MGFontLibrary() {
    }

    public static MGFontLibrary getInstance() {
        return INSTANCE;
    }

    private static float normalizeSize(float size) {
        return Math.round(size * 100.0f) / 100.0f;
    }

    public Identifier getDefaultFontKey() {
        return DEFAULT_FONT_KEY;
    }

    public void registerFont(Identifier key, FontDescriptor descriptor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(descriptor, "descriptor");
        fontDescriptors.put(key, descriptor);
    }

    public ImFont ensureFont(Identifier key, @Nullable Float sizeOverride) {
        Identifier effectiveKey = key != null ? key : DEFAULT_FONT_KEY;
        FontDescriptor descriptor = fontDescriptors.get(effectiveKey);
        Identifier descriptorKey = effectiveKey;
        if (descriptor == null) {
            if (!effectiveKey.equals(DEFAULT_FONT_KEY)) {
                MineGuiCore.LOGGER.warn("Font descriptor not found for key {}", effectiveKey);
            }
            descriptorKey = DEFAULT_FONT_KEY;
            descriptor = fontDescriptors.get(descriptorKey);
            if (descriptor == null) {
                return null;
            }
        }
        float targetSize = sizeOverride != null ? sizeOverride : descriptor.size();
        targetSize = sanitizeRequestedSize(targetSize, descriptor.size());
        float normalizedSize = normalizeSize(targetSize);
        ImFont cached = findCachedFont(descriptorKey, normalizedSize);
        if (cached != null) {
            return cached;
        }
        return loadFont(descriptorKey, descriptor, normalizedSize);
    }

    public void clear() {
        loadedFonts.clear();
        fontDescriptors.clear();
    }

    private ImFont loadFont(Identifier key, FontDescriptor descriptor, float size) {
        FontVariant variant = new FontVariant(key, size);
        ImFont cached = loadedFonts.get(variant);
        if (cached != null) {
            return cached;
        }
        ImGuiIO io = ImGui.getIO();
        if (io.getFonts().isBuilt()) {
            if (warnedPostBuild.putIfAbsent(key, Boolean.TRUE) == null) {
                MineGuiCore.LOGGER.warn("Skipping font load for {} at runtime; register fonts before frame or trigger atlas rebuild.", key);
            }
            return cached;
        }
        float sanitizedSize = size > 0f ? size : sanitizeRequestedSize(size, descriptor.size());
        if (sanitizedSize <= 0f) {
            MineGuiCore.LOGGER.error("Unable to resolve positive font size for {}; skipping load", key);
            return cached;
        }
        ImFont font = descriptor.load(sanitizedSize);
        if (font != null) {
            loadedFonts.put(new FontVariant(key, sanitizedSize), font);
        }
        return font;
    }

    private ImFont findCachedFont(Identifier key, float size) {
        FontVariant direct = new FontVariant(key, size);
        ImFont cached = loadedFonts.get(direct);
        if (cached != null) {
            return cached;
        }
        for (var entry : loadedFonts.entrySet()) {
            FontVariant variant = entry.getKey();
            if (variant.key.equals(key) && Math.abs(variant.size - size) < 0.05f) {
                return entry.getValue();
            }
        }
        return null;
    }

    private float sanitizeRequestedSize(float requested, float fallback) {
        float candidate = requested;
        if (!Float.isFinite(candidate) || candidate <= 0f) {
            candidate = fallback;
        }
        if (!Float.isFinite(candidate) || candidate <= 0f) {
            candidate = 16.0f;
        }
        return candidate;
    }

    @FunctionalInterface
    public interface FontConfigFactory {
        void configure(ImFontConfig config);
    }

    @FunctionalInterface
    public interface FontSource {
        static FontSource asset(String relativePath) {
            return () -> {
                String formatted = "assets/" + MineGuiCore.ID + "/fonts/" + relativePath;
                try (InputStream stream = MineGuiCore.class.getClassLoader().getResourceAsStream(formatted)) {
                    if (stream == null) {
                        MineGuiCore.LOGGER.warn("Font asset not found: {}", formatted);
                        return null;
                    }
                    return stream.readAllBytes();
                } catch (IOException e) {
                    MineGuiCore.LOGGER.error("Failed to load font asset {}", formatted, e);
                    return null;
                }
            };
        }

        static FontSource external(Path path) {
            return () -> {
                if (path == null) {
                    return null;
                }
                Path resolved = FabricLoader.getInstance().getGameDir().resolve(path);
                if (!Files.exists(resolved)) {
                    MineGuiCore.LOGGER.warn("Font file not found: {}", resolved);
                    return null;
                }
                try {
                    return Files.readAllBytes(resolved);
                } catch (IOException e) {
                    MineGuiCore.LOGGER.error("Failed to read font file {}", resolved, e);
                    return null;
                }
            };
        }

        byte[] resolve();
    }

    public record FontDescriptor(FontSource source, float size, @Nullable FontConfigFactory configFactory) {
        public FontDescriptor {
            Objects.requireNonNull(source, "source");
        }

        ImFont load(float targetSize) {
            ImGuiIO io = ImGui.getIO();
            byte[] fontBytes = source.resolve();
            if (fontBytes == null || fontBytes.length == 0) {
                MineGuiCore.LOGGER.warn("Font source returned no data");
                return null;
            }
            ImFontConfig config = new ImFontConfig();
            try {
                if (configFactory != null) {
                    configFactory.configure(config);
                }
                return io.getFonts().addFontFromMemoryTTF(fontBytes, targetSize, config);
            } finally {
                config.destroy();
            }
        }
    }

    private record FontVariant(Identifier key, float size) {
    }
}
