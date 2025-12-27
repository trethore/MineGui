package tytoo.mineguidebug.view.sections;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import tytoo.minegui.imgui.ref.FloatRef;
import tytoo.minegui.view.View;
import tytoo.mineguidebug.view.DebugLayout;

public final class LayoutShowcaseSection implements PlaygroundSection {
    private static final String[] NAV_ITEMS = new String[]{
            "Dashboard", "Inventory", "Particles", "Audio", "Scripting"
    };
    private final ImBoolean showGuides = new ImBoolean(true);
    private final ImBoolean showTable = new ImBoolean(true);
    private final FloatRef leftPaneWidth = new FloatRef(180f);
    private final FloatRef gutterSize = new FloatRef(10f);
    private int selectedNavIndex;

    @Override
    public String tabLabel() {
        return "Layouts";
    }

    @Override
    public void render(View parent) {
        renderIntro();
        DebugLayout.sectionGap();
        renderControls();
        DebugLayout.sectionGap();
        renderTwoPaneLayout();
        DebugLayout.sectionGap();
        renderMicroLayouts();
    }

    private void renderIntro() {
        ImGui.text("Compose panes with ImGui primitives; MineGui stays out of the way.");
        ImGui.textWrapped("Everything below uses beginChild(), sameLine(), and tables to demonstrate how immediate mode stays flexible without any removed layout engine.");
    }

    private void renderControls() {
        leftPaneWidth.sliderFloat("Left pane width", 140f, 260f, "%.0f px");
        ImGui.sameLine();
        gutterSize.sliderFloat("Gutter", 6f, 18f, "%.0f px");
        ImGui.checkbox("Show guides", showGuides);
        ImGui.sameLine();
        ImGui.checkbox("Show layout table", showTable);
    }

    private void renderTwoPaneLayout() {
        ImVec2 startPos = ImGui.getCursorScreenPos();
        float contentHeight = 260f;
        ImGui.beginGroup();
        if (ImGui.beginChild("layout_nav", leftPaneWidth.get(), contentHeight, true)) {
            ImGui.textColored(0.72f, 0.84f, 1f, 1f, "Navigation");
            ImGui.separator();
            for (int i = 0; i < NAV_ITEMS.length; i++) {
                boolean isSelected = selectedNavIndex == i;
                if (ImGui.selectable(NAV_ITEMS[i], isSelected)) {
                    selectedNavIndex = i;
                }
            }
        }
        ImGui.endChild();
        ImGui.sameLine(0f, gutterSize.get());
        if (ImGui.beginChild("layout_body", 0f, contentHeight, true)) {
            ImGui.textColored(0.74f, 0.92f, 0.86f, 1f, "Detail Panel");
            ImGui.separator();
            ImGui.textWrapped("""
                    Child windows let you pin scrolling regions, status bars, and forms. Combine them with sameLine() to create split views that dock nicely inside MineGui windows.
                    """);
            DebugLayout.smallGap();
            ImGui.text("Selected: %s".formatted(NAV_ITEMS[selectedNavIndex]));
            ImGui.bulletText("Scroll independent of the navigation rail.");
            ImGui.bulletText("Tab bar above comes from MineGui Window helper.");
            ImGui.bulletText("Every widget keeps state in this section instance.");
        }
        ImGui.endChild();
        ImGui.endGroup();

        if (showGuides.get()) {
            ImVec2 endPos = ImGui.getCursorScreenPos();
            float left = startPos.x;
            float right = endPos.x;
            float top = startPos.y;
            float bottom = startPos.y + contentHeight;
            float splitX = startPos.x + leftPaneWidth.get() + gutterSize.get() * 0.5f;
            int guideColor = ImGui.getColorU32(0.32f, 0.62f, 1f, 0.35f);
            ImGui.getWindowDrawList().addRect(left - 4f, top - 6f, right + 4f, bottom + 6f, guideColor, 6f, 0, 1.5f);
            ImGui.getWindowDrawList().addLine(splitX, top - 4f, splitX, bottom + 4f, guideColor, 1.5f);
        }
    }

    private void renderMicroLayouts() {
        ImGui.text("Inline layouts");
        ImGui.textWrapped("Use groups and sameLine() spacing to build denser UI without helpers.");
        DebugLayout.smallGap();
        ImGui.beginGroup();
        renderCard("Toolbar row", "Buttons stitched with sameLine()", () -> {
            ImGui.button("Play");
            ImGui.sameLine();
            ImGui.button("Pause");
            ImGui.sameLine();
            ImGui.button("Step");
        });
        ImGui.sameLine(0f, 8f);
        renderCard("Chips", "Selectable tags in a child", () -> {
            ImGui.beginChild("chip_panel", 120f, 64f, true);
            ImGui.textColored(0.8f, 0.9f, 1f, 1f, "Filters");
            ImGui.separator();
            ImGui.selectable("UI");
            ImGui.sameLine();
            ImGui.selectable("Rendering");
            ImGui.sameLine();
            ImGui.selectable("Input");
            ImGui.endChild();
        });
        ImGui.sameLine(0f, 8f);
        renderCard("Sticky footer", "Add spacer + AlignTextToFramePadding", () -> {
            ImGui.beginChild("footer_panel", 140f, 72f, true);
            ImGui.text("Body grows");
            ImGui.text("...");
            ImGui.dummy(0f, 12f);
            ImGui.separator();
            ImGui.alignTextToFramePadding();
            ImGui.text("Inline status");
            ImGui.sameLine();
            ImGui.textColored(0.72f, 0.84f, 1f, 1f, "OK");
            ImGui.endChild();
        });
        ImGui.endGroup();
        DebugLayout.sectionGap();
        if (showTable.get()) {
            renderLayoutTable();
        }
    }

    private void renderCard(String title, String subtitle, Runnable content) {
        ImGui.beginChild(title, 150f, 110f, true);
        ImGui.text(title);
        ImGui.textDisabled(subtitle);
        DebugLayout.smallGap();
        content.run();
        ImGui.endChild();
    }

    private void renderLayoutTable() {
        int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp;
        if (ImGui.beginTable("layout_tips", 3, flags)) {
            ImGui.tableSetupColumn("Primitive", ImGuiTableColumnFlags.WidthFixed, DebugLayout.TABLE_LABEL_WIDTH_SMALL);
            ImGui.tableSetupColumn("Use case", ImGuiTableColumnFlags.WidthStretch);
            ImGui.tableSetupColumn("Tip", ImGuiTableColumnFlags.WidthStretch);
            renderLayoutRow("beginChild()", "Scrollable regions", "Pair with style rounding for cards.");
            renderLayoutRow("sameLine()", "Inline controls", "Supply spacing argument for gutters.");
            renderLayoutRow("group()", "Lock item width", "Great for stacked buttons and labels.");
            renderLayoutRow("tables", "Measured grids", "RowBg + Borders reads well in overlays.");
            ImGui.endTable();
        }
    }

    private void renderLayoutRow(String primitive, String useCase, String tip) {
        ImGui.tableNextRow();
        ImGui.tableSetColumnIndex(0);
        ImGui.text(primitive);
        ImGui.tableSetColumnIndex(1);
        ImGui.textWrapped(useCase);
        ImGui.tableSetColumnIndex(2);
        ImGui.textWrapped(tip);
    }
}
