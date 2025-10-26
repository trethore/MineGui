# MineGui
MineGui is a lightweight wrapper around `imgui-java`, designed to simplify the creation of modern GUIs in Minecraft mods built with Fabric.

## Namespace-Aware Runtime
- Every mod using MineGui registers its own namespace via `MineGuiNamespaces.initialize(MineGuiInitializationOptions)` (the default MineGui namespace remains `minegui` for compatibility).
- Configuration files live under `config/minegui/<namespace>/global_config.json`, and per-view ImGui layouts are written to `config/minegui/<namespace>/views/`.
- Styles and view state are isolated per namespace while sharing the global descriptor registry—use namespaced identifiers such as `mymod:dark` when registering descriptors or fonts.
- Developer command `/minegui reload` refreshes config and styles for all registered namespaces; `/minegui reload <namespace>` targets a specific context.
