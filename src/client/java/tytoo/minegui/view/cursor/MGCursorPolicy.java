package tytoo.minegui.view.cursor;

import tytoo.minegui.view.MGView;

public interface MGCursorPolicy {
    void onOpen(MGView view);

    void onClose(MGView view);
}
