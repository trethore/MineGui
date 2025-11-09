# Layout & Helpers
MineGui includes lightweight utilities to keep immediate-mode layouts manageable without imposing a full widget framework. Use these helpers to place content, apply constraints, and integrate Minecraft textures inside ImGui windows.

## What this page covers
- Stack-based layout helpers (`MineGuiContext.layout()`, `VStack`, `HStack`, and scoped item helpers)
- Constraint-driven positioning with `LayoutConstraints`, `SizeHints`, and the solver
- Rendering Minecraft textures through `ImGuiImageUtils`
- Additional utilities that streamline immediate-mode authoring

## Layout DSL
`MineGuiContext.layout()` exposes immutable builders for vertical stacks, horizontal rows, and grids. Compose a `LayoutTemplate` once and replay it every frame without juggling nested scopes.

```java
MineGuiContext context = MineGuiCore.init(MineGuiInitializationOptions.defaults("examplemod"));
LayoutApi layouts = context.layout();

LayoutTemplate toolbar = layouts.row()
        .spacing(10.0f)
        .uniformHeight(32.0f)
        .child(slot -> slot.width(120.0f).content(() -> ImGui.button("Apply")))
        .child(slot -> slot.width(120.0f).content(() -> ImGui.button("Reset")))
        .child(slot -> slot.fillWidth(true).content(() -> ImGui.textColored(0.5f, 0.8f, 0.5f, 1.0f, "Ready")))
        .build();

LayoutTemplate panel = layouts.vertical()
        .spacing(6.0f)
        .padding(8.0f)
        .fillMode(VStack.FillMode.MATCH_WIDEST)
        .child(slot -> slot.content(() -> ImGui.text("Section Header")))
        .child(slot -> slot.template(toolbar))
        .build();

layouts.render(panel);
```

- Builders emit immutable `LayoutTemplate`s so you can cache them between frames or preload a library of toolbars, panels, and grids.
- `vertical()` accepts `spacing`, `padding`, `fillMode`, and `uniformWidth` so column layouts stay predictable.
- `row()` mirrors the `HStack` options with spacing, alignment, `equalizeHeight`, and `uniformHeight`.
- Slot builders accept `width`, `height`, `fillWidth`, or nested templates so you can stay declarative without extra helper classes.

## Low-Level Stack Helpers
Use `VStack`/`HStack` directly when you want to stay closer to ImGui’s immediate flow or need dynamic control each frame.

```java
VStack.Options columnOptions = new VStack.Options()
        .spacing(8.0f)
        .fillMode(VStack.FillMode.MATCH_WIDEST);

try (VStack column = VStack.begin(columnOptions)) {
    try (VStack.ItemScope ignored = column.next()) {
        ImGui.text("Section Header");
    }

    try (VStack.ItemScope ignored = column.next()) {
        HStack.Options rowOptions = new HStack.Options()
                .spacing(12.0f)
                .alignment(HStack.Alignment.CENTER)
                .equalizeHeight(true);

        try (HStack row = HStack.begin(rowOptions)) {
            row.next(() -> ImGui.button("Apply"), new HStack.ItemRequest().estimatedWidth(120.0f));
            row.next(() -> ImGui.button("Reset"), new HStack.ItemRequest().estimatedWidth(120.0f));
            row.next(() -> ImGui.textColored(0.5f, 0.8f, 0.5f, 1.0f, "Ready"));
        }
    }
}
```

- `VStack.Options.fillMode(VStack.FillMode.MATCH_WIDEST)` equalizes child widths while `uniformWidth(...)` locks them to a fixed size.
- `HStack.Options.equalizeHeight(true)` and `uniformHeight(...)` keep horizontal rows aligned even when widgets vary.
- `VStack.ItemRequest`/`HStack.ItemRequest` accept min/max sizes and height estimates so constraints feed back into layout decisions.

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

## View Sections
`ViewSection` lets you split a large `View` into lightweight sections without registering more views. Sections render immediately, receive the parent `View`, and stay reusable because they can be stored once and invoked wherever the parent needs them.

```java
public final class CraftingView extends View {
    private final ViewSection header = ViewSection.of(this::renderHeader);
    private final ViewSection body = ViewSection.of(view -> renderInventory(view));

    @Override
    protected void renderView() {
        ImGui.begin("Crafting Station");
        header.render(this);
        ImGui.separator();
        body.render(this);
        ImGui.end();
    }

    private void renderHeader() {
        ImGui.text("Workbench");
    }

    private void renderInventory(View view) {
        ImGui.text("Namespace: " + view.getNamespace());
        // emit inventory widgets
    }
}
```

- Sections keep their own state and helpers, so `View` subclasses avoid 500-line `renderView()` methods.
- Use `ViewSection.of(Runnable)` when the section does not need the parent, or `ViewSection.of(Consumer<View>)` when it does.
- Store sections as fields if they need to cache data, or create them inline when composition is simple.

## Additional Utilities
- `InputHelper` exposes ImGui mouse and keyboard helpers for advanced interaction tracking.
- `CursorLockUtils` contains bridge methods for syncing Minecraft’s cursor lock with ImGui workflows.
- `ViewportInteractionTracker` reports recent interactions when you implement custom viewport logic or AFK detection.
- `ResourceId`/`NamespaceIds` build consistent identifiers, `MinecraftIdentifiers` converts to `Identifier`, and `MineGuiText.literal(...)` produces `Text` without verbose boilerplate.
- `ImGuiUtils`, `ColorUtils`, and `SizeHints` gather recurring math and styling helpers so you can keep views focused on rendering.

---

**Next:** [Features & Integrations](features.md) • **Back:** [Developer Docs Home](README.md)
