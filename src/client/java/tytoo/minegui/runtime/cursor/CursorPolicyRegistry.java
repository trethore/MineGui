package tytoo.minegui.runtime.cursor;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiFocusedFlags;
import imgui.internal.ImGuiContext;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.CursorLockUtils;
import tytoo.minegui.view.MGView;
import tytoo.minegui.view.cursor.MGCursorPolicy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CursorPolicyRegistry {
    private static final Map<Identifier, MGCursorPolicy> REGISTERED_POLICIES = new ConcurrentHashMap<>();
    private static final Set<MGView> PERSISTENT_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<MGView> CLICK_RELEASE_UNLOCKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<MGView> CLICK_RELEASE_REGISTERED = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final boolean[] EMPTY_MOUSE_BUTTONS = new boolean[5];
    private static volatile boolean cursorUnlocked;

    private CursorPolicyRegistry() {
    }

    public static void registerPolicy(Identifier id, MGCursorPolicy policy) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(policy, "policy");
        MGCursorPolicy existing = REGISTERED_POLICIES.putIfAbsent(id, policy);
        if (existing != null && existing != policy) {
            MineGuiCore.LOGGER.warn("MineGui cursor policy '{}' is already registered; skipping duplicate registration.", id);
        }
    }

    public static MGCursorPolicy resolvePolicy(Identifier id) {
        if (id == null) {
            return null;
        }
        return REGISTERED_POLICIES.get(id);
    }

    public static MGCursorPolicy resolvePolicyOrDefault(Identifier id, MGCursorPolicy fallback) {
        MGCursorPolicy policy = resolvePolicy(id);
        return policy != null ? policy : fallback;
    }

    public static Set<Identifier> registeredPolicies() {
        return Collections.unmodifiableSet(new HashSet<>(REGISTERED_POLICIES.keySet()));
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
        CLICK_RELEASE_REGISTERED.add(view);
        CLICK_RELEASE_UNLOCKS.add(view);
        refreshState();
    }

    public static void releaseClickReleaseUnlock(MGView view) {
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
        for (MGView view : CLICK_RELEASE_REGISTERED) {
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
