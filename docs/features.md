# Features & Integrations
This page highlights the core MineGui systems you will use day-to-day, explains why each matters, and shows practical snippets so you can plug them into your mod without guesswork.

## What this page covers
- Registering namespaces and initialization options for multiple modules
- Managing cursor policies and input flow inside custom views
- Understanding the render loop hooks and dockspace configuration
- Applying styles, fonts, persistence, and utility helpers in practice

## Initialization & Namespaces
MineGui keeps configuration, view saves, and style descriptors isolated by namespace.

```java
MineGuiNamespaceContext context = MineGuiNamespaces.initialize(
        MineGuiInitializationOptions.defaults("examplemod")
);
context.ui().register(new ExampleOverlay());
```

- Use `MineGuiInitializationOptions.withDefaultCursorPolicy(...)` if you need a namespace-wide override; the default is `click_to_lock`.
- Call `MineGuiCore.init(...)` once during client startup to activate lifecycle hooks and resource reload listeners.

## Cursor & Input Control
MineGui wraps GLFW input so your UI captures events without fighting Minecraft’s lock state.

```java
public final class ExampleOverlay extends MGView {
    public ExampleOverlay() {
        super("examplemod:overlay", true);
        setCursorPolicy(MGCursorPolicies.screen());
    }

    @Override
    protected void renderView() {
        ImGui.begin(scopedWindowTitle("Example Overlay"));
        ImGui.text("MineGui captures input when this window is visible.");
        ImGui.end();
    }
}
```

- `MGCursorPolicies.clickToLock()` unlocks the cursor until you click the world again, ideal for overlays.
- `MGCursorPolicies.screen()` keeps the cursor free while the view is open; pair it with modal tooling.
- `InputRouter` and the mouse/keyboard mixins ensure ImGui only consumes events when required.

## Render Loop Integration
MineGui hooks `RenderSystem.flipFrame` to run ImGui after vanilla rendering, ensuring compatibility with frame pacing, profiler markers, and multi-viewport mode.

- `ImGuiLoader.onFrameRender()` handles the entire ImGui frame lifecycle, including dockspace setup and draw data submission.
- Enable viewports and dockspace through `GlobalConfig` or `MineGuiInitializationOptions`, and customize window placement using a `DockspaceCustomizer`.

## Styles & Fonts
Style descriptors and fonts live in `StyleManager` and `MGFontLibrary`.

- Register fonts during startup with `MGFonts.registerDefaults(io)` or custom calls; runtime font registration after initialization logs a warning.
- Use `StyleManager.get(namespace)` to apply theme changes or to snapshot descriptors for export.
- Views can override style with `configureStyleDelta()` for scoped adjustments.

## Persistence & Commands
`ViewSaveManager` keeps ImGui layout ini data and style snapshots per view id.

- Mark a view with `setShouldSave(true)` to persist position, dock layout, and style tweaks.
- `/minegui reload` refreshes JSON configs and style snapshots; restart the client after changing fonts or other native resources.
- `/minegui export style force` writes all captured style descriptors to disk so you can version controll them.

## Utility Helpers
- `UI.withVStack`/`withHStack` offer lightweight layout helpers that preserve immediate-mode flexibility.
- `ImGuiImageUtils` loads Minecraft textures into ImGui draw lists, enabling sprite previews, item icons, and atlas debugging.
- `CursorLockUtils` and `ViewportInteractionTracker` bridge ImGui input with Minecraft’s own cursor behaviour and AFK handling.

---

**Next:** [Runtime Flow & Debugging](runtime-flow.md) • **Back:** [Developer Docs Home](README.md)
