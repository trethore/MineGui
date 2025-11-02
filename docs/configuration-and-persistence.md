# Configuration & Persistence
MineGui keeps runtime state isolated by namespace so multiple mods can share the same client session without colliding. This page explains how initialization options, global config files, and view persistence fit together.

## What this page covers
- Choosing namespaces and initialization options for MineGui
- Loading, saving, and customizing global configuration profiles
- Persisting ImGui layouts and styles with `ViewSaveManager`
- Using developer commands to reload state and export descriptors

## Namespaces and Initialization
`MineGuiCore.init(...)` establishes the default namespace and registers lifecycle hooks. Every additional namespace is created by calling `MineGuiNamespaces.initialize(...)` with its own `MineGuiInitializationOptions`.

```java
// Primary namespace registered during client init
MineGuiCore.init(MineGuiInitializationOptions.defaults("examplemod"));

// Optional secondary namespace for a tooling module
MineGuiNamespaces.initialize(
        MineGuiInitializationOptions.builder("examplemod-tools")
                .loadGlobalConfig(false)          // opt out of automatic config loads
                .ignoreGlobalConfig(true)          // keep tooling data ephemeral
                .defaultCursorPolicyId(CursorPolicies.screenId())
                .build()
);
```

- The namespace you pass to `MineGuiCore.init(...)` becomes the default for `UIManager.getInstance()` and other singletons.
- The `minegui` namespace is reserved for MineGui internals—use your mod id or another explicit string.
- Combine namespaces when you need separate config directories, cursor policies, or persistence adapters.

### Initialization options cheat sheet
- `loadGlobalConfig(boolean)` decides whether JSON config is read on startup; pair with `ignoreGlobalConfig(true)` when you want a temporary namespace that never touches disk.
- `featureProfile(...)`, `loadFeatures(...)`, and `saveFeatures(...)` let you toggle individual `ConfigFeature` values, so you can load styles without saving layouts (or vice versa).
- `configPathStrategy(...)` points saves at a different location using helpers such as `ConfigPathStrategies.flat(...)` or your own implementation.
- `defaultCursorPolicyId(...)` establishes the namespace-wide cursor policy applied when a view does not set one explicitly.
- `dockspaceCustomizer(...)` installs a `DockspaceCustomizer` that can tweak dock node flags, create splits, or reposition the dockspace each frame.
- `viewPersistenceAdapter(...)` swaps in custom persistence so layouts and style snapshots can flow to a mod-specific folder or database.
- `withNamespace(...)`, `withLoadGlobalConfig(...)`, and other `with*` helpers clone the options record, making it easy to derive variants during runtime setup.

## Working with Global Config
`GlobalConfigManager` stores user-facing settings (dockspace, viewport mode, font scale, etc.) under the config directory associated with your namespace. You can adjust where files live, which features load, and how they save.

```java
// Adjust feature profiles per namespace
NamespaceConfigAccess config = MineGuiNamespaces.get("examplemod")
        .config();

config.setLoadFeatures(Set.of(ConfigFeature.CORE, ConfigFeature.VIEW_LAYOUTS));
config.disableFeature(ConfigFeature.VIEW_STYLE_SNAPSHOTS);

// Swap the config path strategy (e.g., to share a directory across namespaces)
MineGuiInitializationOptions options = MineGuiInitializationOptions.builder("examplemod")
        .configPathStrategy(ConfigPathStrategies.flat("examplemod"))
        .build();
MineGuiNamespaces.initialize(options);
```

- `ConfigFeatureProfile` lets you independently control which features load and which ones save.
- Call `config.configPath()` or `config.viewSavesPath()` when you need to surface configuration files to users.
- Use `GlobalConfigManager.save(namespace)` after programmatic changes so the JSON payload mirrors the active state.

## Persisting Views and Styles
`ViewSaveManager` pairs each registered `View` with layout data (ImGui ini sections) and optional style snapshots. Views must opt in with `setShouldSave(true)` to participate.

```java
public final class InspectorOverlay extends View {
    public InspectorOverlay() {
        super("example/inspector", true);
    }

    @Override
    protected void renderView() {
        ImGui.begin(scopedWindowTitle("Inspector"));
        ImGui.text("Target: " + MinecraftClient.getInstance().player.getName().getString());
        ImGui.end();
    }
}

UIManager.get("examplemod").register(new InspectorOverlay());
```

Customize persistence with a namespace-specific adapter—ideal when you want to redirect saves into your own mod folder or database.

```java
// Minimal adapter that writes layouts to a custom directory
public final class CustomViewAdapter implements ViewPersistenceAdapter {
    // helper methods (customLayoutPath/customStylePath/readString/writeString/deleteIfExists)
    // encapsulate your own persistence strategy

    @Override
    public Optional<String> loadLayout(ViewPersistenceRequest request) {
        return readString(customLayoutPath(request));
    }

    @Override
    public void saveLayout(ViewPersistenceRequest request, String payload) {
        writeString(customLayoutPath(request), payload);
    }

    @Override
    public Optional<String> loadStyleSnapshot(ViewPersistenceRequest request) {
        return readString(customStylePath(request));
    }

    @Override
    public boolean storeStyleSnapshot(ViewStyleSnapshot snapshot) {
        if (snapshot.deleted()) {
            deleteIfExists(customStylePath(snapshot.request()));
            return true;
        }
        writeString(customStylePath(snapshot.request()), snapshot.snapshotJson());
        return true;
    }
}

MineGuiNamespaces.initialize(
        MineGuiInitializationOptions.builder("examplemod")
                .viewPersistenceAdapter(new CustomViewAdapter())
                .build()
);
```

- `ViewSaveManager.flush()` runs during client shutdown; call it manually if you need deterministic saves.
- The `/minegui export style force` command rewrites descriptors that were captured at runtime, making it easy to version control style tweaks.

## Developer Commands and Reloads
- `/minegui reload` refreshes JSON configs, view layout snapshots, and style JSON. It does not rebuild the ImGui context—restart the client for new fonts.
- `/minegui export style force` dumps the current style descriptors for all views with `shouldSave=true`.
- `/minegui reload namespace <id>` lets you reload a single namespace if you’ve built custom commands wrapping MineGui’s APIs.
- Resource reloads (F3 + T) clear ImGui texture caches via `ImGuiImageUtils.invalidateAll()` thanks to the Fabric resource listener registered in `MineGuiCore`.

---

**Next:** [Styling & Fonts](styling-and-fonts.md) • **Back:** [Developer Docs Home](README.md)
