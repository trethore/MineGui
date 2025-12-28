package tytoo.minegui.style;

import tytoo.minegui.util.ResourceId;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Styles {

    private Styles() {
    }

    public static StyleScope push(StyleDelta delta) {
        return StyleScope.push(delta);
    }

    public static StyleScope push(Consumer<StyleDelta.Builder> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        StyleDelta.Builder builder = StyleDelta.builder();
        customizer.accept(builder);
        return StyleScope.push(builder.build());
    }

    public static void registerPreset(ResourceId key, Consumer<StyleDescriptor.Builder> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        StyleDescriptor.Builder builder = StyleDescriptor.builder();
        customizer.accept(builder);
        NamedStyleRegistry.getInstance().registerDescriptor(key, builder.build());
    }

    public static void registerPreset(ResourceId key, StyleDescriptor base, Consumer<StyleDescriptor.Builder> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        StyleDescriptor.Builder builder = StyleDescriptor.builder().fromDescriptor(base);
        customizer.accept(builder);
        NamedStyleRegistry.getInstance().registerDescriptor(key, builder.build());
    }

    public static void applyPreset(ResourceId key) {
        StyleManager.getInstance().setGlobalStyleKey(key);
    }

    public static void applyPresetTransient(ResourceId key) {
        StyleManager.getInstance().setGlobalStyleKeyTransient(key);
    }

    public static StyleDescriptor.Builder descriptor() {
        return StyleDescriptor.builder();
    }

    public static StyleDelta.Builder delta() {
        return StyleDelta.builder();
    }

    public static ColorPalette.Builder colors() {
        return ColorPalette.builder();
    }
}
