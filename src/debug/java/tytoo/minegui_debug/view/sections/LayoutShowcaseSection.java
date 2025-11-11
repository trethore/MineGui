package tytoo.minegui_debug.view.sections;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import tytoo.minegui.helper.constraint.constraints.Constraints;
import tytoo.minegui.helper.layout.LayoutConstraints;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.experimental.GridLayout;
import tytoo.minegui.helper.layout.sizing.ScaleUnit;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.layout.LayoutTemplate;
import tytoo.minegui.view.View;

public final class LayoutShowcaseSection implements PlaygroundSection {
    private static final float MIN_PANEL_WIDTH = 140f;
    private final ImBoolean anchorStack = new ImBoolean(false);
    private final ImBoolean showGridSampler = new ImBoolean(true);
    private float stackSpacing = 12f;
    private float splitRatio = 0.42f;
    private String layoutStatus = "Ready";

    @Override
    public String tabLabel() {
        return "Layouts";
    }

    @Override
    public void render(View parent, LayoutApi layoutApi) {
        LayoutTemplate toolbarTemplate = buildToolbarTemplate(layoutApi);
        LayoutTemplate template = layoutApi.vertical()
                .spacing(8f)
                .child(slot -> slot.content(this::renderDescription))
                .child(slot -> slot.content(this::renderControls))
                .child(slot -> slot.template(toolbarTemplate))
                .child(slot -> slot.height(260f).fillWidth(true).content(this::renderSplit))
                .child(slot -> slot.content(this::renderStatusLine))
                .child(slot -> slot.content(this::renderGridSection))
                .build();
        layoutApi.render(template);
        if (anchorStack.get()) {
            renderAnchoredCallout();
        }
    }

    private void renderDescription() {
        ImGui.text("Layout DSL + helpers");
        ImGui.textWrapped("LayoutApi templates orchestrate high-level regions while VStack/HStack cover bespoke flows. Mix both to keep MineGui overlays declarative without giving up immediate mode control.");
    }

    private void renderControls() {
        SizeHints.itemWidth(220f);
        float[] spacingHolder = {stackSpacing};
        if (ImGui.sliderFloat("Stack spacing", spacingHolder, 4f, 28f, "%.1f px")) {
            stackSpacing = spacingHolder[0];
        }
        float[] ratioHolder = {splitRatio};
        if (ImGui.sliderFloat("Left panel ratio", ratioHolder, 0.25f, 0.7f, "%.2f")) {
            splitRatio = ratioHolder[0];
            layoutStatus = "Split ratio set to %.2f".formatted(splitRatio);
        }
        ImGui.checkbox("Pin stack to window origin", anchorStack);
        ImGui.sameLine();
        ImGui.checkbox("Show grid sampler", showGridSampler);
    }

    private LayoutTemplate buildToolbarTemplate(LayoutApi api) {
        return api.row()
                .spacing(10f)
                .child(slot -> slot.content(() -> {
                    if (ImGui.button("Reset split")) {
                        splitRatio = 0.42f;
                        layoutStatus = "Split ratio reset";
                    }
                }))
                .child(slot -> slot.content(() -> {
                    if (ImGui.button("Center stack")) {
                        anchorStack.set(false);
                        layoutStatus = "Stack anchored to layout flow";
                    }
                }))
                .child(slot -> slot.content(() -> ImGui.textColored(0.4f, 0.8f, 0.5f, 1f, "Fill mode: MATCH_WIDEST")))
                .build();
    }

    private void renderSplit() {
        float availableWidth = Math.max(MIN_PANEL_WIDTH * 2, ImGui.getContentRegionAvailX());
        float spacing = ImGui.getStyle().getItemSpacingX();
        float leftWidth = Math.max(MIN_PANEL_WIDTH, availableWidth * splitRatio);
        float rightWidth = Math.max(MIN_PANEL_WIDTH, availableWidth - leftWidth - spacing);
        ImGui.beginChild("playground_layout_left", leftWidth, 230f, true);
        ImGui.text("Navigation");
        ImGui.separator();
        ImGui.bulletText("Stacks compose spacing without nesting screens.");
        ImGui.bulletText("Use LayoutConstraints for anchored panels.");
        ImGui.bulletText("Prefer MATCH_WIDEST for toolbars.");
        ImGui.endChild();
        ImGui.sameLine();
        ImGui.beginChild("playground_layout_right", rightWidth, 230f, true);
        ImGui.text("Details");
        ImGui.separator();
        ImGui.textWrapped("Right panels often fill the rest of the window. Keeping ratios clamped avoids collapsing when dockspaces resize.");
        if (ImGui.collapsingHeader("Live metrics", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.text("Left width: %.1f".formatted(leftWidth));
            ImGui.text("Right width: %.1f".formatted(rightWidth));
            ImGui.text("Spacing: %.1f".formatted(spacing));
        }
        ImGui.endChild();
    }

    private void renderStatusLine() {
        ImGui.text("Status: %s".formatted(layoutStatus));
        if (anchorStack.get()) {
            ImGui.textColored(0.6f, 0.9f, 0.6f, 1f, "Anchored VStack overlay enabled via LayoutConstraints.");
        }
    }

    private void renderGridSection() {
        if (showGridSampler.get()) {
            renderGridSampler();
            return;
        }
        ImGui.textDisabled("Grid sampler hidden — enable it to preview GridLayout + DSL interop.");
    }

    private void renderGridSampler() {
        ImGui.separator();
        ImGui.text("GridLayout sampler");
        GridLayout.Options options = new GridLayout.Options()
                .columnSpacing(8f)
                .rowSpacing(4f)
                .columns(
                        GridLayout.ColumnDefinition.fixed(120f),
                        GridLayout.ColumnDefinition.weight(1f),
                        GridLayout.ColumnDefinition.auto(120f)
                )
                .rows(
                        GridLayout.RowDefinition.auto(90f),
                        GridLayout.RowDefinition.auto()
                );
        try (GridLayout grid = GridLayout.begin(options)) {
            try (GridLayout.CellScope ignored = grid.cell(0, 0)) {
                ImGui.text("Column 1");
                ImGui.textWrapped("Fixed width keeps labels readable.");
            }
            try (GridLayout.CellScope ignored = grid.cell(1, 0)) {
                ImGui.text("Weighted content");
                ImGui.separator();
                ImGui.textWrapped("This column stretches with the window. Blend GridLayout with stacks if you need nested flows.");
            }
            try (GridLayout.CellScope ignored = grid.cell(2, 0)) {
                ImGui.text("Actions");
                ImGui.separator();
                if (ImGui.button("Snapshot##grid_sampler")) {
                    layoutStatus = "Grid snapshot requested";
                }
            }
            GridLayout.CellRequest detailsRequest = new GridLayout.CellRequest()
                    .columnSpan(2)
                    .estimateHeight(70f)
                    .fillWidth(true);
            try (GridLayout.CellScope ignored = grid.cell(0, 1, detailsRequest)) {
                ImGui.text("Row span example");
                ImGui.textWrapped("Cells can span multiple columns or rows so inspection panes ride alongside controls without extra BeginChild blocks.");
            }
            try (GridLayout.CellScope ignored = grid.cell(2, 1)) {
                ImGui.text("Status");
                ImGui.text(layoutStatus);
            }
        }
    }

    private void renderAnchoredCallout() {
        float offsetX = ScaleUnit.SCALED.applyWidth(18f);
        float offsetY = ScaleUnit.SCALED.applyHeight(16f);
        Constraints placement = new Constraints();
        placement.setX(Constraints.pixels(offsetX));
        placement.setY(Constraints.pixels(offsetY));
        LayoutConstraints anchor = LayoutConstraints.builder()
                .constraints(placement)
                .width(340f)
                .build();
        VStack.Options options = new VStack.Options()
                .placement(anchor)
                .spacing(6f)
                .fillMode(VStack.FillMode.MATCH_WIDEST);
        try (VStack anchored = VStack.begin(options)) {
            try (VStack.ItemScope ignored = anchored.next()) {
                ImGui.text("Anchored VStack");
                ImGui.textWrapped("LayoutConstraints target absolute positions so you can pin overlays to safe areas without writing custom ImGui position math.");
            }
            try (VStack.ItemScope ignored = anchored.next()) {
                ImGui.progressBar(0.6f, -1f, 0f, "Dockspace ready");
            }
        }
    }

}
