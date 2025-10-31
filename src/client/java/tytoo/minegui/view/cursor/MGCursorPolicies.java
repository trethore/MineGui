package tytoo.minegui.view.cursor;

import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;

public final class MGCursorPolicies {
    private static final Identifier EMPTY_ID = Identifier.of(MineGuiCore.ID, "empty");
    private static final Identifier SCREEN_ID = Identifier.of(MineGuiCore.ID, "screen");
    private static final Identifier CLICK_TO_LOCK_ID = Identifier.of(MineGuiCore.ID, "click_to_lock");

    private static final MGCursorPolicy EMPTY = MGCursorPolicy.of(null, null);
    private static final MGCursorPolicy SCREEN = MGCursorPolicy.of(CursorPolicyRegistry::requestPersistentUnlock, CursorPolicyRegistry::releasePersistentUnlock);
    private static final MGCursorPolicy CLICK_TO_LOCK = MGCursorPolicy.of(CursorPolicyRegistry::requestClickReleaseUnlock, CursorPolicyRegistry::releaseClickReleaseUnlock);

    static {
        CursorPolicyRegistry.registerPolicy(EMPTY_ID, EMPTY);
        CursorPolicyRegistry.registerPolicy(SCREEN_ID, SCREEN);
        CursorPolicyRegistry.registerPolicy(CLICK_TO_LOCK_ID, CLICK_TO_LOCK);
    }

    private MGCursorPolicies() {
    }

    public static MGCursorPolicy empty() {
        return EMPTY;
    }

    public static MGCursorPolicy screen() {
        return SCREEN;
    }

    public static MGCursorPolicy clickToLock() {
        return CLICK_TO_LOCK;
    }

    public static Identifier emptyId() {
        return EMPTY_ID;
    }

    public static Identifier screenId() {
        return SCREEN_ID;
    }

    public static Identifier clickToLockId() {
        return CLICK_TO_LOCK_ID;
    }
}
