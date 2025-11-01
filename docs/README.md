# MineGui Developer Docs
MineGui brings Dear ImGui into Fabric-powered Minecraft clients so you can build immediate-mode tooling inside the game. This guide is the home base for contributors: start here to understand the project goals, choose the right deep-dive, and keep your workflow sharp as you extend the library.

## What this page covers
- How the documentation set is organized for new contributors
- Recommended reading order from onboarding to advanced topics
- Quick references for development tips, testing, and clean iteration

## Welcome to MineGui
MineGui keeps the runtime lean so mod authors can drive raw ImGui every frame. The core modules under `src/client/java/tytoo/minegui` wrap imgui-java with helpers for namespaces, style management, persistence, and cursor control. Before diving into the details, confirm you are targeting Java 21, Fabric Loader 1.21.4, and Yarn `1.21.4+build.8` so the APIs align with the code in this repository.

## Using These Docs
Follow the pages in order for the smoothest onboarding. Each document starts with a quick overview, then breaks into focused sections with code snippets. Every page ends with navigation back here along with the suggested next step, so you can progress linearly or jump directly to the system you are working on.

- Begin with [Getting Started](getting-started.md) to install MineGui and build your first `MGView`.
- Continue to [Configuration & Persistence](configuration-and-persistence.md) to learn how namespaces, config files, and view saves link together.
- Explore [Styling & Fonts](styling-and-fonts.md) when you need theming, custom font atlases, or style export flows.
- Review [Cursor & Input](cursor-and-input.md) to understand how MineGui manages GLFW state and cursor policies.
- Dive into [Layout & Helpers](layout-and-helpers.md) for layout stacks, constraints, and texture utilities.
- Use [Features & Integrations](features.md) as a capability tour once you’re ready to combine systems.
- Reference [Runtime Flow & Debugging](runtime-flow.md) while tracing lifecycle events or troubleshooting reloads.
- Return to the root [Introduction](introduction.md) if you need a high-level recap of prerequisites.

## Development Tips
- **Keep iterations small**: register new views through `UIManager`, toggle `shouldSave` only when you want MineGui to persist layout or style, and test changes via the debug namespace (`minegui_debug`).
- **Validate with Gradle**: run `./gradlew compileJava` after edits, `./gradlew build` for full checks, and `./gradlew runDebugClient` to exercise the bundled debug overlays.
- **Leverage commands**: `/minegui reload` refreshes configs and style snapshots; `/minegui export style force` regenerates descriptors for version control.
- **Mind namespaces**: reserve `minegui` for internal tooling; choose explicit namespaces when calling `MineGuiCore.init(...)` or `MineGuiNamespaces.initialize(...)`.
- **Clean code passes**: prefer focused refactors, descriptive identifiers, and the project’s four-space indentation to keep the codebase consistent.

---

**Next:** [Getting Started](getting-started.md) • **Back:** [Developer Docs Home](README.md)
