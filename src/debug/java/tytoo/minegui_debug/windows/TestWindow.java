package tytoo.minegui_debug.windows;

import imgui.ImGui;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.component.id.IDScope;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui_debug.MineGuiDebugCore;

public class TestWindow extends MGWindow {

    private static final int COLUMN_WIDTH = 140;
    private static final int COLUMN_SPACING = 30;
    private static final int ROW_HEIGHT = 34;
    private static final int TITLE_OFFSET = 20;

    public TestWindow() {
        super("test window");
    }

    @Override
    protected void onCreate() {
        this.initialBounds(180, 160, 520, 260);
    }

    @Override
    protected void renderContents() {
        int startX = 20;
        int startY = 55;

        int column = 0;
        for (ButtonSection section : ButtonSection.values()) {
            int columnX = startX + column * (COLUMN_WIDTH + COLUMN_SPACING);
            ImGui.setCursorPos(columnX, startY - TITLE_OFFSET);
            ImGui.text(section.title());

            try (IDScope.Scope ignored = IDScope.push(DemoIds.SECTION, section.name())) {
                renderSectionButtons(section, columnX, startY);
            }

            column++;
        }
    }

    private void renderSectionButtons(ButtonSection section, int columnX, int startY) {
        int row = 0;
        for (String label : section.buttons()) {
            int buttonY = startY + row * ROW_HEIGHT;
            MGButton.of(label)
                    .pos(columnX, buttonY)
                    .width(Constraints.pixels(COLUMN_WIDTH))
                    .height(Constraints.pixels(28))
                    .id(DemoIds.BUTTON, label)
                    .onClick(() -> MineGuiDebugCore.LOGGER.info("Clicked {} -> {}", section.name(), label))
                    .render();
            row++;
        }
    }

    private enum DemoIds {
        SECTION,
        BUTTON
    }

    private enum ButtonSection {
        FAVORITES("Favorites", new String[]{"Alpha", "Bravo", "Charlie"}),
        QUESTS("Quests", new String[]{"Daily Tasks", "Side Missions", "Weekly Challenge"}),
        ACTIONS("Actions", new String[]{"Equip", "Upgrade", "Remove"});

        private final String title;
        private final String[] buttons;

        ButtonSection(String title, String[] buttons) {
            this.title = title;
            this.buttons = buttons;
        }

        public String title() {
            return title;
        }

        public String[] buttons() {
            return buttons;
        }
    }
}
