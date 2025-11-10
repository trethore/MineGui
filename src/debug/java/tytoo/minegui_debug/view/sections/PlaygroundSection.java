package tytoo.minegui_debug.view.sections;

import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.view.View;

public interface PlaygroundSection {
    String tabLabel();

    default String tabId() {
        return getClass().getName();
    }

    void render(View parent, LayoutApi layoutApi);
}
