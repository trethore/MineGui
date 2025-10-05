# MineGui Architecture

## Overview

MineGui is now built on a composable, trait-based architecture that emphasizes:
- **Fluent Builder Pattern**: Every component supports method chaining
- **Trait-based Composition**: Shared behaviors through interfaces with default implementations
- **Hierarchical Windows**: Parent/child relationships with automatic lifecycle propagation
- **Extensible Core**: Plugin system for custom components and behaviors
- **Auto-registration**: No manual UIManager registration required

## Core Architecture

### 1. Trait System (`tytoo.minegui.component.traits`)

Traits eliminate code duplication by providing shared behaviors:

```java
// Textable - for components that display text
interface Textable<T> {
    T text(String text);
    T bindText(State<String> state);
    String getText();
}

// Clickable - for interactive components
interface Clickable<T> {
    T onClick(Runnable action);
    void performClick();
}

// Disableable - for components that can be disabled
interface Disableable<T> {
    T disabled(boolean disabled);
    T enabled(boolean enabled);
}

// Scalable - for components that support scaling
interface Scalable<T> {
    T scale(float scale);
}
```

### 2. Behavior System (`tytoo.minegui.component.behavior`)

Attach behaviors to any component without inheritance:

```java
interface Behavior<T extends MGComponent<?>> {
    void onAttach(T component);
    void preRender(T component);
    void postRender(T component);
}

// Built-in behaviors
TooltipBehavior.of("Hover text")
```

Usage:
```java
MGButton.of("Click me")
    .behavior(TooltipBehavior.of("This is a tooltip"))
    .behavior(new CustomAnimationBehavior())
    .parent(window);
```

### 3. Window Hierarchy

Windows support parent/child relationships with automatic visibility propagation:

```java
public enum VisibilityMode {
    FOLLOW_PARENT,  // Default: show/hide with parent
    INDEPENDENT,    // Ignore parent state
    INVERSE         // Opposite of parent
}

// Usage
MainWindow main = new MainWindow();  // Auto-registers
main.addSubWindow(
    SettingsWindow.create()
        .visibilityMode(VisibilityMode.FOLLOW_PARENT)
);
```

When you close the main window, all sub-windows with `FOLLOW_PARENT` mode automatically close too.

### 4. Fluent Builder API

Every component supports chainable methods:

```java
MGButton.of("Click")
    .pos(10, 10)              // Position shortcut
    .size(100, 30)            // Size shortcut
    .onClick(() -> {...})     // Event handler
    .disabled(false)          // State
    .behavior(...)            // Attach behavior
    .parent(window);          // Set parent
```

Full constraint API still available:
```java
MGButton.of("Centered")
    .width(Constraints.pixels(200))
    .height(Constraints.aspect(16f/9f))
    .x(Constraints.center())
    .y(Constraints.relative(0.5f, -50f))
    .parent(window);
```

### 5. Component Registry

Register custom components from external mods:

```java
// In your mod
ComponentRegistry.getInstance().register(
    "mymod:custom_slider",
    () -> new CustomSlider()
);

// Use it
MGComponent<?> slider = ComponentRegistry.getInstance()
    .create("mymod:custom_slider");
```

## Before vs After

### Creating a Button

**Before:**
```java
MGButton button = MGButton.of("Click");
button.constraints().setX(Constraints.pixels(10));
button.constraints().setY(Constraints.pixels(10));
button.constraints().setWidth(Constraints.pixels(100));
button.constraints().setHeight(Constraints.pixels(30));
button.onPress(() -> System.out.println("Clicked"));
button.setParent(window);
```

**After:**
```java
MGButton.of("Click")
    .pos(10, 10)
    .size(100, 30)
    .onClick(() -> System.out.println("Clicked"))
    .parent(window);
```

### Window Management

**Before:**
```java
TestWindow window = new TestWindow();
UIManager.getInstance().registerWindow(window);  // Manual registration
```

**After:**
```java
TestWindow window = new TestWindow();  // Auto-registers
```

### Component with Behavior

**Before:**
```java
// Had to subclass or modify component
class CustomButton extends MGButton {
    @Override
    public void render() {
        super.render();
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Tooltip");
        }
    }
}
```

**After:**
```java
MGButton.of("Click")
    .behavior(TooltipBehavior.of("Tooltip"))
    .parent(window);
```

## Creating Custom Components

### Using Traits

```java
public class MySlider extends MGComponent<MySlider>
        implements Stateful<Float, MySlider>, Disableable<MySlider> {

    private State<Float> state;
    private boolean disabled = false;

    public static MySlider create(float min, float max) {
        return new MySlider(min, max);
    }

    // Implement trait methods
    @Override
    public State<Float> getState() { return state; }

    @Override
    public void setState(State<Float> state) { this.state = state; }

    @Override
    public boolean isDisabled() { return disabled; }

    @Override
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    // Render logic
    @Override
    public void render() {
        // ImGui slider implementation
    }
}

// Usage
MySlider.create(0, 100)
    .state(volumeState)
    .disabled(false)
    .parent(window);
```

### Creating Custom Behaviors

```java
public class FadeInBehavior implements Behavior<MGComponent<?>> {
    private float alpha = 0f;

    @Override
    public void preRender(MGComponent<?> component) {
        if (alpha < 1f) {
            alpha += 0.02f;
            ImGui.pushStyleVar(ImGuiStyleVar.Alpha, alpha);
        }
    }

    @Override
    public void postRender(MGComponent<?> component) {
        if (alpha < 1f) {
            ImGui.popStyleVar();
        }
    }
}

// Use it
MGButton.of("Fade In")
    .behavior(new FadeInBehavior())
    .parent(window);
```

## Creating Windows

### Basic Window

```java
public class MyWindow extends MGWindow {
    public MyWindow() {
        super("My Window", true);  // true = auto-register
        initialize();
    }

    @Override
    protected void build() {
        super.build();
        this.initialBounds(100, 100, 400, 300);

        MGButton.of("Click")
            .pos(10, 10)
            .size(100, 30)
            .parent(this);
    }
}
```

### Window with Sub-windows

```java
public class MainWindow extends MGWindow {
    public MainWindow() {
        super("Main", true);
        initialize();
    }

    @Override
    protected void build() {
        super.build();

        // Add sub-window that follows parent visibility
        addSubWindow(
            SettingsWindow.create()
                .visibilityMode(VisibilityMode.FOLLOW_PARENT)
        );

        // Button to open sub-window
        MGButton.of("Settings")
            .onClick(() -> {
                for (MGWindow sub : getSubWindows()) {
                    if (sub instanceof SettingsWindow) {
                        sub.open();
                    }
                }
            })
            .parent(this);
    }
}
```

## State Management

State is reactive and supports computed values:

```java
// Simple state
State<Integer> count = State.of(0);

// Computed state
State<String> message = State.computed(() ->
    "Count is: " + count.get()
);

// Bind to component
MGText.of(message).parent(window);

// Update triggers re-render
count.set(count.get() + 1);  // Text auto-updates
```

## Package Structure

```
tytoo.minegui/
├── component/
│   ├── MGComponent.java              # Base component with builder API
│   ├── traits/                       # Behavioral traits
│   │   ├── Textable.java
│   │   ├── Clickable.java
│   │   ├── Disableable.java
│   │   ├── Scalable.java
│   │   └── Stateful.java
│   ├── behavior/                     # Behavior system
│   │   ├── Behavior.java
│   │   ├── TooltipBehavior.java
│   │   ├── VisibilityMode.java
│   │   └── FocusMode.java
│   └── components/
│       ├── layout/
│       │   └── MGWindow.java         # Hierarchical windows
│       ├── interactive/
│       │   └── MGButton.java
│       └── display/
│           └── MGText.java
├── manager/
│   ├── UIManager.java                # Auto-registration
│   └── ComponentRegistry.java        # Plugin system
├── state/
│   ├── State.java
│   └── ComputedState.java
└── constraint/                       # Layout system
    └── ...
```

## Best Practices

1. **Use static factory methods**: `MGButton.of()` instead of `new MGButton()`
2. **Chain everything**: Every method returns `this` for chaining
3. **Auto-register windows**: Pass `true` to MGWindow constructor
4. **Leverage traits**: Implement traits for shared behavior
5. **Use behaviors for cross-cutting concerns**: Tooltips, animations, etc.
6. **Prefer composition over inheritance**: Use traits and behaviors
7. **State-bind reactive values**: Use `State.computed()` for derived values

## Migration Guide

### Update Window Classes

1. Change constructor to accept `autoRegister` parameter:
   ```java
   // Before
   public MyWindow() {
       super("title");
   }

   // After
   public MyWindow() {
       super("title", true);
       initialize();
   }
   ```

2. Move `build()` content to use fluent API:
   ```java
   // Before
   MGButton btn = MGButton.of("Click");
   btn.setParent(this);
   btn.constraints().setX(Constraints.pixels(10));

   // After
   MGButton.of("Click")
       .pos(10, 10)
       .parent(this);
   ```

3. Remove manual registration:
   ```java
   // Before
   MyWindow window = new MyWindow();
   UIManager.getInstance().registerWindow(window);

   // After
   MyWindow window = new MyWindow();  // Auto-registers
   ```

### Update Components

1. Replace `onPress()` with `onClick()`:
   ```java
   // Before
   button.onPress(() -> {...})

   // After
   button.onClick(() -> {...})
   ```

2. Replace `bind()` with `bindText()`:
   ```java
   // Before
   MGText.of("").bind(state)

   // After
   MGText.of(state)  // or .bindText(state)
   ```

## Future Extensions

The architecture supports:
- Custom constraint types
- Custom behaviors (drag & drop, animations)
- Theme system via behaviors
- Component factories via registry
- Window groups/tabs via hierarchy
- Modal dialogs via visibility modes
