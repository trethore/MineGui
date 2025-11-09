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
MineGuiContext context = MineGuiNamespaces.initialize(
        MineGuiInitializationOptions.defaults("examplemod")
);
context.ui().register(new ExampleOverlay());
```

- Use `MineGuiInitializationOptions.withDefaultCursorPolicy(...)` if you need a namespace-wide override; the default is `click_to_lock`.
- Call `MineGuiCore.init(...)` once during client startup to activate lifecycle hooks and resource reload listeners and capture the returned `MineGuiContext` if you only need the default namespace.

## UIManager orchestration
Each namespace receives a dedicated `UIManager` that wires views, cursor policies, styles, and persistence together. When you call `register(view)` the manager:

- Attaches the namespace’s `ViewSaveManager`, immediately applying saved layout/style data if available.
- Applies the namespace default cursor policy unless the view already set one explicitly.
- Keeps the view in a render list processed every frame; invisible views are skipped with no extra work on your side.
- Ensures profiler scopes wrap each view render so you can spot hotspots in the built-in profiler.

Use `unregister(view)` to detach a view permanently—this hides it, removes save hooks, and stops future renders. `UIManager.hasVisibleViews()` is also what powers `MineGuiNamespaces.anyVisible()`, so toggling visibility affects global cursor handling and rendering short-circuiting.

## Layout DSL
`MineGuiContext.layout()` exposes a modern, immutable DSL so you can compose vertical stacks, rows, and grids without juggling nested `try-with-resources` scopes.

```java
MineGuiContext context = MineGuiCore.init(MineGuiInitializationOptions.defaults("examplemod"));
LayoutApi layouts = context.layout();

LayoutTemplate toolbar = layouts.row()
        .spacing(8f)
        .child(slot -> slot.width(120f).content(() -> ImGui.button("Save")))
        .child(slot -> slot.width(120f).content(() -> ImGui.button("Publish")))
        .build();

LayoutTemplate panel = layouts.vertical()
        .spacing(6f)
        .padding(8f)
        .child(slot -> slot.content(() -> ImGui.text("Project Dashboard")))
        .child(slot -> slot.template(toolbar))
        .build();

layouts.render(panel);
```

- Builders produce immutable `LayoutTemplate`s that you can cache and reuse every frame.
- Stack builders support spacing, padding, and nested templates; grid builders wrap the existing `GridLayout` helper behind a fluent interface.
- Rendering a template simply replays its structure, so your immediate-mode code stays in charge of widget state.

## Cursor & Input Control
MineGui wraps GLFW input so your UI captures events without fighting Minecraft’s lock state.

```java
public final class ExampleOverlay extends View {
    public ExampleOverlay() {
        super("examplemod:overlay", true);
        setCursorPolicy(CursorPolicies.screen());
    }

    @Override
    protected void renderView() {
        ImGui.begin(scopedWindowTitle("Example Overlay"));
        ImGui.text("MineGui captures input when this window is visible.");
        ImGui.end();
    }
}
```

- `CursorPolicies.clickToLock()` unlocks the cursor until you click the world again, ideal for overlays.
- `CursorPolicies.screen()` keeps the cursor free while the view is open; pair it with modal tooling.
- `InputRouter` and the mouse/keyboard mixins ensure ImGui only consumes events when required.

## Render Loop Integration
MineGui hooks `RenderSystem.flipFrame` to run ImGui after vanilla rendering, ensuring compatibility with frame pacing, profiler markers, and multi-viewport mode.

- `ImGuiLoader.onFrameRender()` handles the entire ImGui frame lifecycle, including dockspace setup and draw data submission.
- Enable viewports and dockspace through `GlobalConfig` or `MineGuiInitializationOptions`, and customize window placement using a `DockspaceCustomizer`.

## Styles & Fonts
Style descriptors and fonts live in `StyleManager`, `FontLibrary`, and `Fonts`.

- Register fonts during startup with `Fonts.registerDefaults(io)` or custom calls; runtime font registration after initialization logs a warning.
- Use `StyleManager.get(namespace)` to apply theme changes or to snapshot descriptors for export.
- Views can override style with `configureStyleDelta()` for scoped adjustments.

## Persistence & Commands
`ViewSaveManager` keeps ImGui layout ini data and style snapshots per view id.

- Views persist layout, dock data, and styles automatically; call `setPersistent(false)` for overlays that should never write saves.
- `/minegui reload` refreshes JSON configs and style snapshots; restart the client after changing fonts or other native resources.
- `/minegui export style force` writes all captured style descriptors to disk so you can version control them.

## Utility Helpers
- `LayoutApi` templates and the raw `VStack`/`HStack` helpers cover column/row composition, including `HStack.ItemRequest` width and height hints.
- `ImGuiImageUtils` loads Minecraft textures into ImGui draw lists, enabling sprite previews, item icons, and atlas debugging.
- `CursorLockUtils` and `ViewportInteractionTracker` bridge ImGui input with Minecraft’s own cursor behaviour and AFK handling.
- `ResourceId`, `NamespaceIds`, `MinecraftIdentifiers`, and `MineGuiText` simplify identifier conversions and text creation when integrating with vanilla APIs.

---

**Next:** [Runtime Flow & Debugging](runtime-flow.md) • **Back:** [Developer Docs Home](README.md)
