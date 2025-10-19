package tytoo.minegui_debug.windows;

import imgui.flag.ImGuiColorEditFlags;
import tytoo.minegui.component.components.display.MGText;
import tytoo.minegui.component.components.interactive.*;
import tytoo.minegui.component.components.layout.MGWindow;
import tytoo.minegui.component.id.IDScope;
import tytoo.minegui.contraint.constraints.Constraints;
import tytoo.minegui.state.State;
import tytoo.minegui_debug.MineGuiDebugCore;

import java.util.List;
import java.util.Locale;

public class TestWindow extends MGWindow {

    private static final int COLUMN_WIDTH = 140;
    private static final int COLUMN_SPACING = 30;
    private static final int ROW_HEIGHT = 34;
    private static final int TITLE_OFFSET = 20;
    private static final List<String> DIFFICULTIES = List.of("Explorer", "Adventurer", "Veteran", "Nightmare");
    private static final List<String> FACTIONS = List.of("Northwind Pact", "Sunforge Guild", "Duskveil Order", "Ironward Clan");

    private final State<Integer> difficultyIndex = State.of(1);
    private final State<String> factionSelection = State.of(FACTIONS.getFirst());
    private final State<float[]> accentColor = State.of(new float[]{0.18f, 0.55f, 0.91f, 1.0f});
    private final State<Integer> accentColorPacked = State.of(0xFF2E8BE8);
    private final State<String> accentHex = State.of(formatHex(0xFF2E8BE8));

    public TestWindow() {
        super("test window");
    }

    private static String formatHex(int rgba) {
        return String.format(Locale.ROOT, "#%08X", rgba);
    }

    @Override
    protected void onCreate() {
        this.initialBounds(180, 160, 640, 520);
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

        float colorRowY = factionRowY + 70f;
        MGText.of("Accent Color")
                .pos(20, colorRowY)
                .render();

        MGText.of(accentHex)
                .pos(Constraints.pixels(200f), Constraints.pixels(colorRowY))
                .id(DemoIds.ACCENT_HEX)
                .render();

        MGColorPicker.ofRgba()
                .pos(Constraints.pixels(20f), Constraints.pixels(colorRowY + 24f))
                .state(accentColor)
                .packedState(accentColorPacked)
                .id(DemoIds.ACCENT_PICKER)
                .onPackedChange(color -> accentHex.set(formatHex(color)))
                .onPackedCommit(color -> MineGuiDebugCore.LOGGER.info("Accent color committed {}", formatHex(color)))
                .render();

        MGColorEdit.ofRgba()
                .pos(Constraints.pixels(360f), Constraints.pixels(colorRowY + 24f))
                .width(210f)
                .state(accentColor)
                .packedState(accentColorPacked)
                .display(MGColorEdit.DisplayMode.RGB)
                .input(MGColorEdit.InputMode.RGB)
                .picker(MGColorEdit.PickerMode.HUE_BAR)
                .alphaBar(true)
                .alphaPreview(MGColorEdit.AlphaPreviewMode.FULL)
                .id(DemoIds.ACCENT_EDITOR)
                .onPackedCommit(color -> MineGuiDebugCore.LOGGER.info("Accent color edited {}", formatHex(color)))
                .render();

        MGText.of("Accent Swatch")
                .pos(Constraints.pixels(600f), Constraints.pixels(colorRowY))
                .render();

        MGColorButton.ofRgba()
                .pos(Constraints.pixels(600f), Constraints.pixels(colorRowY + 24f))
                .width(54f)
                .scale(1.25f)
                .state(accentColor)
                .packedState(accentColorPacked)
                .alphaPreview(MGColorButton.AlphaPreviewMode.FULL)
                .showTooltip(true)
                .withPicker(false)
                .pickerFlags(ImGuiColorEditFlags.DisplayRGB | ImGuiColorEditFlags.AlphaBar)
                .id(DemoIds.ACCENT_BUTTON)
                .onPackedClick(color -> MineGuiDebugCore.LOGGER.info("Accent swatch pressed {}", formatHex(color)))
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
        FACTION_COMBO,
        ACCENT_PICKER,
        ACCENT_EDITOR,
        ACCENT_BUTTON,
        ACCENT_HEX
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
