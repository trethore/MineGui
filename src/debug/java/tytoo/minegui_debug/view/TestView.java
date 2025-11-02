package tytoo.minegui_debug.view;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.util.Identifier;
import tytoo.minegui.helper.UI;
import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.window.Window;
import tytoo.minegui.style.FontLibrary;
import tytoo.minegui.style.Fonts;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;

import java.util.Locale;

public final class TestView extends View {
    private static final Identifier IMGUI_ICON = Identifier.of("minegui", "icon.png");
    private static final float CONTROL_WIDTH = 220f;
    private static final float CHILD_HEIGHT = 220f;
    private static final String[][] TABLE_ROWS = {
            {"Stacks", "Chain VStack and HStack helpers to normalize spacing.", "Ready"},
            {"Split panels", "ImGui child regions mimic docked editors at runtime.", "Ready"},
            {"Tables", "Immediate tables combine scrolling, borders, and selection.", "Ready"},
            {"Widgets", "Font previews, progress bars, and textures stay in sync.", "Ready"},
            {"Forms", "Shared buffers keep input state stable between ticks.", "Ready"},
            {"Constraints", "FillMode.MATCH_WIDEST evens out composited toolbars.", "Info"}
    };

    private final ImBoolean showStackBorders = new ImBoolean(true);
    private final ImBoolean showIconPreview = new ImBoolean(true);
    private final ImString stackNotes = new ImString("Tune spacing to compare stack helpers.", 256);
    private final ImString tableFilter = new ImString(64);
    private final ImString scratchPad = new ImString("Type notes while you explore layouts.", 512);
    private float stackSpacing = 12f;
    private float leftPanelRatio = 0.4f;
    private float progressValue = 0.45f;
    private boolean clearFocusOnOpen;
    private int selectedTableRow = -1;
    private String lastAction = "Awaiting interaction";
    private ImFont jetbrainsMono;
    private String jetbrainsStatus = "JetBrains Mono pending";

    public TestView() {
        super("minegui_debug:test_view", true);
        setCursorPolicy(CursorPolicies.clickToLock());
    }

    @Override
    protected void onOpen() {
        clearFocusOnOpen = true;
    }

    @Override
    protected void renderView() {
        Window.of(this, "Layout Playground")
                .flags(ImGuiWindowFlags.NoFocusOnAppearing)
                .render(() -> {
                    resetFocusIfPending();
                    UI.withVStack(new VStack.Options().spacing(14f).fillMode(VStack.FillMode.MATCH_WIDEST), layout -> {
                        renderHeader(layout);
                        renderTabs(layout);
                        renderFooter(layout);
                    });
                });
    }

    private void renderHeader(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ImGui.text("Layout playground");
            ImGui.separator();
            ImGui.textWrapped("Exercise MineGui helpers alongside raw ImGui primitives. Explore stacks, split panes, tables, and widget previews to confirm integrations behave as expected.");
        });
    }

    private void renderTabs(VStack layout) {
        UI.withVStackItem(layout, () -> {
            if (ImGui.beginTabBar("test_view_tabbar")) {
                if (ImGui.beginTabItem("Stacks")) {
                    renderStacksTab();
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Split panels")) {
                    renderSplitTab();
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Tables")) {
                    renderTableTab();
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Widgets")) {
                    renderWidgetsTab();
                    ImGui.endTabItem();
                }
                ImGui.endTabBar();
            }
        });
    }

    private void renderStacksTab() {
        SizeHints.itemWidth(CONTROL_WIDTH);
        float[] spacingHolder = {stackSpacing};
        if (ImGui.sliderFloat("Stack spacing", spacingHolder, 4.0f, 32.0f, "%.1f px")) {
            stackSpacing = spacingHolder[0];
            logAction("Stack spacing set to %.1f px".formatted(stackSpacing));
        }
        UI.withVStack(new VStack.Options().spacing(stackSpacing).fillMode(VStack.FillMode.MATCH_WIDEST), column -> {
            UI.withVStackItem(column, () -> ImGui.textWrapped("VStack spacing cascades into nested helpers. Adjust the slider to see shared rhythm across headers, toolbars, and content."));
            UI.withVStackItem(column, () -> {
                float rowHeight = ImGui.getFrameHeight();
                UI.withHStack(new HStack.Options().spacing(stackSpacing).alignment(HStack.Alignment.CENTER), row -> {
                    UI.withHItem(row, new HStack.ItemRequest().estimateWidth(140f).estimateHeight(rowHeight), () -> {
                        if (ImGui.button("Primary", 140f, 0f)) {
                            logAction("Primary action triggered");
                        }
                    });
                    UI.withHItem(row, new HStack.ItemRequest().estimateWidth(140f).estimateHeight(rowHeight), () -> {
                        if (ImGui.button("Secondary", 140f, 0f)) {
                            logAction("Secondary action triggered");
                        }
                    });
                    UI.withHItem(row, new HStack.ItemRequest().estimateWidth(160f).estimateHeight(rowHeight), () -> {
                        if (ImGui.checkbox("Child borders", showStackBorders)) {
                            logAction("Stack preview borders " + (showStackBorders.get() ? "enabled" : "disabled"));
                        }
                    });
                });
            });
            UI.withVStackItem(column, new VStack.ItemRequest().estimateHeight(CHILD_HEIGHT), () -> {
                ImGui.beginChild("stack_preview_region", 0f, CHILD_HEIGHT - 12f, showStackBorders.get());
                ImGui.text("Layout preview");
                ImGui.separator();
                ImGui.bulletText("Spacing %.1f px".formatted(stackSpacing));
                ImGui.bulletText(showStackBorders.get() ? "Borders enabled" : "Borders disabled");
                ImGui.spacing();
                ImGui.textWrapped("Combine VStack for vertical flow with HStack for aligned toolbars without giving up raw ImGui calls.");
                ImGui.endChild();
            });
            UI.withVStackItem(column, () -> {
                ImGui.text("Stack notes");
                ImGui.inputTextMultiline("##stack_notes", stackNotes, -1f, 80f);
            });
        });
    }

    private void renderSplitTab() {
        SizeHints.itemWidth(CONTROL_WIDTH);
        float[] ratioHolder = {leftPanelRatio};
        if (ImGui.sliderFloat("Left pane ratio", ratioHolder, 0.25f, 0.7f, "%.2f")) {
            leftPanelRatio = ratioHolder[0];
            logAction("Split ratio set to %.2f".formatted(leftPanelRatio));
        }
        float totalWidth = ImGui.getContentRegionAvailX();
        if (totalWidth <= 0f) {
            totalWidth = 360f;
        }
        float spacing = ImGui.getStyle().getItemSpacingX();
        float leftWidth = Math.max(150f, totalWidth * leftPanelRatio);
        float rightWidth = totalWidth - leftWidth - spacing;
        if (rightWidth < 150f) {
            rightWidth = 150f;
            leftWidth = Math.max(150f, totalWidth - rightWidth - spacing);
        }
        ImGui.beginChild("split_left", leftWidth, CHILD_HEIGHT, true);
        ImGui.text("Navigation");
        ImGui.separator();
        for (String[] row : TABLE_ROWS) {
            ImGui.bulletText(row[0]);
        }
        ImGui.endChild();
        ImGui.sameLine();
        ImGui.beginChild("split_right", 0f, CHILD_HEIGHT, true);
        ImGui.text("Details");
        ImGui.separator();
        int detailIndex = Math.max(selectedTableRow, 0);
        String[] detailRow = TABLE_ROWS[detailIndex];
        ImGui.textWrapped(detailRow[1]);
        ImGui.spacing();
        ImGui.text("Status: " + detailRow[2]);
        ImGui.spacing();
        if (ImGui.button("Trigger action", 160f, 0f)) {
            logAction("Detail action triggered for " + detailRow[0]);
        }
        ImGui.endChild();
    }

    private void renderTableTab() {
        SizeHints.itemWidth(CONTROL_WIDTH);
        ImGui.inputText("Filter", tableFilter);
        String rawFilter = tableFilter.get().trim().toLowerCase(Locale.ROOT);
        boolean hasFilter = !rawFilter.isEmpty();
        ImGui.spacing();
        int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.ScrollY | ImGuiTableFlags.Resizable;
        if (ImGui.beginTable("layout_table", 3, flags, 0f, CHILD_HEIGHT)) {
            ImGui.tableSetupColumn("Layout", ImGuiTableColumnFlags.WidthFixed, 150f);
            ImGui.tableSetupColumn("Details", ImGuiTableColumnFlags.WidthStretch);
            ImGui.tableSetupColumn("Status", ImGuiTableColumnFlags.WidthFixed, 80f);
            ImGui.tableHeadersRow();
            for (int index = 0; index < TABLE_ROWS.length; index++) {
                String[] row = TABLE_ROWS[index];
                if (hasFilter && !matchesFilter(row, rawFilter)) {
                    continue;
                }
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                boolean selected = selectedTableRow == index;
                if (ImGui.selectable(row[0], selected, ImGuiSelectableFlags.SpanAllColumns)) {
                    selectedTableRow = index;
                    logAction("Selected row " + row[0]);
                }
                ImGui.tableNextColumn();
                ImGui.textWrapped(row[1]);
                ImGui.tableNextColumn();
                ImGui.text(row[2]);
            }
            ImGui.endTable();
        }
    }

    private void renderWidgetsTab() {
        ensureJetbrainsMono();
        ImGui.text(jetbrainsStatus);
        if (jetbrainsMono != null) {
            ImGui.pushFont(jetbrainsMono);
            ImGui.text("JetBrains Mono sample -- 0123456789 ABC xyz");
            ImGui.popFont();
        }
        ImGui.separator();
        SizeHints.itemWidth(CONTROL_WIDTH);
        float[] progressHolder = {progressValue};
        if (ImGui.sliderFloat("Progress value", progressHolder, 0.0f, 1.0f, "%.2f")) {
            progressValue = progressHolder[0];
            logAction("Progress value set to %.2f".formatted(progressValue));
        }
        float width = ImGui.getContentRegionAvailX();
        if (width <= 0f) {
            width = 180f;
        }
        ImGui.progressBar(progressValue, width, 0f, "%.0f%%".formatted(progressValue * 100f));
        ImGui.separator();
        if (ImGui.checkbox("Show icon preview", showIconPreview)) {
            logAction("Icon preview " + (showIconPreview.get() ? "enabled" : "disabled"));
        }
        if (showIconPreview.get()) {
            float size = 96f;
            float cursorX = ImGui.getCursorScreenPosX();
            float cursorY = ImGui.getCursorScreenPosY();
            ImGuiImageUtils.drawImage(IMGUI_ICON, cursorX, cursorY, cursorX + size, cursorY + size, 0, false, 0xFFFFFFFF);
            ImGui.dummy(size, size);
        }
    }

    private void renderFooter(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ImGui.separator();
            ImGui.text("Last action: " + lastAction);
            ImGui.spacing();
            ImGui.text("Tester notes");
            ImGui.inputTextMultiline("##test_notes", scratchPad, -1f, 96f);
        });
    }

    private boolean matchesFilter(String[] row, String filter) {
        for (String cell : row) {
            if (cell.toLowerCase(Locale.ROOT).contains(filter)) {
                return true;
            }
        }
        return false;
    }

    private void ensureJetbrainsMono() {
        if (jetbrainsMono != null) {
            jetbrainsStatus = "JetBrains Mono ready";
            return;
        }
        ImFont resolved = Fonts.ensureJetbrainsMono();
        if (resolved != null) {
            jetbrainsMono = resolved;
            jetbrainsStatus = "JetBrains Mono ready";
        } else if (FontLibrary.getInstance().isRegistrationLocked()) {
            jetbrainsStatus = "JetBrains Mono unavailable; restart after registering fonts.";
        } else {
            jetbrainsStatus = "JetBrains Mono loading...";
        }
    }

    private void resetFocusIfPending() {
        if (!clearFocusOnOpen) {
            return;
        }
        ImGui.setWindowFocus(null);
        clearFocusOnOpen = false;
    }

    private void logAction(String message) {
        lastAction = message;
    }
}
