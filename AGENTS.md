# Repository Guidelines

MineGui is a lightweight Minecraft library that lets you build modern user interfaces directly inside the game. \
It uses imgui-java (Dear ImGui) for rendering and runs on the Fabric mod loader.

## Project Overview & Architecture
- MineGui is a Fabric client library for Minecraft 1.21.4 that provides an immediate mode GUI system through imgui-java, enabling reactive component-based UIs integrated into game logic.
- Source modules live under `src/client/java/tytoo/minegui/**` covering component hierarchy, constraint-based layout, state management, ImGui loader, input routing, and UI management; keep public APIs and runtime code within this tree.
- Resources, mixin configs, and packaged assets reside in `src/client/resources/`, including `assets/minegui/` for shipped assets and `minegui.client.mixins.json` for wiring client mixins.
- Development scaffolding, demo windows, and debug assets stay under `src/debug/` with Java entrypoints in `src/debug/java/tytoo/minegui_debug/**` and resources inside `src/debug/resources/assets/minegui_debug/`.
- The runtime flow boots through `MineGuiCore` and `ImGuiLoader`, which initialize imgui-java natives (loaded from libs/ folder via shadow), manage the ImGui rendering context, and integrate with Minecraft's render loop.
- UI windows extend `MGWindow` component to create reusable interfaces, with `UIManager` handling window registration and lifecycle, while components like `MGButton` and `MGText` compose UIs through the constraint system and state reactivity.

## Design & Philosophy

- Foundation-first: Thin, reliable base you compose—not a closed framework.
- Future-proof: Separate component hierarchy, constraint system, state management, and input routing.
- Pluggable: Extensible component system, custom constraints, reactive state bindings, and layout composition.
- Stable API surface: Small, documented interfaces; semantic versioning; clean component abstractions.
- Configurable: Layout constraints, state reactivity, window management—all via fluent builder APIs.
- Lightweight: Minimal idle footprint; immediate mode rendering with efficient state updates.

## General Coding Conventions
- Target Java 21 with 4-space indentation, LF line endings, and packages under `tytoo.minegui.*`.
- Use PascalCase for classes, camelCase for methods and fields, and UPPER_SNAKE_CASE for constants.
- Declare explicit types and avoid `var`; prefer descriptive names over one-letter identifiers.
- Bring types into scope with imports; do not use fully qualified class names inside method bodies.
- Continue the fluent builder and chainable setter patterns established in existing components.
- Assume contributors are working in IntelliJ IDEA; keep code free of IDE warnings.
- Never add code comments unless the user explicitly requests documentation.
- Keep edits minimal and stylistically aligned with surrounding code; do not introduce new formatting tools or unrelated refactors.
- If the requirements are unclear or the task is infeasible, pause and request clarification before proceeding.

## Java 21 Expectations
- Assume the runtime is Java 21; rely only on features that are stable in this release and avoid preview or incubator APIs.
- Leverage modern Java 21 standard-library utilities (Streams, Optional, records) when they improve clarity and maintainability.
- Maintain explicit, readable control flow; avoid overly clever constructs that impair comprehension.
- Preserve binary compatibility for consumers when adjusting public APIs, and surface any intentional breaking changes to the user before proceeding.

## Minecraft Integration Rules
- The codebase targets Fabric for Minecraft 1.21.4 with Yarn mappings `1.21.4+build.8`; use APIs that exist in this combination.
- Prefer modern Fabric/Minecraft methods such as `Identifier.of(String namespace, String path)` and current rendering APIs; avoid deprecated signatures.
- Place new assets, mixin configs, and JSON metadata within `src/client/resources/`, keeping identifiers in the `minegui` namespace.
- Integrate through established abstractions instead of bypassing them unless you are extending those layers.
- Never reference loaders, mappings, or game versions beyond the configured target without explicit user approval.

## Dependencies & External Sources
- imgui-java (Dear ImGui bindings) is bundled as JAR files in the `libs/` directory and included via shadow dependency configuration in build.gradle.
- ImGui natives are loaded at runtime by `ImGuiLoader`, which handles platform-specific native library initialization.
- Keep ImGui usage behind component abstractions so public APIs remain clean and focused on layout/state concerns rather than raw ImGui calls.
- When you need to inspect imgui-java sources, look for them under `project-sources/`; if they are missing, tell the user to run `./gradlew generateProjectSources` and explain why the source view is necessary (for example, to verify APIs or trace rendering hooks).

## Testing & Verification
- Do not run Gradle commands yourself; provide the exact command so the user can execute it.
- Encourage the user to run `./gradlew compileJava` after code changes to catch compilation errors early.
- Prompt or guide the user to run `./gradlew build` for full validation when feasible, and rely on `./gradlew runClient` or `./gradlew runDebugClient` to exercise UI flows in development environments.
- When tooling restrictions prevent the agent from running Gradle tasks, plainly state the limitation and provide the exact commands so the user can execute them.
- Document manual validation steps (such as running a command or opening a window) and enumerate any remaining risks before finishing.
- ImGui natives are loaded from the bundled libs/ folder; no external downloads are required.

## Pull Requests
- Keep PRs focused on a single concern and avoid unrelated cleanups.
- Provide clear summaries, rationale, and manual test steps; include screenshots or GIFs for UI changes when applicable.
- Use Conventional Commit conventions (e.g., `feat(ui): add slider snap support`) for commit messages.
- Flag breaking API changes and coordinate versioning with the user before merging.
