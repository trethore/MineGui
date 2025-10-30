# Runtime Flow & Debugging
This guide shows how MineGui boots ImGui inside the Minecraft client, when key lifecycle callbacks fire, and how to troubleshoot common issues so you can keep your overlays stable across reloads and restarts.

## Lifecycle at a glance
1. `MineGuiCore.init(...)` registers Fabric lifecycle hooks, resource reload listeners, and dev commands.
2. `ImGuiLoader.onGlfwInit(...)` triggers when the client window is created, priming font libraries and pending registrations.
3. `ClientLifecycleEvents.CLIENT_STARTED` sets the loader as “ready,” allowing the first ImGui frame to initialize the context.
4. Each render tick: `RenderSystem.flipFrame` ➜ `ImGuiLoader.onFrameRender()` ➜ `UIManager.render()` iterates visible views.

Keep your registrations—fonts, styles, cursor policies—before step 3 so they are captured during context initialization.

## Reload expectations
- `/minegui reload` re-reads JSON config, view layout snapshots, and style descriptors. It does **not** rebuild the ImGui context or reload fonts.
- To pick up new fonts, descriptors, or native bindings, restart the client so `ImGuiLoader` can recreate the context from scratch.
- When you change resources (e.g., textures), trigger a standard resource reload (F3+T) or use Minecraft’s resource packs; MineGui’s listener clears cached ImGui textures for you.

## Debugging tips
- Use the built-in debug namespace (`minegui_debug`) and its keybinds (`G`, `J`, `H`) to experiment with layout helpers, dockspace behaviour, and style snapshots.
- Check `logs/latest.log` for MineGui warnings—font registration after initialization or missing textures emit clear messages via `MineGuiCore.LOGGER`.
- Use `/minegui export style force` to snapshot current descriptors and diff them under version control.
- If input stops capturing, confirm that a view with `shouldSave=true` is still visible; `MineGuiNamespaces.anyVisible()` gates the render loop and cursor policies.

---

**Back:** [Features & Integrations](features.md) • **Introduction:** [Start Over](introduction.md)
