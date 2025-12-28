package tytoo.mineguidebug.view.sections;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import tytoo.minegui.helper.LayoutHelper;
import tytoo.minegui.helper.Panel;
import tytoo.minegui.helper.TableHelper;
import tytoo.minegui.imgui.ref.FloatRef;
import tytoo.minegui.view.View;

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
        LayoutHelper.sectionGap();
        renderControls();
        LayoutHelper.sectionGap();
        renderTwoPaneLayout();
        LayoutHelper.sectionGap();
        renderMicroLayouts();
    }

    private void renderIntro() {
        ImGui.text("Compose panes with ImGui primitives; MineGui stays out of the way.");
        ImGui.textWrapped("Everything below uses beginChild(), sameLine(), and tables to demonstrate how immediate mode stays flexible without any removed layout engine.");
    }

    private void renderControls() {
        leftPaneWidth.sliderFloat("Left pane width", 140f, 260f, "%.0f px");
        gutterSize.sliderFloat("Gutter", 6f, 18f, "%.0f px");
        ImGui.checkbox("Show guides", showGuides);
        ImGui.sameLine();
        ImGui.checkbox("Show layout table", showTable);
    }

    private void renderTwoPaneLayout() {
        ImVec2 startPos = ImGui.getCursorScreenPos();
        float contentHeight = 260f;
        ImGui.beginGroup();
        Panel.of("layout_nav").size(leftPaneWidth.get(), contentHeight).border(true).render(() -> {
            ImGui.textColored(0.72f, 0.84f, 1f, 1f, "Navigation");
            ImGui.separator();
            for (int i = 0; i < NAV_ITEMS.length; i++) {
                boolean isSelected = selectedNavIndex == i;
                if (ImGui.selectable(NAV_ITEMS[i], isSelected)) {
                    selectedNavIndex = i;
                }
            }
        });
        ImGui.sameLine(0f, gutterSize.get());
        Panel.of("layout_body").height(contentHeight).border(true).render(() -> {
            ImGui.textColored(0.74f, 0.92f, 0.86f, 1f, "Detail Panel");
            ImGui.separator();
            ImGui.textWrapped("""
                    Child windows let you pin scrolling regions, status bars, and forms. Combine them with sameLine() to create split views that dock nicely inside MineGui windows.
                    """);
            LayoutHelper.smallGap();
            ImGui.text("Selected: %s".formatted(NAV_ITEMS[selectedNavIndex]));
            ImGui.bulletText("Scroll independent of the navigation rail.");
            ImGui.bulletText("Tab bar above comes from MineGui Window helper.");
            ImGui.bulletText("Every widget keeps state in this section instance.");
        });
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
        LayoutHelper.smallGap();
        ImGui.beginGroup();
        renderCard("Toolbar row", "Buttons stitched with sameLine()", () -> {
            ImGui.button("Play");
            ImGui.sameLine();
            ImGui.button("Pause");
            ImGui.sameLine();
            ImGui.button("Step");
        });
        ImGui.sameLine(0f, 8f);
        renderCard("Chips", "Selectable tags in a child",
                () -> Panel.of("chip_panel").size(180f, 80f).border(true).render(() -> {
                    ImGui.textColored(0.8f, 0.9f, 1f, 1f, "Filters");
                    ImGui.separator();
                    ImGui.selectable("UI");
                    ImGui.sameLine();
                    ImGui.selectable("Rendering");
                    ImGui.sameLine();
                    ImGui.selectable("Input");
                }));
        ImGui.sameLine(0f, 8f);
        renderCard("Sticky footer", "Add spacer + AlignTextToFramePadding",
                () -> Panel.of("footer_panel").size(180f, 80f).border(true).render(() -> {
                    ImGui.text("Body grows");
                    ImGui.text("...");
                    ImGui.dummy(0f, 12f);
                    ImGui.separator();
                    ImGui.alignTextToFramePadding();
                    ImGui.text("Inline status");
                    ImGui.sameLine();
                    ImGui.textColored(0.72f, 0.84f, 1f, 1f, "OK");
                }));
        ImGui.endGroup();
        LayoutHelper.sectionGap();
        if (showTable.get()) {
            renderLayoutTable();
        }
    }

    private void renderCard(String title, String subtitle, Runnable content) {
        Panel.of(title).size(200f, 140f).border(true).render(() -> {
            ImGui.text(title);
            ImGui.textDisabled(subtitle);
            LayoutHelper.smallGap();
            content.run();
        });
    }

    private void renderLayoutTable() {
        int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp;
        if (ImGui.beginTable("layout_tips", 3, flags)) {
            ImGui.tableSetupColumn("Primitive", ImGuiTableColumnFlags.WidthFixed, LayoutHelper.TABLE_LABEL_WIDTH_SMALL);
            ImGui.tableSetupColumn("Use case", ImGuiTableColumnFlags.WidthStretch);
            ImGui.tableSetupColumn("Tip", ImGuiTableColumnFlags.WidthStretch);
            TableHelper.rowWrapped("beginChild()", "Scrollable regions", "Pair with style rounding for cards.");
            TableHelper.rowWrapped("sameLine()", "Inline controls", "Supply spacing argument for gutters.");
            TableHelper.rowWrapped("group()", "Lock item width", "Great for stacked buttons and labels.");
            TableHelper.rowWrapped("tables", "Measured grids", "RowBg + Borders reads well in overlays.");
            ImGui.endTable();
        }
    }
}
