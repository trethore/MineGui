# Getting Started
This guide walks you through installing MineGui, wiring the initialization hook, and rendering a first ImGui window so you can confirm everything works inside the Minecraft client.

## What this page covers
- Adding MineGui to your build (Gradle or Maven)
- Initializing `MineGuiCore` during client startup
- Creating and registering a MineGui `View` for on-screen UI
- Verifying the setup with the bundled Gradle tasks and debug utilities

## Installing MineGui
MineGui artifacts are published through GitHub Packages under the `trethore/MineGui` repository. Check [the packages page](https://github.com/trethore/MineGui/packages) for the newest version, then add the repository and dependency to your build.

```groovy
// settings.gradle or build.gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/trethore/MineGui")
        credentials {
            username = findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

```groovy
// build.gradle
def mineguiVersion = "0.0.3-0.119.4+1.21.4+build.8"

dependencies {
    modImplementation include("tytoo.minegui:minegui:${mineguiVersion}")
    sourceDeps "tytoo.minegui:minegui:${mineguiVersion}:sources@jar"
}
```

If you manage dependencies with Maven, declare the module directly:

```xml
<dependency>
  <groupId>tytoo.minegui</groupId>
  <artifactId>minegui</artifactId>
  <version>0.0.3-0.119.4+1.21.4+build.8</version>
</dependency>
```

## Initializing the Runtime
MineGui does not auto-register itself—call `MineGuiCore.init(...)` during client startup and provide a namespace unique to your mod.

```java
// src/main/java/com/example/MyModClient.java
import net.fabricmc.api.ClientModInitializer;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.MineGuiInitializationOptions;

public final class MyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Use your mod id or another explicit namespace; the minegui namespace is reserved.
        MineGuiCore.init(MineGuiInitializationOptions.defaults("examplemod"));
    }
}
```

Provide the same namespace anywhere you interact with `MineGuiNamespaces` or `UIManager` so persistence, styles, and cursor policies stay scoped to your mod.

## Building Your First View
Extend `View` to render Dear ImGui widgets. Register the view with the namespace’s `UIManager` once the mod is ready.

```java
// src/main/java/com/example/ui/ExampleOverlay.java
import imgui.ImGui;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.view.View;

public final class ExampleOverlay extends View {
    public ExampleOverlay() {
        super("example/overlay", true); // persistent=true captures window position and style
    }

    @Override
    protected void renderView() {
        ImGui.begin(scopedWindowTitle("Example Overlay"));
        ImGui.text("Hello MineGui!");
        ImGui.end();
    }
}
```

```java
// Somewhere during client init after MineGuiCore.init(...)
UIManager.get("examplemod").register(new ExampleOverlay());

// Or register and immediately show the view with a fluent setup
UIManager.get("examplemod").registerAndShow(
        new ExampleOverlay()
                .persistent(true)
                .useStyle(ResourceId.of("examplemod", "workspace-style"))
);
```

Tips:
- Use `scopedWindowTitle(...)` to ensure unique ImGui ids when docking or multiple instances are visible.
- Call `setCursorPolicy(...)` (see the cursor guide) if your view should adjust Minecraft’s cursor lock behaviour.
- Keep `setPersistent(true)` (or the fluent `persistent(true)`) for overlays that benefit from persisted layout; disable it for transient tooling.
- Call `registerAndShow(...)` when you want overlays to appear immediately, or `registerAll(viewA, viewB, ...)` when bringing multiple overlays online at once.

## Verifying the Setup
- Run `./gradlew compileJava` to confirm your IDE and toolchain target Java 21.
- Launch `./gradlew runDebugClient` to open the client with MineGui debug tooling enabled.
- Use `/minegui reload` to reload JSON configs and `/minegui export style force` to capture style descriptors after tweaking themes.
- If you need sample views, look at the debug namespace under `src/debug/java/tytoo/minegui_debug` and the assets in `src/debug/resources`.

---

**Next:** [Configuration & Persistence](configuration-and-persistence.md) • **Back:** [Developer Docs Home](README.md)
