package tytoo.minegui.runtime.cursor;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiFocusedFlags;
import imgui.internal.ImGuiContext;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.CursorLockUtils;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CursorPolicyRegistry {
    private static final Map<ResourceId, CursorPolicy> REGISTERED_POLICIES = new ConcurrentHashMap<>();
    private static final Set<View> PERSISTENT_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<View> CLICK_RELEASE_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<View> CLICK_RELEASE_REGISTERED = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final boolean[] EMPTY_MOUSE_BUTTONS = new boolean[5];
    private static volatile boolean cursorUnlocked;

    private CursorPolicyRegistry() {
    }

    public static void registerPolicy(ResourceId id, CursorPolicy policy) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(policy, "policy");
        CursorPolicy existing = REGISTERED_POLICIES.putIfAbsent(id, policy);
        if (existing != null && existing != policy) {
            MineGuiCore.LOGGER.warn("MineGui cursor policy '{}' is already registered; skipping duplicate registration.", id);
        }
    }

    public static CursorPolicy resolvePolicy(ResourceId id) {
        if (id == null) {
            return null;
        }
        return REGISTERED_POLICIES.get(id);
    }

    public static CursorPolicy resolvePolicyOrDefault(ResourceId id, CursorPolicy fallback) {
        CursorPolicy policy = resolvePolicy(id);
        return policy != null ? policy : fallback;
    }

    public static Set<ResourceId> registeredPolicies() {
        return Set.copyOf(REGISTERED_POLICIES.keySet());
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
        ImGuiContext context = ImGui.getCurrentContext();
        if (!MineGuiCore.isInitialized() || context == null || context.isNotValidPtr()) {
            CLICK_RELEASE_UNLOCKS.clear();
            suppressImGuiInput();
            relockIfNecessary();
            return;
        }
        if (ImGui.isWindowFocused(ImGuiFocusedFlags.AnyWindow)) {
            return;
        }
        if (ImGui.isAnyItemActive()) {
            return;
        }
        CLICK_RELEASE_UNLOCKS.clear();
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
        ImGuiContext context = ImGui.getCurrentContext();
        if (!MineGuiCore.isInitialized() || context == null || context.isNotValidPtr()) {
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
        ImGuiContext context = ImGui.getCurrentContext();
        if (!MineGuiCore.isInitialized() || context == null || context.isNotValidPtr()) {
            return;
        }
        ImGui.setWindowFocus(null);
    }
}
