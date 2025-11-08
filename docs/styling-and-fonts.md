# Styling & Fonts
MineGui layers a flexible styling system over Dear ImGui so you can control themes, fonts, and per-view overrides without leaving immediate mode. This page shows how descriptors, deltas, and font registration work together.

## What this page covers
- Managing global style descriptors and named presets
- Applying per-view overrides with `StyleDelta`
- Registering fonts and merged glyph ranges via `FontLibrary`
- Capturing and exporting style data for iteration

## Managing Style Descriptors
`StyleManager` tracks a global descriptor per namespace along with any named descriptors you register. Switch between them at runtime to swap themes or per-view presets.

```java
// Register a shared descriptor during startup
Identifier darkThemeId = Identifier.of("examplemod", "dark_theme");
StyleManager.registerDescriptor(darkThemeId, StyleDescriptor.builder()
        .rounding(6.0f)
        .framePadding(8.0f, 6.0f)
        .windowBorderSize(0.0f)
        .fontKey(Identifier.of("examplemod", "inter-regular"))
        .fontSize(18.0f)
        .build());

// Activate the descriptor across the namespace
StyleManager styles = StyleManager.get("examplemod");
styles.setGlobalStyleKey(darkThemeId);
styles.apply();
```

- Call `UIManager.get(namespace).register(view)` after setting up styles so views pick up the updated defaults.
- Use `StyleManager.snapshotDescriptors()` when you need to inspect all registered descriptors (for debugging or exporting).

### Waiting for the default descriptor
`StyleManager.onGlobalDescriptorReady` lets you react once MineGui captures the base ImGui style and the font atlas is rebuilt. The listener runs immediately if MineGui already initialized and it reruns whenever the default descriptor is refreshed, so you no longer need to poll tick events to register derived descriptors.

```java
ResourceId workspaceFontKey = ResourceId.of("examplemod", "workspace-font");
ResourceId workspaceStyleKey = ResourceId.of("examplemod", "workspace-style");

StyleManager.onGlobalDescriptorReady(baseDescriptor -> {
    StyleDescriptor workspaceDescriptor = StyleDescriptor.builder()
            .fromDescriptor(baseDescriptor)
            .windowRounding(10.0f)
            .fontKey(workspaceFontKey)
            .fontSize(18.0f)
            .build();
    StyleManager.registerDescriptor(workspaceStyleKey, workspaceDescriptor);
    StyleManager styles = StyleManager.get("examplemod");
    styles.setGlobalStyleKey(workspaceStyleKey);
    styles.apply();
});
```

- Keep font registration in `FontLibrary` queued before `MineGuiCore.init(...)` so your custom font key is available when the descriptor listener fires.

## Per-View Style Deltas
Views can override the active descriptor through `configureBaseStyle` and `configureStyleDelta`. Deltas are stacked and resolved automatically when the view renders.

```java
public final class WarningPanel extends View {
    @Override
    public StyleDelta configureStyleDelta() {
        return StyleDelta.builder()
                .frameBgColor(0.20f, 0.05f, 0.05f, 1.0f)
                .fontSize(20.0f) // bump font size without swapping the descriptor font key
                .build();
    }

    @Override
    protected void renderView() {
        ImGui.begin(scopedWindowTitle("Warning Panel"));
        ImGui.textColored(1.0f, 0.3f, 0.3f, 1.0f, "Something needs your attention!");
        ImGui.end();
    }
}
```

- Return `null` from `configureStyleDelta` when no overrides are required; MineGui short-circuits the allocation.
- `configureBaseStyle(StyleDescriptor descriptor)` lets you clone and adjust the descriptor before the delta applies—ideal for switching to another named descriptor just for this view.
- Outside of overrides, call `view.useStyle(ResourceId.of("modid", "descriptor"))` (or pass a `Identifier`) to set a base descriptor fluently.

## Registering Fonts
`FontLibrary` loads fonts before the ImGui context initializes. Register fonts during mod startup (before the first frame) to avoid warnings.

```java
public final class FontBootstrap {
    public static void registerFonts() {
        Identifier interRegular = Identifier.of("examplemod", "inter-regular");
        Identifier materialIcons = Identifier.of("examplemod", "material-icons");

        Path fontDir = FabricLoader.getInstance()
                .getGameDir()
                .resolve("config/examplemod/fonts");

        FontLibrary fontLibrary = FontLibrary.getInstance();
        fontLibrary.registerFont(
                interRegular,
                new FontLibrary.FontDescriptor(
                        FontLibrary.FontSource.external(fontDir.resolve("Inter-Regular.ttf")),
                        18.0f,
                        config -> config.setPixelSnapH(true)
                )
        );

        // Merge icon glyphs into the same atlas
        fontLibrary.registerMergedFont(
                interRegular,
                materialIcons,
                FontLibrary.FontSource.external(fontDir.resolve("MaterialIcons-Regular.ttf")),
                18.0f,
                fonts -> fonts.getGlyphRangesDefault(), // supply glyph ranges if needed
                config -> config.setGlyphMinAdvanceX(18.0f)
        );
    }
}
```

- Call `FontBootstrap.registerFonts()` before `MineGuiCore.init(...)` finishes so the atlas is ready when `ImGuiLoader` builds the context.
- Use `StyleManager.get(namespace).apply()` or set a style descriptor with `fontKey` pointing to your new font.
- After initialization, `FontLibrary` locks registration—late calls log errors and are ignored.

## Exporting and Debugging Styles
- Enable `setPersistent(true)` on a view to capture style deltas for export. Use `/minegui export style force` to write JSON descriptors under the namespace’s view saves directory.
- Leverage `StyleManager.get(namespace).getEffectiveDescriptor()` while debugging to inspect the active colors and font for a view at runtime.
- If fonts or descriptors appear out of sync, restart the client to rebuild the ImGui context; `/minegui reload` only refreshes JSON payloads.

---

**Next:** [Cursor & Input](cursor-and-input.md) • **Back:** [Developer Docs Home](README.md)
