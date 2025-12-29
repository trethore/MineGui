package tytoo.mineguidebug;

import imgui.ImFontConfig;
import tytoo.minegui.style.FontLibrary;
import tytoo.minegui.util.ResourceId;

import java.util.function.Consumer;

public final class DebugFonts {

    public static final ResourceId PROXIMA_KEY = ResourceId.of(MineGuiDebugCore.ID, "proxima");
    public static final ResourceId JETBRAINS_MONO_KEY = ResourceId.of(MineGuiDebugCore.ID, "jetbrains-mono");
    public static final ResourceId NOTO_SANS_KEY = ResourceId.of(MineGuiDebugCore.ID, "noto-sans");

    private static final float PROXIMA_SIZE = 20.0f;
    private static final float JETBRAINS_MONO_SIZE = 18.0f;
    private static final float NOTO_SANS_SIZE = 18.0f;

    private DebugFonts() {
    }

    public static void registerAll() {
        FontLibrary library = FontLibrary.getInstance();
        Consumer<ImFontConfig> configurer = config -> config.setPixelSnapH(true);

        registerFont(library, PROXIMA_KEY, "proxima.ttf", PROXIMA_SIZE, configurer);
        registerFont(library, JETBRAINS_MONO_KEY, "jetbrains-mono.ttf", JETBRAINS_MONO_SIZE, configurer);
        registerFont(library, NOTO_SANS_KEY, "notosans.ttf", NOTO_SANS_SIZE, configurer);
    }

    private static void registerFont(
            FontLibrary library,
            ResourceId key,
            String assetPath,
            float size,
            Consumer<ImFontConfig> configurer
    ) {
        library.registerFont(
                key,
                new FontLibrary.FontDescriptor(
                        FontLibrary.FontSource.asset(MineGuiDebugCore.ID, assetPath),
                        size,
                        config -> {
                            if (configurer != null) {
                                configurer.accept(config);
                            }
                        }
                )
        );
    }
}
