# Layout & Helpers
MineGui includes lightweight utilities to keep immediate-mode layouts manageable without imposing a full widget framework. Use these helpers to place content, apply constraints, and integrate Minecraft textures inside ImGui windows.

## What this page covers
- Stack-based layout helpers (`VStack`, `HStack`, the `UI` facade, and scoped item helpers)
- Constraint-driven positioning with `LayoutConstraints`, `SizeHints`, and the solver
- Rendering Minecraft textures through `ImGuiImageUtils`
- Additional utilities that streamline immediate-mode authoring

## Stack-Based Layouts
`VStack` and `HStack` provide structured flow without abandoning ImGui’s immediate model. Scoped helpers in `UI` create predictable spacing, wrap `ImGui.beginGroup()`/`endGroup()`, and expose overloads that accept sizing hints.

```java
UI.withVStack(stack -> {
    UI.withVStackItem(stack, () -> ImGui.text("Section Header"));

    VStack.ItemRequest row = new VStack.ItemRequest()
            .estimateHeight(32.0f);

    UI.withVStackItem(stack, row, () -> {
        HStack.Options rowOptions = new HStack.Options()
                .spacing(12.0f)
                .alignment(HStack.Alignment.CENTER)
                .equalizeHeight(true);

        UI.withHStack(rowOptions, hStack -> {
            UI.withHItem(hStack, 120.0f, () -> ImGui.button("Apply"));
            UI.withHItem(hStack, 120.0f, () -> ImGui.button("Reset"));
            UI.withHItem(hStack, () -> ImGui.textColored(0.5f, 0.8f, 0.5f, 1.0f, "Ready"));
        });
    });
});
```

- `VStack.Options.fillMode(VStack.FillMode.MATCH_WIDEST)` equalizes child widths; `uniformWidth(...)` enforces a fixed width.
- `HStack.Options.equalizeHeight(true)` and `uniformHeight(...)` keep horizontal rows aligned even when items vary in size.
- `UI.withVStackResult(...)` and `UI.withHStackResult(...)` return values from scoped blocks, letting you compute layout-dependent data inline.
- `UI.withHItem(...)` overloads accept width/height estimates or an `HStack.ItemRequest` so you can provide `SizeRange` and constraint hints per item.

## Constraint Placement
For precise placement, use `LayoutConstraints` with the solver in `helper.constraint` to target specific regions relative to the current window.

```java
Constraints placement = new Constraints();
placement.setX(Constraints.relative(0.5f));   // center horizontally
placement.setY(Constraints.pixels(64.0f));    // drop 64 pixels from the top

LayoutConstraints constraints = LayoutConstraints.builder()
        .constraints(placement)
        .width(220.0f)
        .build();

VStack.Options options = new VStack.Options()
        .placement(constraints)
        .spacing(8.0f);

try (VStack stack = VStack.begin(options)) {
    try (VStack.ItemScope ignored = stack.next()) {
        ImGui.text("Anchored panel");
    }
}
```

- Combine `RelativeConstraint`, `PixelConstraint`, and `CenterConstraint` to mix relative and absolute positioning.
- Use `LayoutConstraints.builder().target(...)` when the default window-sized target is not what you need (for example, docking nodes or custom viewports).
- `SizeHints.itemSize(...)` and `SizeHints.windowSize(...)` bridge constraints into ImGui’s next-item/window APIs, and `ScaleUnit.SCALED` adapts spacing for high-DPI environments.
- `LayoutConstraintSolver` evaluates constraints each frame; avoid heavy computations inside constraint callbacks to keep rendering smooth.

### Sizing utilities
MineGui bundles a few helpers to keep size calculations predictable:

- `SizeHints.itemWidth(...)`/`itemSize(...)` allow you to pre-compute sizes (with optional `LayoutConstraints`) and automatically apply them to the next widget.
- `SizeRange.of(min, max)` clamps widths/heights without additional branching.
- `ScaleUnit.SCALED` applies framebuffer scale so values stay consistent on high-DPI displays; mix it with `itemWidth(value, unit)` or `HStack.Options.spacing(value, unit)`.
- `LayoutContext.capture()` exposes the current content region metrics if you need to base estimates on whatever ImGui reports for the parent window.

## Working with Textures
`ImGuiImageUtils` bridges Minecraft textures into ImGui draw lists. It caches texture ids and keeps them in sync with resource reloads.

```java
Identifier itemIcon = Identifier.of("minecraft", "textures/item/diamond_sword.png");
ImGuiImageUtils.drawImage(
        itemIcon,
        32.0f, 32.0f, 96.0f, 96.0f, // quad corners
        0,                          // rotation (0-3)
        false,                      // parity
        new float[]{1.0f, 1.0f, 1.0f, 1.0f} // RGBA tint
);
```

- Call `ImGuiImageUtils.textureId(identifier)` when you need the id for custom draw commands.
- During resource reloads (F3 + T), MineGui automatically invalidates cached textures so refreshed assets appear without restarting the client.

## Window Helpers
`Window` wraps `ImGui.begin()`/`end()` so you can reuse placement, sizing, and lifecycle callbacks without duplicating boilerplate.

```java
ImBoolean paletteOpen = new ImBoolean(true);

Window.of(view, "Palette")
        .flags(ImGuiWindowFlags.AlwaysAutoResize)
        .initPos(Constraints.relative(0.5f), Constraints.pixels(96.0f))
        .initDimensions(320.0f, 220.0f)
        .open(paletteOpen)
        .onOpen(() -> ImGui.setScrollHereY(0.0f))
        .render(() -> {
            ImGui.text("Pick a theme and start painting!");
            // render the rest of your view here
        });
```

- Titles scoped with `Window.of(view, ...)` reuse the view’s identifier, preventing dock conflicts.
- Chain `initPos(...)`/`initDimensions(...)` for defaults and `pos(...)`/`dimensions(...)` to enforce placement every frame; both accept numeric values or constraint instances.
- `onOpen(...)` and `onClose(...)` mirror the view lifecycle when you need to reset scroll positions or persist state.

## Additional Utilities
- `InputHelper` exposes ImGui mouse and keyboard helpers for advanced interaction tracking.
- `CursorLockUtils` contains bridge methods for syncing Minecraft’s cursor lock with ImGui workflows.
- `ViewportInteractionTracker` reports recent interactions when you implement custom viewport logic or AFK detection.
- `ResourceId`/`NamespaceIds` build consistent identifiers, `MinecraftIdentifiers` converts to `Identifier`, and `MineGuiText.literal(...)` produces `Text` without verbose boilerplate.
- `ImGuiUtils`, `ColorUtils`, and `SizeHints` gather recurring math and styling helpers so you can keep views focused on rendering.

---

**Next:** [Features & Integrations](features.md) • **Back:** [Developer Docs Home](README.md)
