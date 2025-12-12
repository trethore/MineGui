# Repository Guidelines

MineGui is a lightweight Minecraft library that lets you build modern user interfaces directly inside the game. \
It uses imgui-java (Dear ImGui) for rendering and runs on the Fabric mod loader.

## General Coding Conventions

- Target Java 21 with 4-space indentation, and packages under `tytoo.minegui*`.
- Use PascalCase for classes, camelCase for methods and fields, and UPPER_SNAKE_CASE for constants.
- Declare explicit types and avoid `var`; prefer descriptive names over one-letter identifiers.
- Import types rather than using fully qualified names inside method bodies.
- When adding shared utilities, document behavior through clear method names and arguments rather than abstract
  component hierarchies.
- Assume contributors are working in IntelliJ IDEA; keep code free of IDE warnings.
- Avoid code comments unless documentation is explicitly requested.
- Keep edits minimal and stylistically consistent with surrounding code; do not introduce unrelated refactors or new
  formatting tools.
- If requirements are unclear or infeasible, request clarification before proceeding.
- Order members in Java classes consistently: static constants, static fields, instance fields, constructors, overridden
  methods, public methods, protected/private helpers, and finally getters and setters.

## Java 21 Expectations

- Assume Java 21 at runtime; use only stable features and avoid preview or incubator APIs.
- Use modern Java 21 standard-library utilities (Streams, Optional, records) when they improve clarity.
- Maintain explicit, readable control flow; avoid clever constructs that harm comprehension.

## Minecraft Integration Rules

- The codebase targets Fabric for Minecraft 1.21.4 with Yarn mappings `1.21.4+build.8`; use APIs that exist in this
  combination.
- Prefer modern Fabric/Minecraft methods such as `Identifier.of(String namespace, String path)` and up-to-date rendering
  APIs; avoid deprecated signatures.
- Place new assets, mixin configs, and JSON metadata within `src/client/resources/`, keeping identifiers in the
  `Main.MOD_ID` namespace.
- Integrate through established abstractions unless explicitly extending them.
- Never reference loaders, mappings, or game versions beyond the configured target without explicit user approval.
- Always initialize MineGui with an explicit, non-blank namespace; the `minegui` namespace is reserved for MineGui’s own
  tooling and debug flows.

## Dependencies & External Sources

- Fabric Loader, Fabric API, and Yarn mappings are versioned in `gradle.properties`; Fabric Loom integrates them into
  the client source set and remaps game classes during packaging. Keep these aligned with Minecraft `1.21.4` before
  updating APIs.
- When you need to inspect third-party library code (Fabric, Minecraft, or imgui-java), first look in
  `libs-src/<library>/` (produced by `./gradlew unpackSources`) instead of decompiling jars or browsing online.
- Library sources are fetched through the `sourceDeps` configuration (see `build.gradle`) and unpacked per-library with
  `./gradlew unpackSources` into `libs-src/<library>`; if `libs-src/` is missing or stale, ask the user to run that
  task.
- imgui-java (Dear ImGui bindings) is bundled as JAR files in the `libs/` directory and included via shadow dependency
  configuration in `build.gradle`.
- Lombok ships as a dependency; prefer its annotations to reduce boilerplate.

## Testing & Verification

- **NEVER** execute any Gradle commands (such as `./gradlew ...`) yourself; instead, **ALWAYS** provide the exact
  command for the user to run manually.
- Encourage running `./gradlew compileJava` after changes, `./gradlew build` for full validation, and
  `./gradlew runClient` to test UI flows.
- Document manual validation steps and remaining risks before completing work.

## Pull Requests

- Keep PRs focused on a single concern and avoid unrelated cleanups.
- Provide clear summaries, rationale, and manual test steps; include visuals for UI changes when relevant.
- Use Conventional Commit conventions (such as `feat(ui): add slider snap support`) and flag breaking API changes early.
