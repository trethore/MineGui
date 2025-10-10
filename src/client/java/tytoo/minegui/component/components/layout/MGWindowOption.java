package tytoo.minegui.component.components.layout;

import imgui.flag.ImGuiWindowFlags;

@SuppressWarnings("unused")
public enum MGWindowOption {
    NO_TITLE_BAR(ImGuiWindowFlags.NoTitleBar),
    NO_SCROLLBAR(ImGuiWindowFlags.NoScrollbar),
    MENU_BAR(ImGuiWindowFlags.MenuBar),
    NO_MOVE(ImGuiWindowFlags.NoMove),
    NO_RESIZE(ImGuiWindowFlags.NoResize),
    NO_COLLAPSE(ImGuiWindowFlags.NoCollapse),
    NO_NAV(ImGuiWindowFlags.NoNav),
    NO_BACKGROUND(ImGuiWindowFlags.NoBackground),
    NO_BRING_TO_FRONT_ON_FOCUS(ImGuiWindowFlags.NoBringToFrontOnFocus),
    NO_DOCKING(ImGuiWindowFlags.NoDocking),
    UNSAVED_DOCUMENT(ImGuiWindowFlags.UnsavedDocument),
    NO_CLOSE(null, true, false, false),
    ALLOW_CHILD_MOVE(null, false, true, false),
    ALLOW_CHILD_RESIZE(null, false, false, true);

    private final Integer imGuiFlag;
    private final boolean disablesCloseButton;
    private final boolean allowChildMove;
    private final boolean allowChildResize;

    MGWindowOption(Integer imGuiFlag) {
        this(imGuiFlag, false, false, false);
    }

    MGWindowOption(Integer imGuiFlag, boolean disablesCloseButton, boolean allowChildMove, boolean allowChildResize) {
        this.imGuiFlag = imGuiFlag;
        this.disablesCloseButton = disablesCloseButton;
        this.allowChildMove = allowChildMove;
        this.allowChildResize = allowChildResize;
    }

    public Integer getImGuiFlag() {
        return imGuiFlag;
    }

    public boolean disablesCloseButton() {
        return disablesCloseButton;
    }

    public boolean allowsChildMove() {
        return allowChildMove;
    }

    public boolean allowsChildResize() {
        return allowChildResize;
    }
}
