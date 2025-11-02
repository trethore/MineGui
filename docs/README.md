# MineGui Developer Docs
MineGui integrates Dear ImGui with Fabric-powered Minecraft clients, enabling developers to build immediate-mode GUIs directly within the game. This guide is the central resource for contributors: start here to understand the project goals, find the right documentation for your task, and keep your development workflow efficient.

## What this page covers
- How the documentation set is organized for new contributors
- Recommended reading order from onboarding to advanced topics
- Quick references for development tips, testing, and clean iteration

## Welcome to MineGui
MineGui provides a lean wrapper around `imgui-java`, allowing mod authors to drive raw ImGui calls every frame. The core modules under `src/client/java/tytoo/minegui` add helpers for multi-mod namespaces, style management, state persistence, and cursor control. Before you begin, confirm your environment targets **Java 21**, Fabric Loader `1.21.4`, and Yarn `1.21.4+build.8` to ensure API compatibility.

## Using These Docs
Follow the pages in order for the smoothest onboarding. Each document starts with a quick overview, then breaks into focused sections with code snippets. Every page ends with navigation back here along with the suggested next step, so you can progress linearly or jump directly to the system you are working on.

- Begin with [Getting Started](getting-started.md) to install MineGui and build your first `View`.
- Continue to [Configuration & Persistence](configuration-and-persistence.md) to learn how namespaces, config files, and view saves link together.
- Explore [Styling & Fonts](styling-and-fonts.md) when you need theming, custom font atlases, or style export flows.
- Review [Cursor & Input](cursor-and-input.md) to understand how MineGui manages GLFW state and cursor policies.
- Dive into [Layout & Helpers](layout-and-helpers.md) for layout stacks, constraints, and texture utilities.
- Use [Features & Integrations](features.md) as a capability tour once you’re ready to combine systems.
- Reference [Runtime Flow & Debugging](runtime-flow.md) while tracing lifecycle events or troubleshooting reloads.

## Development Tips
- **Keep iterations small**: register new views through `UIManager`. Toggle a view’s `shouldSave` property only when you want MineGui to persist its layout or style. Test changes using the bundled debug tools (`minegui_debug` namespace).
- **Validate with Gradle**: run `./gradlew compileJava` after edits, `./gradlew build` for full checks, and `./gradlew runDebugClient` to exercise the bundled debug overlays.
- **Use dev commands**: `/minegui reload [namespace]` refreshes JSON configs and view layouts. Use `/minegui export style force [namespace]` to write active style snapshots to disk for version control.
- **Mind namespaces**: reserve `minegui` for internal tooling; choose explicit namespaces when calling `MineGuiCore.init(...)` or `MineGuiNamespaces.initialize(...)`.
- **Clean code passes**: prefer focused refactors, descriptive identifiers, and the project’s four-space indentation to keep the codebase consistent.

---

**Next:** [Getting Started](getting-started.md)