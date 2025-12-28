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

## P1 - High-Impact Quick Wins (COMPLETED)

### 5. ~~Cache `hasVisibleViews()` Result~~ ✅ DONE
**Location:** `manager/UIManager.java:142`

**Problem:** Called every frame, iterates all views with `stream().anyMatch()`.

**Solution Implemented:** Added `cachedHasVisibleViews` flag with invalidation on visibility changes.

---

### 6. ~~Add ThreadLocal Cleanup Hooks~~ ✅ DONE
**Location:** `style/StyleManager.java:29-30`

**Problem:** ThreadLocal stacks (`styleStack`, `activeFont`) never cleaned on thread death.

**Solution Implemented:** Added `StyleManager.cleanup()` method, integrated into teardown flow.

---

### 7. ~~Replace `MineGuiInitializationOptions` Boilerplate with Lombok~~ ✅ DONE
**Location:** `MineGuiInitializationOptions.java:59-118`

**Problem:** 12 nearly identical `withXxx()` methods.

**Solution Implemented:** Converted to Lombok `@With` annotation on record components.

---

## P2 - Code Quality Improvements (COMPLETED)

### 9. ~~Extract Layout Helper from Debug Module~~ ✅ DONE
**Location:** `debug/.../DebugLayout.java`

**Problem:** Useful spacing utilities only available in debug module.

**Solution Implemented:**
- Created `tytoo.minegui.helper.LayoutHelper` with `sectionGap()`, `smallGap()`, `tinyGap()`, `verticalSpace()`, `horizontalSpace()`
- Updated `DebugLayout` to delegate to `LayoutHelper`

---

### 10. ~~Consolidate Sanitize Methods~~ ✅ DONE
**Location:** `imgui/dock/DockspaceRenderState.java:80-92`

**Problem:** `sanitizeFinite()`, `sanitizeDimension()`, `sanitizeNonNegative()` have overlapping logic.

**Solution Implemented:**
- Created `tytoo.minegui.util.MathUtils` with `clampFinite()` and `clampNonNegative()`
- Updated `DockspaceRenderState.normalize()` to use `MathUtils`

---

### 11. ~~Replace Switch with Map in `InputHelper`~~ ✅ DONE
**Location:** `util/InputHelper.java:53-91`

**Problem:** Large switch statement for key name mapping.

**Solution Implemented:**
- Created static `Map<String, Integer> KEY_NAME_MAP` with all key mappings
- Replaced switch with `KEY_NAME_MAP.getOrDefault(keyName, localKeyCode)`

---

### 12. ~~Add Table Row Helper~~ ✅ DONE
**Location:** Multiple sections in debug module use identical table row patterns

**Solution Implemented:**
- Created `tytoo.minegui.helper.TableHelper` with `row()` and `rowWrapped()` methods

---

### 13. ~~Unify `ResourceId` and `Identifier` Usage~~ ✅ DONE
**Problem:** Some APIs expose both types, creating confusion.

**Solution Implemented:**
- Deprecated `View.useStyle(Identifier)` with `@Deprecated(forRemoval = true)`
- `MinecraftIdentifiers` already provides clean conversion utilities

---

## P3 - Nice-to-Have Cleanups (COMPLETED)

### 14. ~~Remove Unused Fields~~ ✅ DONE
| Location | Issue | Fix |
|----------|-------|-----|
| `Window.java:173` | `WindowState(String title)` doesn't use `title` | Removed unused parameter |
| `ImGuiUtils.java:11` | `IM_GUI_INSTANCE` naming | Renamed to `IMGUI`, added `cleanup()` method |

### 15. ~~Improve Error Handling Specificity~~ ✅ DONE
**Location:** `StyleJsonSerializer.java:199-200`

**Problem:** Catches `RuntimeException` for JSON parsing - overly broad.

**Solution Implemented:** Catch specific exceptions: `NumberFormatException | IllegalStateException | UnsupportedOperationException`

### 16. ~~Add Memory Cleanup for Window State~~ ✅ DONE
**Location:** `helper/window/Window.java:15`

**Problem:** `STATE_BY_TITLE` ConcurrentHashMap never cleaned.

**Solution Implemented:** Added `Window.disposeAll()` method for bulk cleanup.

### 17. ~~Normalize Line Endings~~ ✅ DONE
**Problem:** Mixed CRLF/LF in debug module files.

**Solution Implemented:** `.gitattributes` already has `* text=auto`; ran `git add --renormalize .`

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

**Phase 2 - Quick Wins (P1)** ✅ COMPLETED
5. ~~Cache `hasVisibleViews()`~~ ✅
6. ~~Add ThreadLocal cleanup~~ ✅
7. ~~Lombok for `MineGuiInitializationOptions`~~ ✅

**Phase 3 - Polish (P2/P3)** ✅ COMPLETED
8. ~~Extract `LayoutHelper` from debug module~~ ✅
9. ~~Consolidate sanitize methods to `MathUtils`~~ ✅
10. ~~Replace switch with Map in `InputHelper`~~ ✅
11. ~~Add `TableHelper` row utilities~~ ✅
12. ~~Unify `ResourceId`/`Identifier` usage~~ ✅
13. ~~Remove unused fields in `Window` and `ImGuiUtils`~~ ✅
14. ~~Improve error handling in `StyleJsonSerializer`~~ ✅
15. ~~Add memory cleanup for `Window.STATE_BY_TITLE`~~ ✅
16. ~~Normalize line endings~~ ✅

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
| `UIManager.java` | 268 | ✅ Optimized (P1) |
| `OverviewSection.java` | 260 | Debug, acceptable |
| `MineGuiInitializationOptions.java` | 225 | ✅ Lombok refactored (P1) |
| ~~`CursorPolicyRegistry.java`~~ | ~~199~~ | ✅ REFACTORED to ~80 lines (split to CursorUnlockManager) |
| `ViewPersistenceManager.java` | 191 | Acceptable |
| `FontLibrary.java` | 190 | Acceptable |
| `Window.java` | 180 | ✅ Cleaned (P3) |
| `LayoutShowcaseSection.java` | 175 | Debug, acceptable |
| `StyleJsonSerializer.java` | 165 | ✅ Error handling improved (P3) |
| `View.java` | 155 | Clean |
| `StyleWorkflowSection.java` | 150 | Debug |
| `InputRouter.java` | 144 | Clean |
| `InputHelper.java` | ~100 | ✅ Map-based (P2) |
| `DockspaceRenderState.java` | ~360 | ✅ Uses MathUtils (P2) |
| `MineGuiCore.java` | 142 | Clean |
| `ConfigState.java` | 140 | Minor cleanup (P2) |
