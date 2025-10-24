# Repository Guidelines

MineGui is a lightweight Minecraft library that lets you build modern user interfaces directly inside the game. \
It uses imgui-java (Dear ImGui) for rendering and runs on the Fabric mod loader.

## Project Overview & Architecture
- MineGui is a Fabric client library for Minecraft 1.21.4 that wraps imgui-java (Dear ImGui) with lightweight helpers so mod developers can build immediate-mode interfaces directly in game code.
- Source modules live under `src/client/java/tytoo/minegui/**`.
- Resources, mixin configs, and packaged assets remain in `src/client/resources/`, including `assets/minegui/` and `minegui.client.mixins.json` for client mixins.
- Development scaffolding and debug samples stay under `src/debug/` with Java entrypoints in `src/debug/java/tytoo/minegui_debug/**` and supporting assets in `src/debug/resources/assets/minegui_debug/`.
- The runtime flow boots through `MineGuiCore` and `ImGuiLoader`, which initialize imgui-java natives, prepare the ImGui context, and integrate with Minecraft's render loop. `UIManager` manages registered `MGView` instances and renders them each frame.
- Views extend `tytoo.minegui.view.MGView`, rendering raw ImGui calls inside `renderView()`. Layout helpers, cursor controls, and drawing utilities live in `utils` and `constraint`; there are no reusable component subclasses or state containers.

## Design & Philosophy

- Immediate-first: Keep the core thin and let authors drive raw ImGui calls directly.
- Utility-driven: Provide small, composable helpers (cursor locking, texture loading, constraint math) instead of declarative component trees.
- Opt-in layout: Offer optional constraint utilities and future layout helpers without imposing structure on callers.
- Stable surface: Maintain a small, well-documented API focused on ImGui integration and lifecycle management.
- Lightweight runtime: Avoid hidden state machines; emphasise direct ImGui usage with minimal overhead.

## General Coding Conventions
- Target Java 21 with 4-space indentation and packages under `tytoo.minegui.*`.
- Use PascalCase for classes, camelCase for methods and fields, and UPPER_SNAKE_CASE for constants.
- Declare explicit types and avoid `var`; prefer descriptive names over one-letter identifiers.
- Bring types into scope with imports; do not use fully qualified class names inside method bodies.
- Prefer straightforward helper methods over fluent builders; keep APIs explicit and readable.
- When adding shared utilities, document behaviour through clear method names and focused Javadoc rather than abstract component hierarchies.
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
- imgui-java (Dear ImGui bindings) is bundled as JAR files in the `libs/` directory and included via shadow dependency configuration in `build.gradle`.
- ImGui natives are loaded at runtime by `ImGuiLoader`, which handles platform-specific native library initialization.
- ImGui usage is primarily direct; rely on helpers such as `CursorLockUtils`, `ImGuiImageUtils`, and constraint utilities to integrate cleanly with Minecraft.
- When you need to inspect imgui-java sources, look for them under `project-sources/`; if they are missing, ask the user to run `./gradlew generateProjectSources` and explain the verification need (API lookup, rendering hook trace, etc.).

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
