package tytoo.mineguidebug.view.sections;

import tytoo.minegui.view.ViewSection;

public interface PlaygroundSection extends ViewSection {
    String tabLabel();

    default String tabId() {
        return getClass().getName();
    }
}
