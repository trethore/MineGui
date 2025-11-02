package tytoo.minegui.style;

import imgui.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.MineGuiCore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FontLibrary {
    private static final FontLibrary INSTANCE = new FontLibrary();
    private static final Identifier DEFAULT_FONT_KEY = Identifier.of(MineGuiCore.ID, "default");

    private final ConcurrentHashMap<FontVariant, ImFont> loadedFonts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Identifier, FontDescriptor> fontDescriptors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Identifier, Boolean> warnedPostBuild = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Identifier, Identifier> mergeParents = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FontVariant, byte[]> fontData = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<Identifier>> loadingKeys = ThreadLocal.withInitial(HashSet::new);
    private volatile boolean registrationLocked;

    private FontLibrary() {
    }

    public static FontLibrary getInstance() {
        return INSTANCE;
    }

    private static float normalizeSize(float size) {
        return Math.round(size * 100.0f) / 100.0f;
    }

    public Identifier getDefaultFontKey() {
        return DEFAULT_FONT_KEY;
    }

    public void registerFont(Identifier key, FontDescriptor descriptor) {
        if (registrationLocked) {
            MineGuiCore.LOGGER.error("Ignoring font registration for {} after MineGui initialization; register fonts during mod startup.", key);
            return;
        }
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(descriptor, "descriptor");
        mergeParents.remove(key);
        fontDescriptors.put(key, descriptor);
    }

    public void registerMergedFont(Identifier baseKey, Identifier key, FontSource source, float size) {
        registerMergedFont(baseKey, key, source, size, null, null);
    }

    public void registerMergedFont(
            Identifier baseKey,
            Identifier key,
            FontSource source,
            float size,
            @Nullable GlyphRangeSupplier glyphRanges,
            @Nullable FontConfigFactory extraConfig
    ) {
        if (registrationLocked) {
            MineGuiCore.LOGGER.error("Ignoring merged font registration for {} after MineGui initialization; register fonts during mod startup.", key);
            return;
        }
        Objects.requireNonNull(baseKey, "baseKey");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(source, "source");
        FontDescriptor descriptor = new FontDescriptor(source, size, config -> {
            config.setMergeMode(true);
            if (glyphRanges != null) {
                short[] ranges = glyphRanges.supply(ImGui.getIO().getFonts());
                if (ranges != null && ranges.length > 0) {
                    config.setGlyphRanges(ranges);
                }
            }
            if (extraConfig != null) {
                extraConfig.configure(config);
            }
        });
        if (!Objects.equals(baseKey, DEFAULT_FONT_KEY) && !fontDescriptors.containsKey(baseKey)) {
            MineGuiCore.LOGGER.warn("Registering merged font {} before base {}; ensure the base font is available", key, baseKey);
        }
        registerFont(key, descriptor);
        mergeParents.put(key, baseKey);
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
        FontVariant targetVariant = new FontVariant(descriptorKey, normalizedSize);
        ImFont cached = findCachedFont(descriptorKey, normalizedSize);
        if (cached != null) {
            return cached;
        }
        return loadFont(targetVariant, descriptor);
    }

    private ImFont loadFont(FontVariant variant, FontDescriptor descriptor) {
        ImFont cached = loadedFonts.get(variant);
        if (cached != null) {
            return cached;
        }
        Set<Identifier> stack = loadingKeys.get();
        if (!stack.add(variant.key())) {
            MineGuiCore.LOGGER.error("Detected recursive font load for {}; aborting", variant.key());
            return null;
        }
        try {
            Identifier baseKey = mergeParents.get(variant.key());
            if (baseKey != null && !baseKey.equals(variant.key())) {
                ImFont baseFont = ensureFont(baseKey, null);
                if (baseFont == null) {
                    MineGuiCore.LOGGER.warn("Unable to merge font {} because base {} failed to load", variant.key(), baseKey);
                    return null;
                }
            }
            ImGuiIO io = ImGui.getIO();
            if (io.getFonts().isBuilt()) {
                if (warnedPostBuild.putIfAbsent(variant.key(), Boolean.TRUE) == null) {
                    MineGuiCore.LOGGER.warn("Skipping font load for {} at runtime; register fonts during mod startup before MineGui initializes.", variant.key());
                }
                return null;
            }
            float sanitizedSize = variant.size() > 0f ? variant.size() : sanitizeRequestedSize(variant.size(), descriptor.size());
            if (sanitizedSize <= 0f) {
                MineGuiCore.LOGGER.error("Unable to resolve positive font size for {}; skipping load", variant.key());
                return null;
            }
            float normalizedSize = normalizeSize(sanitizedSize);
            FontVariant normalizedVariant = new FontVariant(variant.key(), normalizedSize);
            ImFont font = descriptor.load(this, normalizedVariant, normalizedSize);
            if (font != null) {
                loadedFonts.put(normalizedVariant, font);
            }
            return font;
        } finally {
            stack.remove(variant.key());
        }
    }

    public void clear() {
        loadedFonts.clear();
        fontDescriptors.clear();
        warnedPostBuild.clear();
        mergeParents.clear();
        fontData.clear();
    }

    public void resetRuntime() {
        loadedFonts.clear();
        warnedPostBuild.clear();
        fontData.clear();
    }

    public void lockRegistration() {
        registrationLocked = true;
    }

    public boolean isRegistrationLocked() {
        return registrationLocked;
    }

    public void preloadRegisteredFonts() {
        for (Map.Entry<Identifier, FontDescriptor> entry : fontDescriptors.entrySet()) {
            FontDescriptor descriptor = entry.getValue();
            if (descriptor != null) {
                ensureFont(entry.getKey(), descriptor.size());
            }
        }
    }

    private ImFont findCachedFont(Identifier key, float size) {
        FontVariant direct = new FontVariant(key, size);
        ImFont cached = loadedFonts.get(direct);
        if (cached != null) {
            return cached;
        }
        for (Map.Entry<FontVariant, ImFont> entry : loadedFonts.entrySet()) {
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
    public interface GlyphRangeSupplier {
        short[] supply(ImFontAtlas atlas);
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

        ImFont load(FontLibrary library, FontVariant variant, float targetSize) {
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
                config.setFontDataOwnedByAtlas(false);
                library.fontData.put(variant, fontBytes);
                return io.getFonts().addFontFromMemoryTTF(fontBytes, targetSize, config);
            } finally {
                config.destroy();
            }
        }
    }

    private record FontVariant(Identifier key, float size) {
    }
}
