# MineGui Architecture Review & Recommendations

## Executive Summary

**Overall Assessment: The codebase is well-designed and largely meets its goals.**

MineGui demonstrates solid architectural decisions with a namespaced, extensible, and modular design. The previous refactoring work has significantly improved the codebase quality. This document identifies remaining opportunities and proposes enhancements to further align with the project goals.

## Implementation Updates (Applied)

- Added lifecycle listener API (`MineGuiLifecycleListener`) with per-context pre/post render hooks.
- Made `UIManager.register(...)` return the registered view for fluent usage.
- Added `Styles` facade for common style operations (push, register presets, apply presets).
- Renamed global config service to `GlobalConfigService` (deprecated `ConfigService` wrapper).
- Added `Renderable` interface and implemented it in `View`.

### Project Goals Checklist

| Goal | Status | Assessment |
|------|--------|------------|
| Safe namespaced library | GOOD | Strong namespace isolation with validation |
| Easy to implement/use | GOOD | Simple entry points, fluent APIs |
| Useful utils without bloatware | GOOD | Helpers are minimal and focused |
| Not screen dependent | EXCELLENT | Overlay-based, works alongside any screen |
| Clear and well organized | GOOD | Logical package structure, some minor improvements possible |

---

## Architecture Strengths

### 1. Namespace Isolation (Excellent)

The namespace system is well-implemented:

- **`MineGuiInitializationOptions`** enforces non-blank, unique namespaces at construction time
- **`MineGuiCore.ID`** is explicitly reserved for internal use
- **`ResourceId`** provides pattern-validated identifiers (`[a-z0-9_.-]+:path`)
- **`ConfigRegistry.sanitizeNamespace()`** prevents path traversal attacks and validates format
- **Multi-context support** via `CONTEXTS` map allows multiple mods to coexist safely

```java
// Validation happens at initialization - fails fast
if (MineGuiCore.ID.equals(namespace)) {
    throw new IllegalArgumentException("Namespace 'minegui' is reserved");
}
```

### 2. Clean Entry Points (Good)

Two clear initialization paths:
- `MineGui.setup(namespace)` - Full-featured with config persistence
- `MineGui.setupSimple(namespace)` - Lightweight, no global config

The builder pattern in `MineGuiInitializationOptions` allows fine-grained control without complexity.

### 3. Extensibility Points (Good)

The library provides several clean extension mechanisms:
- **`CursorPolicy`** - Functional interface for custom cursor behavior
- **`ViewPersistenceAdapter`** - Interface for custom persistence backends
- **`DockspaceCustomizer`** - Customize dockspace layout
- **`NamespaceConfigStore`** - Custom config storage implementation
- **`ViewSection`** - Functional interface for composable UI sections

### 4. Screen Independence (Excellent)

- Renders directly to OpenGL via mixins, not through Minecraft's screen system
- Input routing correctly captures/releases events based on ImGui focus state
- Cursor policies manage lock/unlock without requiring screen context

### 5. Helper Utilities (Good - Minimal & Focused)

- `LayoutHelper` - 5 spacing methods, no bloat
- `TableHelper` - 2 row methods for table construction
- `Window` - Fluent builder for ImGui windows with lifecycle hooks
- `MathUtils` - Validation utilities

---

## Identified Issues & Recommendations

### P0 - Critical (None Found)

The previous refactoring successfully addressed all critical architectural issues.

---

### P1 - High Priority Improvements

#### 1.1 Missing Public Event/Lifecycle API

**Problem:** Developers cannot hook into MineGui lifecycle events (context ready, pre-render, post-render) without using mixins or internal APIs.

**Location:** No dedicated event system exists

**Recommendation:** Add a simple observer pattern for key lifecycle events:

```java
public interface MineGuiLifecycleListener {
    default void onContextReady(MineGuiContext context) {}
    default void onPreRender(MineGuiContext context) {}
    default void onPostRender(MineGuiContext context) {}
    default void onShutdown(MineGuiContext context) {}
}

// In MineGuiRuntimeContext or UIManager:
public void addLifecycleListener(MineGuiLifecycleListener listener);
public void removeLifecycleListener(MineGuiLifecycleListener listener);
```

**Impact:** Enables clean integration without internal coupling.
**Effort:** Medium

---

#### 1.2 View Registration Returns Void

**Problem:** `UIManager.register(View)` returns void, requiring extra code:
```java
// Current (awkward)
MyView view = new MyView();
context.ui().register(view);
view.show();

// vs registerAndShow which returns View (better)
View view = context.ui().registerAndShow(new MyView());
```

**Location:** `manager/UIManager.java:84-99`

**Recommendation:** Make `register()` return the registered view for fluency:
```java
public <T extends View> T register(T view) {
    // ... existing logic
    return view;
}
```

**Impact:** Better API ergonomics
**Effort:** Low

---

#### 1.3 Style System Complexity

**Problem:** The relationship between `StyleManager`, `StyleDescriptor`, `StyleDelta`, and `NamedStyleRegistry` is not immediately clear. Developers must understand:
- StyleManager per namespace (instance-based)
- StyleDescriptor = full style snapshot
- StyleDelta = partial override
- NamedStyleRegistry = global presets
- StyleScope = RAII-style push/pop

**Location:** `style/` package

**Recommendation:** Add a focused facade for common operations:
```java
public final class Styles {
    public static StyleScope push(Consumer<StyleDelta.Builder> customizer);
    public static void registerPreset(ResourceId key, Consumer<StyleDescriptor.Builder> builder);
    public static void applyPreset(ResourceId key);
}
```

This doesn't replace the existing system but provides a simpler entry point.

**Impact:** Improved developer experience
**Effort:** Low

---

### P2 - Medium Priority Improvements

#### 2.1 `ConfigService` vs `NamespaceConfigService` Confusion

**Problem:** Two similarly-named config service classes exist:
- `ConfigService` - manages `GlobalConfig` (legacy mutable)
- `NamespaceConfigService` - manages `NamespaceConfig` (immutable record)

Both serve config purposes but with different data models.

**Location:** `config/ConfigService.java`, `runtime/config/NamespaceConfigService.java`

**Recommendation:** Rename for clarity:
- `ConfigService` -> `LegacyConfigService` or consolidate into `NamespaceConfigService`
- Eventually deprecate `GlobalConfig` in favor of `NamespaceConfig`

**Impact:** Reduced confusion for contributors
**Effort:** Medium

---

#### 2.2 View Base Class Could Use Interface Extraction

**Problem:** `View` is an abstract class. This works well but some developers prefer composition-based approaches. A `Renderable` interface could allow more flexibility.

**Location:** `view/View.java`

**Recommendation:** Extract a minimal interface:
```java
public interface Renderable {
    void render();
    boolean isVisible();
}
```

Views would implement `Renderable` while keeping the abstract class for convenience.

**Impact:** Flexibility for advanced users
**Effort:** Low

---

#### 2.3 Static Singletons in Some Classes

**Problem:** Some classes use static singleton patterns that could make testing harder:
- `InputRouter.INSTANCE`
- `FontLibrary.INSTANCE` 
- `NamedStyleRegistry.INSTANCE`

**Location:** Various

**Recommendation:** These are acceptable for a library focused on simplicity. However, consider providing `get(String namespace)` variants for namespace-scoped retrieval where appropriate. `FontLibrary` and `NamedStyleRegistry` are intentionally global (fonts/styles shared across namespaces).

**Impact:** Testability (minor concern for a mod library)
**Effort:** Low-Medium

---

#### 2.4 Consider Adding Widget Helpers

**Problem:** Developers often repeat common widget patterns:
- Labeled input fields
- Two-column property tables
- Confirmation dialogs
- Notification toasts

**Location:** N/A - new feature

**Recommendation:** Add a `Widgets` utility class with common patterns:
```java
public final class Widgets {
    public static boolean labeledCheckbox(String label, ImBoolean value);
    public static boolean labeledSlider(String label, float[] value, float min, float max);
    public static void propertyRow(String label, String value);
    // etc.
}
```

**Note:** Keep this minimal to avoid bloatware. Only add patterns that are used frequently.

**Impact:** Developer productivity
**Effort:** Medium

---

### P3 - Low Priority / Nice-to-Have

#### 3.1 CRLF Line Endings in Some Files

**Problem:** Some files have mixed CRLF/LF endings (visible in file reads).

**Location:** Various config and view files

**Recommendation:** Run `git add --renormalize .` to apply `.gitattributes` normalization.

**Effort:** Trivial

---

#### 3.2 Consider Documentation Comments for Public APIs

**Problem:** Public API methods lack Javadoc. While the code is readable, API documentation would help external developers.

**Location:** All public methods

**Recommendation:** Add Javadoc to:
- `MineGui` entry points
- `MineGuiContext` interface
- `View` abstract class
- `CursorPolicy` interface
- Public helpers (`Window`, `LayoutHelper`, etc.)

**Effort:** Medium (but valuable)

---

#### 3.3 Debug Module Could Be a Separate Artifact

**Problem:** Debug module is in `src/debug/` but ships with main artifact.

**Location:** `src/debug/`

**Recommendation:** Consider splitting into separate Gradle module if size becomes a concern. Current structure is fine for development.

**Effort:** Low

---

## Questions / Design Considerations

### Q1: Should views support reactive state?

Currently views are imperative (developer calls ImGui each frame). Consider whether a lightweight reactive state holder would be useful:

```java
public class Observable<T> {
    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    
    public void set(T value) {
        this.value = value;
        listeners.forEach(l -> l.accept(value));
    }
}
```

**Trade-off:** Adds complexity but could simplify state management in larger views.

### Q2: Should there be a "hot reload" for styles?

Currently style changes require client restart for font changes. Consider whether runtime font reloading is worth the complexity.

### Q3: Widget validation/constraints?

Should the library provide input validation helpers (e.g., clamped sliders, regex text input)?

---

## Package Structure Analysis

```
tytoo.minegui/
├── command/           # Client commands (/minegui) - Clean, 2 files
├── config/            # Configuration system - Complex but necessary, 15 files
├── helper/            # UI helpers - Minimal, 3 files
│   └── window/        # Window builder - 1 file
├── imgui/             # ImGui integration - Well-factored, 9 files
│   ├── dock/          # Dockspace customization
│   ├── ref/           # Value references (IntRef, FloatRef)
│   └── scope/         # Scoped style overrides
├── input/             # Input routing - 1 file, focused
├── manager/           # UI management - 1 file (UIManager)
├── mixin/             # Minecraft mixins - 7 files, necessary
│   └── client/
├── runtime/           # Runtime services - 6 files
│   ├── config/        # Namespace config service
│   ├── cursor/        # Cursor management
│   └── viewport/      # Viewport utilities
├── style/             # Styling system - 12 files (largest package)
├── util/              # Utilities - 10 files, reasonable
└── view/              # View system - 8 files
    ├── cursor/        # Cursor policies
    └── persistence/   # View persistence
```

**Assessment:** Package structure is logical and follows single-responsibility principle. The `style/` package is the largest due to the comprehensive style system, which is expected.

---

## Summary of Recommended Actions

### Immediate (Before 1.0)
1. Add `register()` return value for fluency (P1.2) - **Low effort, high value**
2. Normalize line endings (P3.1) - **Trivial**

### Short-Term
3. Add lifecycle listener API (P1.1) - **Medium effort, high value for integrations**
4. Add `Styles` facade for common operations (P1.3) - **Low effort**
5. Add Javadoc to public APIs (P3.2) - **Medium effort, ongoing**

### Consider Later
6. Clarify ConfigService naming (P2.1) - **Wait for stability**
7. Add Widget helpers (P2.4) - **Based on user feedback**
8. Extract Renderable interface (P2.2) - **If requested**

---

## Conclusion

MineGui is a well-architected library that achieves its stated goals. The namespace isolation is robust, the API is approachable, and the codebase follows consistent patterns. The recommendations above are refinements rather than fundamental changes.

The library successfully provides:
- Safe multi-mod coexistence through namespacing
- Simple entry points with `MineGui.setup()` and `MineGui.setupSimple()`
- Useful helpers without bloatware
- Screen-independent overlay rendering
- Clear package organization

No critical issues were found. The codebase is ready for broader adoption with minor polish.
