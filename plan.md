# MineGui Configuration System Rework Plan

## Current State Analysis

### Problems Identified

1. **Dual-Layer Configuration Redundancy**
   - `GlobalConfig` (mutable POJO) + `GlobalConfigService` for file-based persistence
   - `NamespaceConfig` (immutable record) + `NamespaceConfigStore` + `NamespaceConfigService` for runtime access
   - `GlobalConfigNamespaceConfigStore` bridges them, creating unnecessary indirection
   - Both layers duplicate the same fields (viewport, dockspace, globalScale, globalStyleKey)

2. **Confusing Initialization Flags**
   - `loadGlobalConfig` vs `ignoreGlobalConfig` are similar but distinct
   - Logic is scattered across `MineGuiCore.applyConfigOptions()`, `MineGuiRuntimeContext`, and `GlobalConfigService`
   - Users must understand the subtle differences to choose correctly

3. **Overly Complex Feature Profile**
   - Separate load/save feature sets when most users want symmetric behavior
   - Many intermediate objects created with immutable builder pattern
   - `ConfigFeature` enum has 4 features that could be simplified

4. **Static Singletons with Namespace Keys**
   - `ConfigRegistry`, `UIManager`, `StyleManager` all use static maps
   - Couples everything to namespaces at the global level
   - Complicates testing and cleanup

5. **Initialization Options Bloat**
   - 10+ fields in `MineGuiInitializationOptions`
   - Some are rarely used (configRoot, fontRegistrar, viewPersistenceAdapter)
   - Mix of config concerns (persistence) with runtime concerns (fonts, cursor)

6. **Default Fonts Bloat**
   - Bundled fonts in client module add unnecessary weight
   - Not all mods need default fonts

---

## Design Decisions

### Directory Structure

Each namespace gets its own isolated folder:

```
{configRoot}/
  {namespace}/
    config.json              # Core settings
    views/
      {view_slug}.ini        # Per-view ImGui layout
    styles/
      {style_name}.json      # Named style definitions (shared resources)
```

### config.json Contents

```json
{
  "viewport": true,
  "dockspace": true,
  "globalScale": 1.0,
  "globalStyleKey": "mymod:dark_theme"
}
```

- `globalStyleKey` is a reference to a registered style (acts as default)
- Styles are shared resources, not tied to views

### Persistence Flags (not enum)

Instead of a `PersistenceMode` enum, use composable flags:

```java
MineGuiOptions.builder("mymod")
    .withConfig(true)      // persist config.json (default: true)
    .withLayouts(true)     // persist views/*.ini (default: true)
    .withStyles(true)      // persist styles/*.json (default: true)
    .none()                // shorthand: sets all to false
    .build();
```

### Persistence Hierarchy

Namespace config sets the **ceiling**. Views can only **downgrade**, never upgrade:

| Namespace | View | Result |
|-----------|------|--------|
| `withLayouts(true)` | `persistLayout=true` | Layout saved |
| `withLayouts(true)` | `persistLayout=false` | Layout NOT saved (view opts out) |
| `withLayouts(false)` | `persistLayout=true` | Layout NOT saved (namespace ceiling) |
| `none()` | `persistLayout=true` | Nothing saved (namespace wins) |

### View Constructor

```java
// Default: follows namespace rules
View view = new View("my_view");

// Explicit: can opt-out of layout persistence
View view = new View("my_view", true);   // same as default
View view = new View("my_view", false);  // opts out of layout saving
```

- `persistentStyle` boolean is **removed** from View (styles are shared resources, not per-view)

### Save Triggers

1. **Manual**: Developer calls `context.save()` 
2. **Automatic**: Minecraft shutdown (via existing mixin)
3. **View-level**: Respects view's `persistLayout` flag + namespace ceiling

### Styles

- Styles are **named resources** stored in `styles/{name}.json`
- Views reference styles by key (e.g., `"mymod:dark_theme"`)
- Multiple views can share the same style
- No per-view style snapshots (simplification)

### No Default Namespace

- Remove the reserved `"minegui"` namespace
- No default fonts in client module
- Fonts move to debug module or are registered by each mod
- Public font registration API remains available

### No Backward Compatibility

- Clean break, no migration of old config files
- Old classes deleted, not deprecated

---

## Proposed Solution

### Core Principles

1. **Single Source of Truth** - One config record per namespace, no conversion layers
2. **Sensible Defaults** - Most users should only specify a namespace
3. **Flag-Based Persistence** - Composable flags instead of rigid enum
4. **Namespace Isolation** - Each namespace fully owns its configuration and state
5. **Hierarchy Respect** - Namespace is ceiling, views can only opt-out

---

### Phase 1: Simplify Config Model

#### Keep `NamespaceConfig` as the only representation

```java
public record NamespaceConfig(
    String namespace,
    boolean viewportEnabled,
    boolean dockspaceEnabled,
    float globalScale,
    ResourceId globalStyleKey
) {
    public static NamespaceConfig defaults(String namespace) {
        return new NamespaceConfig(namespace, true, true, 1.0f, null);
    }
    
    public NamespaceConfig withViewportEnabled(boolean value) { ... }
    public NamespaceConfig withDockspaceEnabled(boolean value) { ... }
    public NamespaceConfig withGlobalScale(float value) { ... }
    public NamespaceConfig withGlobalStyleKey(ResourceId value) { ... }
}
```

#### New `PersistenceFlags` record

```java
public record PersistenceFlags(
    boolean config,
    boolean layouts,
    boolean styles
) {
    public static PersistenceFlags all() {
        return new PersistenceFlags(true, true, true);
    }
    
    public static PersistenceFlags none() {
        return new PersistenceFlags(false, false, false);
    }
    
    public boolean isAnyEnabled() {
        return config || layouts || styles;
    }
}
```

#### Remove these classes

- `GlobalConfig` - replaced by `NamespaceConfig`
- `GlobalConfigService` - replaced by internal persistence logic
- `ConfigState` - no longer needed
- `GlobalConfigNamespaceConfigStore` - no longer needed
- `ConfigRegistry` - namespace management moves to `MineGuiCore`
- `ConfigSerializer` - becomes internal implementation detail
- `ConfigService` (deprecated) - finally removed
- `ConfigFeature` - replaced by `PersistenceFlags`
- `ConfigFeatureProfile` - replaced by `PersistenceFlags`
- `NamespaceConfigStore` - no longer needed
- `NamespaceConfigService` - merged into context

---

### Phase 2: Streamline Initialization Options

#### New `MineGuiOptions` record with builder

```java
public record MineGuiOptions(
    // === REQUIRED ===
    String namespace,
    
    // === PERSISTENCE ===
    Path configRoot,           // default: Fabric config dir
    PersistenceFlags persistence,  // default: all()
    
    // === BEHAVIOR ===
    ResourceId defaultCursorPolicy,    // default: clickToLock
    DockspaceCustomizer dockspaceCustomizer  // default: noop
) {
    // Simple factory - most common usage
    public static MineGuiOptions of(String namespace) {
        return builder(namespace).build();
    }
    
    // Lite mode - no persistence
    public static MineGuiOptions lite(String namespace) {
        return builder(namespace).none().build();
    }
    
    // Builder for full customization
    public static Builder builder(String namespace) { ... }
    
    public static class Builder {
        private String namespace;
        private Path configRoot;
        private boolean persistConfig = true;
        private boolean persistLayouts = true;
        private boolean persistStyles = true;
        private ResourceId defaultCursorPolicy;
        private DockspaceCustomizer dockspaceCustomizer;
        
        public Builder configRoot(Path path) { ... }
        
        public Builder withConfig(boolean enabled) { 
            this.persistConfig = enabled; 
            return this; 
        }
        
        public Builder withLayouts(boolean enabled) { 
            this.persistLayouts = enabled; 
            return this; 
        }
        
        public Builder withStyles(boolean enabled) { 
            this.persistStyles = enabled; 
            return this; 
        }
        
        public Builder none() {
            this.persistConfig = false;
            this.persistLayouts = false;
            this.persistStyles = false;
            return this;
        }
        
        public Builder cursorPolicy(ResourceId policy) { ... }
        public Builder dockspaceCustomizer(DockspaceCustomizer customizer) { ... }
        
        public MineGuiOptions build() { ... }
    }
}
```

#### Removed from initialization options

- `loadGlobalConfig` / `ignoreGlobalConfig` - replaced by `PersistenceFlags`
- `configStore` - internal detail
- `viewPersistenceAdapter` - internal detail
- `featureProfile` - replaced by `PersistenceFlags`
- `registerDefaultFonts` - no default fonts anymore
- `fontRegistrar` - use public API instead

---

### Phase 3: Simplify Entry Points

```java
public final class MineGui {
    /**
     * Full-featured setup with persistence.
     */
    public static MineGuiContext setup(String namespace) {
        return MineGuiCore.init(MineGuiOptions.of(namespace));
    }
    
    /**
     * Lightweight setup with no persistence.
     */
    public static MineGuiContext setupLite(String namespace) {
        return MineGuiCore.init(MineGuiOptions.lite(namespace));
    }
    
    /**
     * Custom setup with full control.
     */
    public static MineGuiContext setup(MineGuiOptions options) {
        return MineGuiCore.init(options);
    }
}
```

---

### Phase 4: Refactor Runtime Context

#### Simplified `MineGuiContext` interface

```java
public interface MineGuiContext {
    String namespace();
    MineGuiOptions options();
    
    // Config - direct access
    NamespaceConfig config();
    void updateConfig(UnaryOperator<NamespaceConfig> updater);
    
    // Persistence
    PersistenceFlags persistence();
    void save();  // Manual save trigger
    void load();  // Manual load trigger
    
    // Managers
    UIManager ui();
    StyleManager style();
    
    // Behavior
    ResourceId cursorPolicy();
    void setCursorPolicy(ResourceId policyId);
    DockspaceCustomizer dockspaceCustomizer();
    
    // Lifecycle
    void addLifecycleListener(MineGuiLifecycleListener listener);
}
```

#### Simplified `MineGuiRuntimeContext`

```java
public final class MineGuiRuntimeContext implements MineGuiContext {
    private final MineGuiOptions options;
    private NamespaceConfig config;
    private final UIManager uiManager;
    private final StyleManager styleManager;
    private final Path namespaceRoot;  // {configRoot}/{namespace}/
    
    @Override
    public NamespaceConfig config() {
        return config;
    }
    
    @Override
    public void updateConfig(UnaryOperator<NamespaceConfig> updater) {
        this.config = updater.apply(config);
    }
    
    @Override
    public void save() {
        PersistenceFlags flags = options.persistence();
        if (flags.config()) {
            saveConfigJson();
        }
        if (flags.layouts()) {
            saveViewLayouts();
        }
        if (flags.styles()) {
            saveStyles();
        }
    }
    
    private void saveConfigJson() {
        // Write to {namespaceRoot}/config.json
    }
    
    private void saveViewLayouts() {
        // Write each view's INI to {namespaceRoot}/views/{slug}.ini
        // Respects view.persistLayout flag
    }
    
    private void saveStyles() {
        // Write registered styles to {namespaceRoot}/styles/{name}.json
    }
}
```

---

### Phase 5: Update View Class

```java
public abstract class View implements Renderable {
    private final String id;
    private final boolean persistLayout;  // Can opt-out of layout saving
    
    protected View(String id) {
        this(id, true);
    }
    
    protected View(String id, boolean persistLayout) {
        this.id = id;
        this.persistLayout = persistLayout;
    }
    
    public boolean shouldPersistLayout(PersistenceFlags namespaceFlags) {
        // Namespace is ceiling, view can only downgrade
        return namespaceFlags.layouts() && this.persistLayout;
    }
    
    // persistentStyle field REMOVED - styles are shared resources
}
```

---

### Phase 6: Font Registration API

Move default fonts to debug module. Provide public API for custom registration:

```java
// In FontLibrary or similar
public class FontLibrary {
    /**
     * Register a custom font from a resource path.
     * @param namespace The mod's namespace
     * @param fontId Font identifier (e.g., "my_font")
     * @param resourcePath Path to TTF file
     * @param defaultSize Default font size
     */
    public void registerFont(String namespace, String fontId, 
                            Identifier resourcePath, float defaultSize) {
        // Registration logic
    }
}

// Usage in mod initialization
context.fonts().registerFont("mymod", "custom", 
    Identifier.of("mymod", "fonts/custom.ttf"), 16.0f);
```

---

## File Changes Summary

### New Files

| File | Purpose |
|------|---------|
| `config/PersistenceFlags.java` | Flag-based persistence settings |
| `MineGuiOptions.java` | Simplified initialization options with builder |

### Modified Files

| File | Changes |
|------|---------|
| `config/NamespaceConfig.java` | Keep as-is, single source of truth |
| `MineGuiCore.java` | Use `MineGuiOptions`, manage namespace contexts |
| `MineGui.java` | Update to use `MineGuiOptions` |
| `runtime/MineGuiRuntimeContext.java` | Simplify, add `save()`/`load()` methods |
| `MineGuiContext.java` | Simplify interface |
| `view/View.java` | Add `persistLayout` constructor param, remove `persistentStyle` |
| `style/FontLibrary.java` | Add public registration API |

### Deleted Files

| File | Reason |
|------|--------|
| `config/GlobalConfig.java` | Replaced by `NamespaceConfig` |
| `config/GlobalConfigService.java` | Logic moves to context |
| `config/GlobalConfigNamespaceConfigStore.java` | No longer needed |
| `config/ConfigRegistry.java` | Namespace management in `MineGuiCore` |
| `config/ConfigState.java` | No longer needed |
| `config/ConfigSerializer.java` | Becomes internal utility |
| `config/ConfigService.java` | Already deprecated |
| `config/ConfigFeature.java` | Replaced by `PersistenceFlags` |
| `config/ConfigFeatureProfile.java` | Replaced by `PersistenceFlags` |
| `config/NamespaceConfigStore.java` | No longer needed |
| `config/MemoryNamespaceConfigStore.java` | No longer needed |
| `runtime/config/NamespaceConfigService.java` | Merged into context |

### Moved Files

| File | From | To |
|------|------|-----|
| Default fonts | `src/client/resources/assets/minegui/fonts/` | `src/debug/resources/assets/mineguidebug/fonts/` |

---

## API Comparison

### Before (current)

```java
// Complex setup
MineGuiContext context = MineGuiCore.init(
    MineGuiInitializationOptions.builder()
        .namespace("mymod")
        .configRoot(FabricLoader.getInstance().getConfigDir())
        .loadGlobalConfig(true)
        .ignoreGlobalConfig(false)
        .featureProfile(ConfigFeatureProfile.all()
            .withoutFeature(ConfigFeature.VIEW_STYLE_SNAPSHOTS, false, true))
        .registerDefaultFonts(true)
        .defaultCursorPolicyId(ResourceId.of("minegui", "click_to_lock"))
        .build()
);

// Accessing config (confusing layers)
NamespaceConfig cfg = context.config().get();
context.config().update(c -> c.withGlobalScale(1.5f));

// Or via registry (different path to same data)
GlobalConfigService svc = ConfigRegistry.get("mymod");
svc.config().setGlobalScale(1.5f);
svc.save();
```

### After (proposed)

```java
// Simple setup (most users)
MineGuiContext context = MineGui.setup("mymod");

// Lite mode (no persistence)
MineGuiContext context = MineGui.setupLite("mymod");

// Custom setup with flags
MineGuiContext context = MineGui.setup(
    MineGuiOptions.builder("mymod")
        .configRoot(customPath)
        .withConfig(true)
        .withLayouts(false)  // Don't save layouts
        .withStyles(true)
        .build()
);

// Or disable all persistence
MineGuiContext context = MineGui.setup(
    MineGuiOptions.builder("mymod")
        .none()
        .build()
);

// Accessing config (direct and clear)
NamespaceConfig cfg = context.config();
context.updateConfig(c -> c.withGlobalScale(1.5f));

// Manual save
context.save();
```

### View usage

```java
// Default: follows namespace rules
public class MyView extends View {
    public MyView() {
        super("my_view");  // persistLayout defaults to true
    }
}

// Opt-out of layout persistence
public class TransientView extends View {
    public TransientView() {
        super("transient_view", false);  // Never save layout
    }
}
```

---

## Benefits

1. **Fewer Concepts** - 3 config-related classes instead of 12
2. **No Conversion Layers** - `NamespaceConfig` is the only representation
3. **Composable Flags** - Mix and match what to persist
4. **Clear Hierarchy** - Namespace ceiling, views can only opt-out
5. **Simpler Initialization** - 5 options instead of 10+
6. **Direct Config Access** - No intermediate services or registries
7. **Explicit Save/Load** - Developer controls when persistence happens
8. **Lighter Client Module** - No bundled fonts by default
9. **No Backward Compat Burden** - Clean implementation
