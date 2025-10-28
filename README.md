# MineGui
MineGui is a lightweight wrapper around `imgui-java`, designed to simplify the creation of modern GUIs in Minecraft mods built with Fabric.

## Namespace-Aware Runtime
- Every mod using MineGui registers its own namespace via `MineGuiNamespaces.initialize(MineGuiInitializationOptions)` (the default MineGui namespace remains `minegui` for compatibility).
- Configuration files live under `config/minegui/<namespace>/global_config.json`, and per-view ImGui layouts are written to `config/minegui/<namespace>/views/`.
- Styles and view state are isolated per namespace while sharing the global descriptor registry—use namespaced identifiers such as `mymod:dark` when registering descriptors or fonts.
- Developer command `/minegui reload` refreshes config and styles for all registered namespaces; `/minegui reload <namespace>` targets a specific context.

## Config Feature Profiles & Cursor Policies
- `MineGuiInitializationOptions` accepts a `ConfigFeatureProfile`, letting you declare which config sections load or save (`CORE`, `STYLE_REFERENCES`, `VIEW_LAYOUTS`, `VIEW_STYLE_SNAPSHOTS`). Use `.withFeature(...)`, `.withLoadFeatures(...)`, or `.withSaveFeatures(...)` to tailor persistence while keeping other settings untouched.
- Each `MineGuiNamespaceContext` exposes `context.config().setFeatureProfile(...)`, `enableFeature(...)`, and `disableFeature(...)` for adjusting profiles at runtime without commands.
- Default cursor behaviour is configurable via `MineGuiInitializationOptions.withDefaultCursorPolicy(Identifier)`. Registered views that do not set an explicit `MGCursorPolicy` automatically inherit the namespace default, and you can change it later with `MineGuiNamespaces.setDefaultCursorPolicy(...)`.
