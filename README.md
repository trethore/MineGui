# MineGui
MineGui is a lightweight wrapper around `imgui-java`, designed to simplify the creation of modern GUIs in Minecraft mods built with Fabric.

## Namespace-Aware Runtime
- Every mod using MineGui registers its own namespace via `MineGuiNamespaces.initialize(MineGuiInitializationOptions)` (the default MineGui namespace remains `minegui` for compatibility).
- Configuration files live under `config/minegui/<namespace>/global_config.json`, and per-view ImGui layouts are written to `config/minegui/<namespace>/views/` by default; provide a custom `ConfigPathStrategy` via `MineGuiInitializationOptions.withConfigPathStrategy(...)` when you need to relocate persistence.
- Reuse `ConfigPathStrategies.sandboxed()` to retain MineGui's namespace validation or develop bespoke strategies; invalid resolutions fall back to the sandboxed defaults with a log warning.
- Styles and view state are isolated per namespace while sharing the global descriptor registry—use namespaced identifiers such as `mymod:dark` when registering descriptors or fonts.
- Developer command `/minegui reload` refreshes config and styles for all registered namespaces; `/minegui reload <namespace>` targets a specific context.

## Config Feature Profiles & Cursor Policies
- `MineGuiInitializationOptions` accepts a `ConfigFeatureProfile`, letting you declare which config sections load or save (`CORE`, `STYLE_REFERENCES`, `VIEW_LAYOUTS`, `VIEW_STYLE_SNAPSHOTS`). Use `.withFeature(...)`, `.withLoadFeatures(...)`, or `.withSaveFeatures(...)` to tailor persistence while keeping other settings untouched.
- Each `MineGuiNamespaceContext` exposes `context.config().setFeatureProfile(...)`, `enableFeature(...)`, and `disableFeature(...)` for adjusting profiles at runtime without commands.
- Default cursor behaviour is configurable via `MineGuiInitializationOptions.withDefaultCursorPolicy(Identifier)`. Registered views that do not set an explicit `MGCursorPolicy` automatically inherit the namespace default, and you can change it later with `MineGuiNamespaces.setDefaultCursorPolicy(...)`.

## View Persistence SPI
- MineGui loads and saves ImGui layouts and style snapshots through a pluggable `ViewPersistenceAdapter`, defaulting to the built-in file-backed adapter that hashes view ids under `config/minegui/<namespace>/views/`.
- Provide an adapter during initialization with `MineGuiInitializationOptions.withViewPersistenceAdapter(...)` or swap one at runtime via `MineGuiNamespaces.setViewPersistenceAdapter(...)`. Passing `null` restores the default file adapter.
- Adapters receive `ViewPersistenceRequest` instances containing the namespace, raw view id, and scoped identifier (`<viewNamespace>/<viewId>`); return `Optional.empty()` from `loadLayout` or `loadStyleSnapshot` when no persisted data exists.
- `storeStyleSnapshot` may be invoked on the render thread during flush cycles—keep implementations thread-safe, avoid blocking ImGui, and prefer batching heavy IO asynchronously before returning.
- Use the supplied `ViewStyleSnapshot` helpers (`present`, `deleted`) when exporting descriptors, and honour feature flags: when a namespace disables `VIEW_LAYOUTS` or `VIEW_STYLE_SNAPSHOTS`, your adapter should tolerate `saveLayout` and `storeStyleSnapshot` no-ops without side effects.

## Dockspace Customization Hooks
- Provide a dockspace hook via `MineGuiInitializationOptions.withDockspaceCustomizer(DockspaceCustomizer)` to adjust flags, padding, or placement during namespace registration while MineGui preserves the default passthrough setup.
- Swap or stack behaviour later with `MineGuiNamespaces.setDockspaceCustomizer(...)`; combine handlers using `DockspaceCustomizer.andThen(...)` when multiple systems want to extend the dockspace host.
- The `DockspaceCustomizer` receives a mutable `DockspaceRenderState`, enabling menu bar injection, dockspace size overrides, or `beforeDockspace` runnables, and MineGui keeps `ImGuiWindowFlags.NoBackground` synced whenever `ImGuiDockNodeFlags.PassthruCentralNode` stays active.
