# Cursor & Input
MineGui coordinates cursor locks and input capture so ImGui windows feel native inside Minecraft. This page covers the built-in policies, custom extensions, and the input lifecycle that keeps the client responsive.

## What this page covers
- Using the bundled cursor policies (`empty`, `screen`, `click_to_lock`)
- Registering custom policies through `CursorPolicyRegistry`
- Understanding the render-loop input checks and visibility gates
- Troubleshooting tips when input stops reaching your views

## Built-in Policies
Each `MGView` holds a cursor policy that dictates how MineGui unlocks the cursor and forwards events to ImGui.

- `MGCursorPolicies.empty()` — No special handling; ImGui only captures input when the view is focused.
- `MGCursorPolicies.screen()` — Unlocks the cursor while the view is visible, ideal for editors and inspectors.
- `MGCursorPolicies.clickToLock()` — Unlocks until the player clicks the world again (default namespace policy).

```java
public final class PaletteView extends MGView {
    public PaletteView() {
        super("example/palette", true);
        setCursorPolicy(MGCursorPolicies.screen()); // keep mouse free while the palette is open
    }

    @Override
    protected void renderView() {
        ImGui.begin(scopedWindowTitle("Palette"));
        ImGui.text("Pick a theme and start painting!");
        ImGui.end();
    }
}
```

`UIManager.setDefaultCursorPolicy(...)` applies a policy to every registered view that has not set one explicitly.

```java
UIManager.get("examplemod").setDefaultCursorPolicy(MGCursorPolicies.clickToLock());
```

## Custom Policies
Create your own cursor policies when you need domain-specific behaviour, such as holding the cursor unlocked while a modal workflow runs or re-locking after an asynchronous event.

```java
Identifier modalId = Identifier.of("examplemod", "modal");

MGCursorPolicy modalPolicy = MGCursorPolicy.of(
        view -> {
            CursorPolicyRegistry.requestPersistentUnlock(view);
            CursorLockUtils.blockNextLockRequest(); // optional helper if you coordinate with vanilla screens
        },
        CursorPolicyRegistry::releasePersistentUnlock
);

CursorPolicyRegistry.registerPolicy(modalId, modalPolicy);

view.setCursorPolicy(modalPolicy);
```

- The `onOpen` callback executes whenever the view toggles visibility on, while the `onClose` callback is invoked both when the view hides and during destruction.
- Use `CursorPolicyRegistry.resolvePolicyOrDefault(id, fallback)` when loading policies from config to avoid missing references.

## Input Lifecycle
MineGui bridges input through several checkpoints each frame:

1. `MineGuiNamespaces.anyVisible()` runs before the render loop; if any view is visible, MineGui keeps the cursor unlocked as needed.
2. `CursorPolicyRegistry.onFrameStart()` executes at the start of each ImGui frame to restore click-release state or relock if no view requests input.
3. `ImGuiLoader.onFrameRender()` updates the ImGui IO flags and hands control to each namespace’s `UIManager`.
4. Mixins in `runtime` and `input` packages prevent Minecraft from reclaiming the cursor while a policy wants it unlocked.

When you work with raw GLFW callbacks or custom keybinds, check MineGui’s IO state to avoid conflicts:

```java
public boolean onKeyPressed(int keyCode) {
    if (ImGui.getIO().getWantCaptureKeyboard()) {
        return true; // let ImGui handle the key
    }
    // fall back to vanilla behaviour
    return false;
}
```

## Troubleshooting Input Issues
- Ensure at least one view with `isVisible()` true is registered; MineGui only unlocks the cursor while views are on-screen.
- If `/minegui reload` stops input, verify that your namespace reinitializes any custom policies after the reload sequence.
- For click-to-lock workflows, confirm that no other screen or keybind calls `CursorLockUtils.applyCursorLock(true)` prematurely.
- Inspect logs from `MineGuiCore.LOGGER`—duplicate policy registrations or late font loads surface warnings that affect input handling.

---

**Next:** [Layout & Helpers](layout-and-helpers.md) • **Back:** [Developer Docs Home](README.md)
