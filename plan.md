# MineGui Refactoring Plan

## Executive Summary

This document outlines refactoring opportunities to improve performance, readability, code quality, and
maintainability for MineGui - a lightweight ImGui-based Minecraft modding library.

**Codebase Stats:**
- ~7,200 lines of Java across 86 files
- 11 packages in main library + 1 debug module
- Generally well-organized with consistent patterns

---

## Decisions Made

Based on project owner feedback:

| Question | Decision |
|----------|----------|
| Static vs instance-based | **Instance-based with composition over inheritance** |
| Backwards compatibility | **Not required** - lib is in development |
| Code generation | **Use Lombok** for reducing boilerplate |
| `LayoutHelper`/`TableHelper` | **Include in public API** |
| `IntRef` | **Already exists** at `imgui/ref/IntRef.java` |
| Line endings | **No enforcement** |
| Lombok vs manual | **Lombok preferred** to reduce boilerplate |
| Target priority | **P0 first** |
| Out of scope | **Nothing** |
| Debug module | **Stays separate** - test/visual debugging module |

---

## Priority Matrix

| Priority | Impact | Effort | Category |
|----------|--------|--------|----------|
| P0 | High | Medium | Critical architectural issues |
| P1 | High | Low-Medium | Quick wins with significant improvement |
| P2 | Medium | Low | Code quality improvements |
| P3 | Low | Low | Nice-to-have cleanups |

---

## P0 - Critical Architectural Refactors (COMPLETED)

### 1. ~~Split `GlobalConfigManager` God Class~~ ✅ DONE
**Location:** `config/GlobalConfigManager.java` (DELETED)

**Solution Implemented:**
- Deleted `GlobalConfigManager.java` (399 lines)
- Created `ConfigRegistry.java` (~100 lines) - static facade for namespace management
- Created `ConfigService.java` (~270 lines) - instance-based service per namespace
- Migrated all 8 dependent files to use new API

---

### 2. ~~Split `ImGuiLoader` Large Class~~ ✅ DONE
**Location:** `imgui/ImGuiLoader.java`

**Solution Implemented:**
- Refactored `ImGuiLoader.java` (388 → ~75 lines) - now thin coordinator
- Created `ImGuiContextManager.java` (~220 lines) - context lifecycle management
- Created `ImGuiRenderer.java` (~155 lines) - frame rendering coordination

---

### 3. ~~Eliminate `StyleDescriptor`/`StyleDelta` Duplication~~ ✅ ACCEPTABLE
**Location:** `style/StyleDescriptor.java`, `style/StyleDelta.java`

**Resolution:**
- Analyzed and found that core logic is already shared via `StylePropertyValues`
- Both classes delegate to `StylePropertyValues.Builder` for all builder methods
- The duplication is only in wrapper getter methods (nullable vs. with-defaults)
- Marked as acceptable - extracting further would add complexity without benefit

---

### 4. ~~Convert `CursorPolicyRegistry` to Instance-Based~~ ✅ DONE
**Location:** `runtime/cursor/CursorPolicyRegistry.java`

**Solution Implemented:**
- Refactored `CursorPolicyRegistry.java` (199 → ~80 lines) - policy registration only
- Created `CursorUnlockManager.java` (~160 lines) - unlock request management
- `CursorPolicyRegistry` now delegates unlock operations to `CursorUnlockManager`

---

## P1 - High-Impact Quick Wins

### 5. Cache `hasVisibleViews()` Result
**Location:** `manager/UIManager.java:142`

**Problem:** Called every frame, iterates all views with `stream().anyMatch()`.

**Fix:** Add cached visibility flag, invalidate on visibility change.

---

### 6. Add ThreadLocal Cleanup Hooks
**Location:** `style/StyleManager.java:29-30`

**Problem:** ThreadLocal stacks (`styleStack`, `activeFont`) never cleaned on thread death.

**Fix:** Add `StyleManager.cleanup()` method, call from teardown.

---

### 7. Replace `MineGuiInitializationOptions` Boilerplate with Lombok
**Location:** `MineGuiInitializationOptions.java:59-118`

**Problem:** 12 nearly identical `withXxx()` methods.

**Fix:** Use Lombok `@With` annotation on the record.

---

## P2 - Code Quality Improvements

### 9. Extract Layout Helper from Debug Module
**Location:** `debug/.../DebugLayout.java`

**Problem:** Useful spacing utilities only available in debug module.

**Proposed:**
```
tytoo.minegui.helper.LayoutHelper
    - sectionGap(), smallGap(), tinyGap()
    - horizontal(), vertical() spacing methods
```

---

### 10. Consolidate Sanitize Methods
**Location:** `imgui/dock/DockspaceRenderState.java:80-92`

**Problem:** `sanitizeFinite()`, `sanitizeDimension()`, `sanitizeNonNegative()` have overlapping logic.

**Fix:** Extract to `tytoo.minegui.util.MathUtils`:
```java
public static float clampFinite(float value, float fallback);
public static float clampPositive(float value, float fallback);
```

---

### 11. Replace Switch with Map in `InputHelper`
**Location:** `util/InputHelper.java:53-91`

**Problem:** Large switch statement for key name mapping.

**Fix:**
```java
private static final Map<Integer, String> KEY_NAMES = Map.ofEntries(
    Map.entry(GLFW.GLFW_KEY_SPACE, "Space"),
    Map.entry(GLFW.GLFW_KEY_ENTER, "Enter"),
    // ...
);
```

---

### 12. Add Table Row Helper
**Location:** Multiple sections in debug module use identical table row patterns

**Proposed:**
```java
// tytoo.minegui.helper.TableHelper
public static void row(Object... columns) {
    ImGui.tableNextRow();
    for (int i = 0; i < columns.length; i++) {
        ImGui.tableSetColumnIndex(i);
        ImGui.text(String.valueOf(columns[i]));
    }
}
```

---

### 13. Unify `ResourceId` and `Identifier` Usage
**Problem:** Some APIs expose both types, creating confusion.

**Proposed:**
- Keep `ResourceId` as the public API type
- Add conversion utilities in one place
- Deprecate methods accepting raw `Identifier`

---

## P3 - Nice-to-Have Cleanups

### 14. Remove Unused Fields
| Location | Issue |
|----------|-------|
| `Window.java:173` | `WindowState(String title)` doesn't use `title` |
| `ImGuiUtils.java:11` | `IM_GUI_INSTANCE` only used for one method |

### 15. Improve Error Handling Specificity
**Location:** `StyleJsonSerializer.java:199-200`

**Problem:** Catches `RuntimeException` for JSON parsing - overly broad.

**Fix:** Catch specific JSON exceptions, log with context.

### 16. Add Memory Cleanup for Window State
**Location:** `helper/window/Window.java:15`

**Problem:** `STATE_BY_TITLE` ConcurrentHashMap never cleaned.

**Fix:** Add `Window.clearState(String title)` or automatic cleanup.

### 17. Normalize Line Endings
**Problem:** Mixed CRLF/LF in debug module files.

**Fix:** Add `.editorconfig` with consistent line ending rules.

---

## Performance Considerations Summary

| Issue | Location | Impact | Fix |
|-------|----------|--------|-----|
| Stream every frame | `UIManager:142` | Medium | Cache result |
| String split per flush | `ViewPersistenceManager:196` | Low | Already throttled |
| New objects per scope chain | `StyleColorScope:22-29` | Low | Accept for readability |
| HashMap creation per style capture | `ColorPalette:42-44` | Low | Only on capture, acceptable |

---

## Thread Safety Summary

| Concern | Location | Risk |
|---------|----------|------|
| ThreadLocal not cleaned | `StyleManager:29-30` | Memory leak on mod reload |
| Non-atomic state access | `CursorPolicyRegistry:25` | Race condition possible |
| Heavy synchronized usage | `GlobalConfigManager` | Contention under load |
| Non-thread-safe lazy init | `StyleWorkflowSection:95` (debug) | Double registration |

---

## Suggested Implementation Order

Targeting P0 first as decided:

**Phase 1 - Architecture (P0)** ✅ COMPLETED
1. ~~Split `GlobalConfigManager` into smaller services~~ ✅
2. ~~Split `ImGuiLoader` responsibilities~~ ✅
3. ~~Convert `CursorPolicyRegistry` to instance-based~~ ✅
4. ~~Address `StyleDescriptor`/`StyleDelta` duplication~~ ✅ (acceptable as-is)

**Phase 2 - Quick Wins (P1)**
5. Cache `hasVisibleViews()`
6. Add ThreadLocal cleanup
7. Lombok for `MineGuiInitializationOptions`

**Phase 3 - Polish (P2/P3)**
8. Extract helpers from debug module
9. Consolidate utilities
10. Cleanup unused code

---

## Appendix: Files by Lines of Code (Top 20)

| File | Lines | Status |
|------|-------|--------|
| `StyleDescriptor.java` | 605 | Acceptable (shares core via StylePropertyValues) |
| `StyleDelta.java` | 498 | Acceptable (shares core via StylePropertyValues) |
| ~~`GlobalConfigManager.java`~~ | ~~399~~ | ✅ DELETED - replaced by ConfigRegistry + ConfigService |
| ~~`ImGuiLoader.java`~~ | ~~388~~ | ✅ REFACTORED to ~75 lines (split to ImGuiContextManager + ImGuiRenderer) |
| `StylePropertyValues.java` | 356 | Builder heavy, acceptable |
| `WidgetShowcaseSection.java` | 280 | Debug, acceptable |
| `UIManager.java` | 268 | Needs optimization (P1) |
| `OverviewSection.java` | 260 | Debug, acceptable |
| `MineGuiInitializationOptions.java` | 225 | Lombok candidate (P1) |
| ~~`CursorPolicyRegistry.java`~~ | ~~199~~ | ✅ REFACTORED to ~80 lines (split to CursorUnlockManager) |
| `ViewPersistenceManager.java` | 191 | Acceptable |
| `FontLibrary.java` | 190 | Acceptable |
| `Window.java` | 180 | Minor cleanup (P3) |
| `LayoutShowcaseSection.java` | 175 | Debug, extractable |
| `StyleJsonSerializer.java` | 165 | Minor fixes (P3) |
| `View.java` | 155 | Clean |
| `StyleWorkflowSection.java` | 150 | Debug |
| `InputRouter.java` | 144 | Clean |
| `MineGuiCore.java` | 142 | Clean |
| `ConfigState.java` | 140 | Minor cleanup (P2) |
