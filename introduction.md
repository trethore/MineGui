# Introduction
This overview recaps the MineGui toolchain, runtime expectations, and setup prerequisites so you can confirm your environment before exploring the deeper guides.

## What this page covers
- Tooling and runtime requirements for developing MineGui
- Essential bootstrapping steps shared by every mod
- Quick pointers to the rest of the developer documentation

## Prerequisites
- **Java 21** – Ensure your Gradle toolchain and IDE compile against Java 21.
- **Fabric Loader client 1.21.4** – MineGui targets Fabric with Yarn mappings `1.21.4+build.8`.
- **Gradle setup** – The bundled build uses Fabric Loom; run `./gradlew genSources` when refreshing mappings.
- **IDE configuration** – Enable Lombok annotation processing so generated accessors appear in all source sets.
- **ImGui natives** – The `libs/` folder ships platform natives; keep them packaged with your distribution to avoid load failures.

## Getting Ready in Code
1. Add MineGui as a dependency or include the project module in your workspace.
2. Call `MineGuiCore.init(MineGuiInitializationOptions.defaults("your_namespace"))` from your client entrypoint before you register views.
3. Register fonts, styles, and other ImGui resources **before** the first frame starts—MineGui constructs the ImGui context once per client session.
4. Use `MineGuiNamespaces.initialize(...)` if multiple mods or modules need isolated config, view saves, or styling profiles.

## Next Steps in the Docs
- Read through [Getting Started](getting-started.md) when you are ready for installation details and a first `MGView`.
- Follow the sequence in [Developer Docs Home](README.md) to explore configuration, styling, input, layout helpers, and runtime flow in depth.
- Keep `/minegui reload` and `/minegui export style force` in mind for quick iteration as you move through the guides.

---

**Next:** [Developer Docs Home](README.md) • **Back:** [Developer Docs Home](README.md)
