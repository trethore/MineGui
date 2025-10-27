package tytoo.minegui.view.cursor;

import tytoo.minegui.runtime.cursor.CursorPolicyRegistry;
import tytoo.minegui.view.MGView;

public final class MGCursorPolicies {
    private static final MGCursorPolicy EMPTY = new MGCursorPolicy() {
        @Override
        public void onOpen(MGView view) {
        }

        @Override
        public void onClose(MGView view) {
        }
    };

    private static final MGCursorPolicy SCREEN = new MGCursorPolicy() {
        @Override
        public void onOpen(MGView view) {
            CursorPolicyRegistry.requestPersistentUnlock(view);
        }

        @Override
        public void onClose(MGView view) {
            CursorPolicyRegistry.releasePersistentUnlock(view);
        }
    };

    private static final MGCursorPolicy CLICK_TO_LOCK = new MGCursorPolicy() {
        @Override
        public void onOpen(MGView view) {
            CursorPolicyRegistry.requestClickReleaseUnlock(view);
        }

        @Override
        public void onClose(MGView view) {
            CursorPolicyRegistry.releaseClickReleaseUnlock(view);
        }
    };

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
}
