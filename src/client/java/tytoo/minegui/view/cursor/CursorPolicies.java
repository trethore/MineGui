package tytoo.minegui.view.cursor;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.util.ResourceId;

public final class CursorPolicies {
    private static final ResourceId EMPTY_ID = ResourceId.of(MineGuiCore.ID, "empty");
    private static final ResourceId SCREEN_ID = ResourceId.of(MineGuiCore.ID, "screen");
    private static final ResourceId CLICK_TO_LOCK_ID = ResourceId.of(MineGuiCore.ID, "click_to_lock");

    private static final CursorPolicy EMPTY = CursorPolicy.of(null, null);
    private static final CursorPolicy SCREEN = CursorPolicy.of(CursorPolicyRegistry::requestPersistentUnlock, CursorPolicyRegistry::releasePersistentUnlock);
    private static final CursorPolicy CLICK_TO_LOCK = CursorPolicy.of(CursorPolicyRegistry::requestClickReleaseUnlock, CursorPolicyRegistry::releaseClickReleaseUnlock);

    static {
        CursorPolicyRegistry.registerPolicy(EMPTY_ID, EMPTY);
        CursorPolicyRegistry.registerPolicy(SCREEN_ID, SCREEN);
        CursorPolicyRegistry.registerPolicy(CLICK_TO_LOCK_ID, CLICK_TO_LOCK);
    }

    private CursorPolicies() {
    }

    public static CursorPolicy empty() {
        return EMPTY;
    }

    public static CursorPolicy screen() {
        return SCREEN;
    }

    public static CursorPolicy clickToLock() {
        return CLICK_TO_LOCK;
    }

    public static ResourceId emptyId() {
        return EMPTY_ID;
    }

    public static ResourceId screenId() {
        return SCREEN_ID;
    }

    public static ResourceId clickToLockId() {
        return CLICK_TO_LOCK_ID;
    }
}
