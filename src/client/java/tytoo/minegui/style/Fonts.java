package tytoo.minegui.style;

import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImGuiIO;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;

import java.util.Objects;
import java.util.function.Consumer;

public final class Fonts {
    private static final Identifier PROXIMA_KEY = Identifier.of(MineGuiCore.ID, "proxima");
    private static final Identifier JETBRAINS_MONO_KEY = Identifier.of(MineGuiCore.ID, "jetbrains-mono");
    private static final Identifier NOTO_SANS_KEY = Identifier.of(MineGuiCore.ID, "noto-sans");
    private static final float PROXIMA_SIZE = 20.0f;
    private static final float JETBRAINS_MONO_SIZE = 18.0f;
    private static final float NOTO_SANS_SIZE = 18.0f;

    private Fonts() {
    }

    public static Identifier proxima() {
        return PROXIMA_KEY;
    }

    public static Identifier jetbrainsMono() {
        return JETBRAINS_MONO_KEY;
    }

    public static Identifier notoSans() {
        return NOTO_SANS_KEY;
    }

    public static float proximaSize() {
        return PROXIMA_SIZE;
    }

    public static float jetbrainsMonoSize() {
        return JETBRAINS_MONO_SIZE;
    }

    public static float notoSansSize() {
        return NOTO_SANS_SIZE;
    }

    public static void registerDefaults(ImGuiIO io) {
        Objects.requireNonNull(io, "io");
        FontLibrary library = FontLibrary.getInstance();
        Consumer<ImFontConfig> configureCyrillic = config -> {
            config.setPixelSnapH(true);
            config.setGlyphRanges(io.getFonts().getGlyphRangesCyrillic());
        };
        registerFont(library, library.getDefaultFontKey(), "proxima.ttf", PROXIMA_SIZE, configureCyrillic);
        registerFont(library, PROXIMA_KEY, "proxima.ttf", PROXIMA_SIZE, configureCyrillic);
        registerFont(library, JETBRAINS_MONO_KEY, "jetbrains-mono.ttf", JETBRAINS_MONO_SIZE, configureCyrillic);
        registerFont(library, NOTO_SANS_KEY, "notosans.ttf", NOTO_SANS_SIZE, configureCyrillic);
    }

    public static ImFont ensure(Identifier key) {
        Objects.requireNonNull(key, "key");
        FontLibrary library = FontLibrary.getInstance();
        return library.ensureFont(key, resolveDefaultSize(key, library.getDefaultFontKey()));
    }

    public static ImFont ensureJetbrainsMono() {
        return ensure(JETBRAINS_MONO_KEY);
    }

    private static void registerFont(FontLibrary library, Identifier key, String assetPath, float size, Consumer<ImFontConfig> configurer) {
        Objects.requireNonNull(library, "library");
        library.registerFont(
                key,
                new FontLibrary.FontDescriptor(
                        FontLibrary.FontSource.asset(assetPath),
                        size,
                        config -> {
                            if (configurer != null) {
                                configurer.accept(config);
                            }
                        }
                )
        );
    }

    private static Float resolveDefaultSize(Identifier key, Identifier defaultKey) {
        if (key.equals(defaultKey) || key.equals(PROXIMA_KEY)) {
            return PROXIMA_SIZE;
        }
        if (key.equals(JETBRAINS_MONO_KEY)) {
            return JETBRAINS_MONO_SIZE;
        }
        if (key.equals(NOTO_SANS_KEY)) {
            return NOTO_SANS_SIZE;
        }
        return null;
    }
}
