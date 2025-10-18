package tytoo.minegui_debug.windows;

import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.MGButton;
import tytoo.minegui.component.components.interactive.MGCombo;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.component.id.IDScope;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui_debug.MineGuiDebugCore;

import java.util.List;

public class TestWindow extends MGWindow {

    private static final int COLUMN_WIDTH = 140;
    private static final int COLUMN_SPACING = 30;
    private static final int ROW_HEIGHT = 34;
    private static final int TITLE_OFFSET = 20;
    private static final List<String> DIFFICULTIES = List.of("Explorer", "Adventurer", "Veteran", "Nightmare");
    private static final List<String> FACTIONS = List.of("Northwind Pact", "Sunforge Guild", "Duskveil Order", "Ironward Clan");

    private final State<Integer> difficultyIndex = State.of(1);
    private final State<String> factionSelection = State.of(FACTIONS.getFirst());

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
            try (IDScope.Scope ignored = IDScope.push(DemoIds.SECTION, section.name())) {
                MGText.of(section.title())
                        .pos(columnX, startY - TITLE_OFFSET)
                        .id(DemoIds.SECTION_TITLE, section.name())
                        .render();
                renderSectionButtons(section, columnX, startY);
            }

            column++;
        }

        float demoY = startY + ButtonSection.values().length * ROW_HEIGHT + 30f;
        MGButton.of("Bounded Width")
                .pos(Constraints.relative(0.5f, -90f), Constraints.pixels(demoY))
                .dimensions(Constraints.relative(0.45f), Constraints.pixels(36))
                .minWidth(180f)
                .maxWidth(240f)
                .minHeight(32f)
                .maxHeight(40f)
                .id(DemoIds.BOUNDED_BUTTON)
                .onClick(() -> MineGuiDebugCore.LOGGER.info("Triggered bounded width demo"))
                .render();

        float comboRowY = demoY + 60f;
        MGText.of("Difficulty Preset")
                .pos(20, comboRowY)
                .render();

        MGCombo.of(DIFFICULTIES)
                .pos(Constraints.pixels(160f), Constraints.pixels(comboRowY - 4f))
                .width(210f)
                .indexState(difficultyIndex)
                .id(DemoIds.DIFFICULTY_COMBO)
                .onChange(selection -> MineGuiDebugCore.LOGGER.info("Difficulty changed to {}", selection))
                .render();

        float factionRowY = comboRowY + 40f;
        MGText.of("Faction Alignment")
                .pos(20, factionRowY)
                .render();

        MGCombo.<String>of()
                .items(() -> FACTIONS)
                .pos(Constraints.pixels(160f), Constraints.pixels(factionRowY - 4f))
                .width(210f)
                .state(factionSelection)
                .formatter(value -> value != null ? value.toUpperCase() : "")
                .emptyPreview(() -> "Select faction")
                .id(DemoIds.FACTION_COMBO)
                .onCommit(selection -> MineGuiDebugCore.LOGGER.info("Faction committed to {}", selection))
                .render();
    }

    private void renderSectionButtons(ButtonSection section, int columnX, int startY) {
        int row = 0;
        for (String label : section.buttons()) {
            int buttonY = startY + row * ROW_HEIGHT;
            MGButton.of(label)
                    .x(columnX)
                    .y(buttonY)
                    .dimensions(COLUMN_WIDTH, 28)
                    .id(DemoIds.BUTTON, label)
                    .onClick(() -> MineGuiDebugCore.LOGGER.info("Clicked {} -> {}", section.name(), label))
                    .render();
            row++;
        }
    }

    private enum DemoIds {
        SECTION,
        SECTION_TITLE,
        BUTTON,
        BOUNDED_BUTTON,
        DIFFICULTY_COMBO,
        FACTION_COMBO
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
