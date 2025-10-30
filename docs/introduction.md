# Introduction
This page outlines the MineGui toolchain requirements, how to prepare your environment, and where to find the deeper integration guides so you can start rendering ImGui inside Fabric-based mods with confidence.

## Prerequisites
- **Java 21** – MineGui targets Java 21 language and runtime features; ensure your Gradle toolchain and IDE compile against 21.
- **Fabric Loader client 1.21.4** – MineGui integrates with Fabric loader APIs and Yarn mappings at `1.21.4+build.8`.
- **Gradle setup** – The included `build.gradle` expects the Fabric Loom plugin; run `./gradlew genSources` when you refresh mappings.
- **IDE configuration** – Enable Lombok annotation processing (e.g., IntelliJ IDEA ➜ *Settings → Build → Compiler → Annotation Processors*) so generated getters/setters appear in the client and debug source sets.
- **ImGui natives** – MineGui bundles platform natives in `libs/`; keep them on the classpath to avoid load failures at runtime.

## Getting ready in code
1. Add MineGui as a dependency or include the project module in your workspace.
2. Call `MineGuiCore.init(MineGuiInitializationOptions.defaults("your_namespace"))` from your client entrypoint before you register views.
3. Register fonts, styles, and other ImGui resources **before** the first frame starts—MineGui constructs the ImGui context once per client session.
4. Use `MineGuiNamespaces.initialize(...)` if multiple mods or modules need isolated config, view saves, or styling profiles.

## What’s next?
- Continue to [Features & Integrations](features.md) for concrete examples covering cursor policies, rendering hooks, persistence, and more.
- Need to jump back here? Every page links back to the introduction at the bottom for easy navigation.
