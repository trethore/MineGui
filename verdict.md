# MineGui Code Quality Audit

This document provides an objective assessment of the MineGui codebase, identifying strengths, areas for improvement,
and proposing lightweight utility additions aligned with MineGui's non-bloatware philosophy.

---

## Architecture Overview

MineGui follows a clean multi-namespace architecture where each mod gets an isolated context:

```
MineGui.setup() -> MineGuiCore.init() -> MineGuiRuntimeContext
    -> UIManager.get(namespace)
    -> StyleManager.get(namespace)
    -> ViewPersistenceManager
    -> NamespaceConfigService
```

**Key Design Decisions:**

- Thread-safe singletons via `ConcurrentHashMap` (`MineGuiCore.java:27-28`)
- Lazy initialization until GLFW window is ready (`ImGuiLoader.java:131-142`)
- Namespace isolation prevents cross-mod interference

---

## What's Good

### 1. AutoCloseable Resource Management

`StyleScope.java:5-22` implements a clean try-with-resources pattern:

```java
try(StyleScope ignored = StyleScope.push(theme())){
        // render content - style auto-pops on exit
        }
```

This pattern at `PlaygroundView.java:33-40` eliminates manual push/pop counting.

### 2. Fluent Window Builder

`Window.java:27-170` is an excellent pattern that wraps `ImGui.begin()/end()` safely:

```java
Window.of(view, "My Window")
    .

flags(ImGuiWindowFlags.NoCollapse)
    .

initDimensions(400f,300f)
    .

render(() ->{
        // content - window auto-ends on exit or error
        });
```

Key strengths:

- Lifecycle callbacks (`onOpen`, `onClose`) at lines 107-114
- Position/size conditions at lines 62-100
- State tracking per title at lines 173-178

### 3. Immutable Value Objects

| Class             | Location                      | Pattern                            |
|-------------------|-------------------------------|------------------------------------|
| `ResourceId`      | `util/ResourceId.java:39-60`  | Validated immutable identifier     |
| `Vec2`            | `style/Vec2.java`             | Simple record for 2D vectors       |
| `ColorPalette`    | `style/ColorPalette.java`     | Immutable with builder             |
| `NamespaceConfig` | `config/NamespaceConfig.java` | Record with `withX()` copy methods |

### 4. Thread Safety

- ThreadLocal style stacks (`StyleManager.java:29-30`)
- CopyOnWriteArrayList for views (`UIManager.java:30-31`)
- Volatile fields for cross-thread visibility (`ImGuiLoader.java:36-40`)
- ConcurrentHashMap for all registries

### 5. Defensive Programming

Consistent null checks with early returns:

- `UIManager.java:77-89` - null view guard
- `ResourceId.java:39-60` - constructor validation with normalization
- `Objects.requireNonNull()` usage throughout builders

---

## What's Bad / Can Be Improved

### 1. ~~Style System Duplication~~ [FIXED]

**Refactored:** Created shared infrastructure:
- `StyleProperty.java` - Enum defining all 33 ImGui style properties with getters/setters
- `StylePropertyValues.java` - Shared container with EnumMap storage, validation, merge logic

Both `StyleDelta` and `StyleDescriptor` now delegate to `StylePropertyValues`.

### 2. ~~GlobalConfigManager Monolith~~ [FIXED]

**Refactored:** Split into focused classes:
- `ConfigState.java` - Namespace state container with field accessors
- `ConfigPathResolver.java` - Path resolution logic and strategy handling  
- `ConfigSerializer.java` - JSON serialization, load/save profile merging
- `ConfigPaths.java` - Shared path utility methods

`GlobalConfigManager.java` reduced from 735 to ~400 lines.

### 3. Missing AutoCloseable Wrappers for ImGui Operations

Manual push/pop at `WidgetShowcaseSection.java:132-136`:

```java
ImGui.pushID("command_preview");
ImGui.

beginDisabled();
ImGui.

inputText("##command",commandBuffer);
ImGui.

endDisabled();
ImGui.

popID();
```

No equivalent to `StyleScope` exists for:

- `ImGui.pushID()` / `popID()`
- `ImGui.beginDisabled()` / `endDisabled()`
- `ImGui.pushStyleColor()` / `popStyleColor()`
- `ImGui.pushStyleVar()` / `popStyleVar()`

### 4. Float Holder Array Ceremony

Repeated 14+ times across debug sections. Example at `WidgetShowcaseSection.java:64-68`:

```java
float[] sliderHolder = {sliderValue};
if(ImGui.

sliderFloat("Chunk radius",sliderHolder, 16f,96f,"%.0f blocks")){
sliderValue =sliderHolder[0];
        }
```

Also at:

- `WidgetShowcaseSection.java:69-73, 100-103, 104-108`
- `LayoutShowcaseSection.java:42-48`
- `ResourcePreviewSection.java:50-57`

### 5. Repeated Context Validity Checks

Same guard repeated in multiple files:

```java
ImGuiContext context = ImGui.getCurrentContext();
if(!MineGuiCore.

isInitialized() ||context ==null||context.

isNotValidPtr()){
        return;
        }
```

Found at:

- `InputRouter.java:127-132, 135-139`
- `CursorPolicyRegistry.java:102-108, 182-185, 194-197`
- `ImGuiLoader.java:336-339, 352-354`

### 6. Magic Numbers

Spacing values hardcoded throughout debug sections:

```java
ImGui.dummy(0f,6f);  // Repeated 20+ times
```

Found at:

- `WidgetShowcaseSection.java:39, 41, 43, 45`
- `OverviewSection.java:26-31`
- `LayoutShowcaseSection.java:27-33`

### 7. Table Boilerplate

Same table setup repeated in 5 sections:

```java
int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp;
if(ImGui.

beginTable("id",2,flags)){
        ImGui.

tableSetupColumn("Column1",ImGuiTableColumnFlags.WidthFixed, 140f);
    ImGui.

tableSetupColumn("Column2");
// rows...
    ImGui.

endTable();
}
```

Locations:

- `OverviewSection.java:72-83`
- `LayoutShowcaseSection.java:152-162`
- `WidgetShowcaseSection.java:85-94, 155-168`
- `StyleWorkflowSection.java:72-83`

---

## Proposed Utility Helpers

The following utilities align with MineGui's lightweight philosophy - small, focused, zero-overhead abstractions.

### 1. IdScope (High Priority)

AutoCloseable wrapper for `ImGui.pushID()/popID()`.

**Proposed API:**

```java
public final class IdScope implements AutoCloseable {

    private IdScope() {
    }

    public static IdScope of(String id) {
        ImGui.pushID(id);
        return new IdScope();
    }

    public static IdScope of(int id) {
        ImGui.pushID(id);
        return new IdScope();
    }

    @Override
    public void close() {
        ImGui.popID();
    }
}
```

**Usage:**

```java
// Before
ImGui.pushID("command_preview");
ImGui.

beginDisabled();
ImGui.

inputText("##command",commandBuffer);
ImGui.

endDisabled();
ImGui.

popID();

// After
try(
IdScope ignored = IdScope.of("command_preview")){
        ImGui.

beginDisabled();
    ImGui.

inputText("##command",commandBuffer);
    ImGui.

endDisabled();
}
```

### 2. DisabledScope (High Priority)

AutoCloseable wrapper for `ImGui.beginDisabled()/endDisabled()`.

**Proposed API:**

```java
public final class DisabledScope implements AutoCloseable {
    private final boolean wasDisabled;

    private DisabledScope(boolean disabled) {
        this.wasDisabled = disabled;
        if (disabled) {
            ImGui.beginDisabled();
        }
    }

    public static DisabledScope of() {
        return new DisabledScope(true);
    }

    public static DisabledScope when(boolean condition) {
        return new DisabledScope(condition);
    }

    @Override
    public void close() {
        if (wasDisabled) {
            ImGui.endDisabled();
        }
    }
}
```

**Usage:**

```java
// Before
ImGui.beginDisabled();
ImGui.

inputText("##command",commandBuffer);
ImGui.

endDisabled();

// After
try(
DisabledScope ignored = DisabledScope.of()){
        ImGui.

inputText("##command",commandBuffer);
}

// Conditional disable
        try(
DisabledScope ignored = DisabledScope.when(isReadOnly)){
        ImGui.

inputText("##field",buffer);
}
```

### 3. FloatRef / IntRef (High Priority)

Mutable wrapper to eliminate float holder array ceremony.

**Proposed API:**

```java
public final class FloatRef {
    private float value;
    private final float[] holder = new float[1];

    public FloatRef(float initial) {
        this.value = initial;
        this.holder[0] = initial;
    }

    public float get() {
        return value;
    }

    public void set(float value) {
        this.value = value;
        this.holder[0] = value;
    }

    public float[] asArray() {
        holder[0] = value;
        return holder;
    }

    public boolean sync() {
        if (holder[0] != value) {
            value = holder[0];
            return true;
        }
        return false;
    }

    public boolean sliderFloat(String label, float min, float max) {
        return sliderFloat(label, min, max, "%.3f");
    }

    public boolean sliderFloat(String label, float min, float max, String format) {
        holder[0] = value;
        if (ImGui.sliderFloat(label, holder, min, max, format)) {
            value = holder[0];
            return true;
        }
        return false;
    }

    public boolean dragFloat(String label, float speed, float min, float max) {
        return dragFloat(label, speed, min, max, "%.3f");
    }

    public boolean dragFloat(String label, float speed, float min, float max, String format) {
        holder[0] = value;
        if (ImGui.dragFloat(label, holder, speed, min, max, format)) {
            value = holder[0];
            return true;
        }
        return false;
    }
}
```

**Usage:**

```java
// Before
private float sliderValue = 48f;

void render() {
    float[] sliderHolder = {sliderValue};
    if (ImGui.sliderFloat("Chunk radius", sliderHolder, 16f, 96f, "%.0f blocks")) {
        sliderValue = sliderHolder[0];
        widgetStatus = "Chunk radius set to %.0f".formatted(sliderValue);
    }
}

// After
private final FloatRef sliderValue = new FloatRef(48f);

void render() {
    if (sliderValue.sliderFloat("Chunk radius", 16f, 96f, "%.0f blocks")) {
        widgetStatus = "Chunk radius set to %.0f".formatted(sliderValue.get());
    }
}
```

### 4. StyleColorScope / StyleVarScope (Medium Priority)

AutoCloseable wrappers for style color and var pushes.

**Proposed API:**

```java
public final class StyleColorScope implements AutoCloseable {
    private final int count;

    private StyleColorScope(int count) {
        this.count = count;
    }

    public static StyleColorScope of(int idx, int color) {
        ImGui.pushStyleColor(idx, color);
        return new StyleColorScope(1);
    }

    public static StyleColorScope of(int idx, float r, float g, float b, float a) {
        ImGui.pushStyleColor(idx, r, g, b, a);
        return new StyleColorScope(1);
    }

    public StyleColorScope and(int idx, int color) {
        ImGui.pushStyleColor(idx, color);
        return new StyleColorScope(count + 1);
    }

    @Override
    public void close() {
        ImGui.popStyleColor(count);
    }
}

public final class StyleVarScope implements AutoCloseable {
    private final int count;

    private StyleVarScope(int count) {
        this.count = count;
    }

    public static StyleVarScope of(int idx, float val) {
        ImGui.pushStyleVar(idx, val);
        return new StyleVarScope(1);
    }

    public static StyleVarScope of(int idx, float x, float y) {
        ImGui.pushStyleVar(idx, x, y);
        return new StyleVarScope(1);
    }

    public StyleVarScope and(int idx, float val) {
        ImGui.pushStyleVar(idx, val);
        return new StyleVarScope(count + 1);
    }

    @Override
    public void close() {
        ImGui.popStyleVar(count);
    }
}
```

**Usage:**

```java
// Before
ImGui.pushStyleColor(ImGuiCol.Button, 0xFF0000FF);
ImGui.

pushStyleColor(ImGuiCol.ButtonHovered, 0xFF3333FF);
ImGui.

button("Danger");
ImGui.

popStyleColor(2);

// After
try(
StyleColorScope ignored = StyleColorScope.of(ImGuiCol.Button, 0xFF0000FF)
        .and(ImGuiCol.ButtonHovered, 0xFF3333FF)){
        ImGui.

button("Danger");
}
```

### 5. ContextGuard (Medium Priority)

Utility for repeated context validity checks.

**Proposed API:**

```java
public final class ContextGuard {

    private ContextGuard() {
    }

    public static boolean isReady() {
        if (!MineGuiCore.isInitialized()) {
            return false;
        }
        ImGuiContext context = ImGui.getCurrentContext();
        return context != null && !context.isNotValidPtr();
    }

    public static void ifReady(Runnable action) {
        if (isReady()) {
            action.run();
        }
    }

    public static <T> Optional<T> whenReady(Supplier<T> supplier) {
        if (isReady()) {
            return Optional.ofNullable(supplier.get());
        }
        return Optional.empty();
    }
}
```

**Usage:**

```java
// Before
ImGuiContext context = ImGui.getCurrentContext();
if(!MineGuiCore.

isInitialized() ||context ==null||context.

isNotValidPtr()){
        return;
        }

doStuff();

// After
ContextGuard.

ifReady(() ->

doStuff());

// Or for early return pattern
        if(!ContextGuard.

isReady()){
        return;
        }

doStuff();
```

---

## Summary

### Strengths

- Clean multi-namespace architecture
- Excellent thread safety practices
- Well-designed builder patterns (`Window`, `StyleDelta`)
- Good use of AutoCloseable for `StyleScope`
- Proper separation between public API and implementation

### Priority Improvements

| Priority | Item                                   | Impact                                            | Status |
|----------|----------------------------------------|---------------------------------------------------|--------|
| High     | Add `IdScope`, `DisabledScope`         | Reduces boilerplate, prevents push/pop mismatches | [ ]    |
| High     | Add `FloatRef` / `IntRef`              | Eliminates holder array ceremony                  | [ ]    |
| Medium   | Add `StyleColorScope`, `StyleVarScope` | Consistent with existing `StyleScope` pattern     | [ ]    |
| Medium   | Add `ContextGuard`                     | Deduplicates repeated validity checks             | [ ]    |
| ~~Low~~  | ~~Refactor style system duplication~~  | ~~Code maintainability (future)~~                 | [X]    |
| ~~Low~~  | ~~Split GlobalConfigManager~~          | ~~Single responsibility (future)~~               | [X]    |

### Alignment with MineGui Philosophy

The proposed utilities:

- Are **zero-overhead** abstractions (no runtime cost beyond what ImGui requires)
- Are **optional** - devs can still use raw ImGui calls
- Follow **existing patterns** (`StyleScope`, `Window`)
- Keep the library **lightweight** - each utility is a single-purpose class
- Reduce **cognitive load** - no need to count push/pop pairs
