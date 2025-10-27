package tytoo.minegui.runtime.cursor;

import imgui.ImGui;
import imgui.ImGuiIO;
import tytoo.minegui.util.CursorLockUtils;
import tytoo.minegui.view.MGView;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CursorPolicyRegistry {
    private static final Set<MGView> PERSISTENT_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<MGView> CLICK_RELEASE_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final boolean[] EMPTY_MOUSE_BUTTONS = new boolean[5];

    private static volatile boolean cursorUnlocked;

    private CursorPolicyRegistry() {
    }

    public static void requestPersistentUnlock(MGView view) {
        if (view == null) {
            return;
        }
        PERSISTENT_UNLOCKS.add(view);
        refreshState();
    }

    public static void releasePersistentUnlock(MGView view) {
        if (view == null) {
            return;
        }
        PERSISTENT_UNLOCKS.remove(view);
        refreshState();
    }

    public static void requestClickReleaseUnlock(MGView view) {
        if (view == null) {
            return;
        }
        CLICK_RELEASE_UNLOCKS.add(view);
        refreshState();
    }

    public static void releaseClickReleaseUnlock(MGView view) {
        if (view == null) {
            return;
        }
        CLICK_RELEASE_UNLOCKS.remove(view);
        refreshState();
    }

    public static void releaseClickReleaseForWorldInteraction() {
        if (CLICK_RELEASE_UNLOCKS.isEmpty()) {
            return;
        }
        CLICK_RELEASE_UNLOCKS.clear();
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
        if (!cursorUnlocked) {
            CursorLockUtils.applyCursorLock(false);
            cursorUnlocked = true;
        }
    }

    public static void onFrameStart() {
        if (wantsImGuiInput()) {
            ensureUnlockedIfRequested();
            return;
        }
        suppressImGuiInput();
        relockIfNecessary();
    }

    private static void refreshState() {
        if (wantsImGuiInput()) {
            ensureUnlockedIfRequested();
            return;
        }
        relockIfNecessary();
    }

    private static void relockIfNecessary() {
        if (cursorUnlocked && CursorLockUtils.clientWantsLockCursor()) {
            CursorLockUtils.applyCursorLock(true);
        }
        cursorUnlocked = false;
    }

    private static void suppressImGuiInput() {
        if (ImGui.getCurrentContext() == null) {
            return;
        }
        ImGuiIO io = ImGui.getIO();
        io.setMousePos(-Float.MAX_VALUE, -Float.MAX_VALUE);
        io.setMouseDown(EMPTY_MOUSE_BUTTONS);
        io.setWantCaptureMouse(false);
        io.setWantCaptureKeyboard(false);
        io.setWantTextInput(false);
    }
}
