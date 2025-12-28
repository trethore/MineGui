package tytoo.minegui.runtime.cursor;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicy;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CursorPolicyRegistry {
    private static final Map<ResourceId, CursorPolicy> REGISTERED_POLICIES = new ConcurrentHashMap<>();

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
        CursorUnlockManager.requestPersistentUnlock(view);
    }

    public static void releasePersistentUnlock(View view) {
        CursorUnlockManager.releasePersistentUnlock(view);
    }

    public static void requestClickReleaseUnlock(View view) {
        CursorUnlockManager.requestClickReleaseUnlock(view);
    }

    public static void releaseClickReleaseUnlock(View view) {
        CursorUnlockManager.releaseClickReleaseUnlock(view);
    }

    public static void releaseClickReleaseForWorldInteraction() {
        CursorUnlockManager.releaseClickReleaseForWorldInteraction();
    }

    public static void onScreenClosed() {
        CursorUnlockManager.onScreenClosed();
    }

    public static boolean wantsImGuiInput() {
        return CursorUnlockManager.wantsImGuiInput();
    }

    public static boolean shouldBlockLockRequest() {
        return CursorUnlockManager.shouldBlockLockRequest();
    }

    public static void ensureUnlockedIfRequested() {
        CursorUnlockManager.ensureUnlockedIfRequested();
    }

    public static void onFrameStart() {
        CursorUnlockManager.onFrameStart();
    }
}
