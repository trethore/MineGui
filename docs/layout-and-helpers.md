# Layout & Helpers
MineGui includes lightweight utilities to keep immediate-mode layouts manageable without imposing a full widget framework. Use these helpers to place content, apply constraints, and integrate Minecraft textures inside ImGui windows.

## What this page covers
- Stack-based layout helpers (`VStack`, `HStack`, and the `UI` facade)
- Constraint-driven positioning with `LayoutConstraints` and the solver
- Rendering Minecraft textures through `ImGuiImageUtils`
- Additional utilities that streamline immediate-mode authoring

## Stack-Based Layouts
`VStack` and `HStack` provide structured flow without abandoning ImGui’s immediate model. Scoped helpers in `UI` create predictable spacing and wrap `ImGui.beginGroup()`/`endGroup()` calls for you.

```java
UI.withVStack(stack -> {
    UI.withVStackItem(stack, () -> ImGui.text("Section Header"));

    VStack.ItemRequest rowRequest = new VStack.ItemRequest()
            .estimatedHeight(28.0f); // reserve space for the row

    UI.withVStackItem(stack, rowRequest, () -> {
        UI.withHStack(null, hStack -> {
            UI.withHItem(hStack, () -> ImGui.button("Apply"));
            UI.withHItem(hStack, () -> ImGui.sameLine());
            UI.withHItem(hStack, () -> ImGui.button("Reset"));
        });
    });
});
```

- Set `VStack.Options.fillMode(VStack.FillMode.MATCH_WIDEST)` to equalize child widths automatically.
- `UI.withVStackResult(...)` returns a value from the scoped block, allowing you to compute layout-dependent data inline.

## Constraint Placement
For precise placement, use `LayoutConstraints` with the solver in `helper.constraint` to target specific regions relative to the current window.

```java
LayoutConstraints constraints = LayoutConstraints.builder()
        .constraints(Constraints.builder()
                .x(Constraints.relative(0.5f))   // center horizontally
                .y(Constraints.pixel(64.0f))      // drop 64 pixels from the top
                .build())
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
- `LayoutConstraintSolver` evaluates constraints each frame; avoid heavy computations inside constraint callbacks to keep rendering smooth.

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

## Additional Utilities
- `InputHelper` exposes ImGui mouse and keyboard helpers for advanced interaction tracking.
- `CursorLockUtils` contains bridge methods for syncing Minecraft’s cursor lock with ImGui workflows.
- `ViewportInteractionTracker` reports recent interactions when you implement custom viewport logic or AFK detection.

---

**Next:** [Features & Integrations](features.md) • **Back:** [Developer Docs Home](README.md)
