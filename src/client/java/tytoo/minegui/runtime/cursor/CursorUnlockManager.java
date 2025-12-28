package tytoo.minegui.runtime.cursor;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiFocusedFlags;
import tytoo.minegui.imgui.ContextGuard;
import tytoo.minegui.util.CursorLockUtils;
import tytoo.minegui.view.View;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CursorUnlockManager {
    private static final boolean[] EMPTY_MOUSE_BUTTONS = new boolean[5];
    private static final Set<View> PERSISTENT_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<View> CLICK_RELEASE_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<View> CLICK_RELEASE_REGISTERED = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static volatile boolean cursorUnlocked;

    private CursorUnlockManager() {
    }

    public static void requestPersistentUnlock(View view) {
        if (view == null) {
            return;
        }
        PERSISTENT_UNLOCKS.add(view);
        refreshState();
    }

    public static void releasePersistentUnlock(View view) {
        if (view == null) {
            return;
        }
        PERSISTENT_UNLOCKS.remove(view);
        refreshState();
    }

    public static void requestClickReleaseUnlock(View view) {
        if (view == null) {
            return;
        }
        CLICK_RELEASE_REGISTERED.add(view);
        CLICK_RELEASE_UNLOCKS.add(view);
        refreshState();
    }

    public static void releaseClickReleaseUnlock(View view) {
        if (view == null) {
            return;
        }
        CLICK_RELEASE_REGISTERED.remove(view);
        CLICK_RELEASE_UNLOCKS.remove(view);
        refreshState();
    }

    public static void releaseClickReleaseForWorldInteraction() {
        if (CLICK_RELEASE_UNLOCKS.isEmpty()) {
            return;
        }
        CLICK_RELEASE_UNLOCKS.clear();
        clearImGuiFocus();
        refreshState();
    }

    public static void onScreenClosed() {
        if (!CursorLockUtils.clientWantsLockCursor()) {
            return;
        }
        if (!ContextGuard.isReady()) {
            CLICK_RELEASE_UNLOCKS.clear();
            suppressImGuiInput();
            relockIfNecessary();
            return;
        }
        if (!CLICK_RELEASE_UNLOCKS.isEmpty()) {
            return;
        }
        if (ImGui.isWindowFocused(ImGuiFocusedFlags.AnyWindow)) {
            return;
        }
        if (ImGui.isAnyItemActive()) {
            return;
        }
        clearImGuiFocus();
        refreshState();
    }

    public static boolean wantsImGuiInput() {
        return !PERSISTENT_UNLOCKS.isEmpty() || !CLICK_RELEASE_UNLOCKS.isEmpty();
    }

    public static boolean shouldBlockLockRequest() {
        return wantsImGuiInput();
    }

    public static void ensureUnlockedIfRequested() {
        if (!wantsImGuiInput()) {
            return;
        }
        if (!cursorUnlocked || CursorLockUtils.isCursorLocked()) {
            CursorLockUtils.applyCursorLock(false);
            cursorUnlocked = true;
        }
    }

    public static void onFrameStart() {
        restoreClickReleaseUnlocksIfInactive();
        if (wantsImGuiInput()) {
            ensureUnlockedIfRequested();
            return;
        }
        suppressImGuiInput();
        relockIfNecessary();
    }

    private static void restoreClickReleaseUnlocksIfInactive() {
        if (!CLICK_RELEASE_UNLOCKS.isEmpty()) {
            return;
        }
        if (CLICK_RELEASE_REGISTERED.isEmpty()) {
            return;
        }
        if (CursorLockUtils.clientWantsLockCursor()) {
            return;
        }
        for (View view : CLICK_RELEASE_REGISTERED) {
            if (view == null || !view.isVisible()) {
                continue;
            }
            CLICK_RELEASE_UNLOCKS.add(view);
        }
    }

    private static void refreshState() {
        if (wantsImGuiInput()) {
            ensureUnlockedIfRequested();
            return;
        }
        relockIfNecessary();
    }

    private static void relockIfNecessary() {
        if (CursorLockUtils.clientWantsLockCursor()) {
            CursorLockUtils.applyCursorLock(true);
        }
        cursorUnlocked = false;
    }

    private static void suppressImGuiInput() {
        if (!ContextGuard.isReady()) {
            return;
        }
        ImGuiIO io = ImGui.getIO();
        io.setMousePos(-Float.MAX_VALUE, -Float.MAX_VALUE);
        io.setMouseDown(EMPTY_MOUSE_BUTTONS);
        io.setWantCaptureMouse(false);
        io.setWantCaptureKeyboard(false);
        io.setWantTextInput(false);
    }

    private static void clearImGuiFocus() {
        if (!ContextGuard.isReady()) {
            return;
        }
        ImGui.setWindowFocus(null);
    }
}
