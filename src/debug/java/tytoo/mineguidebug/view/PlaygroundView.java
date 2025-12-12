package tytoo.mineguidebug.view;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import tytoo.minegui.helper.window.Window;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.mineguidebug.view.sections.*;

import java.util.List;

public final class PlaygroundView extends View {
    private static final int STYLE_VAR_PUSH_COUNT = 4;
    private static final int STYLE_COLOR_PUSH_COUNT = 14;
    private final List<PlaygroundSection> sections = List.of(
            new OverviewSection(),
            new LayoutShowcaseSection(),
            new WidgetShowcaseSection(),
            new StyleWorkflowSection(),
            new ResourcePreviewSection()
    );

    public PlaygroundView() {
        super("playground_view");
        setCursorPolicy(CursorPolicies.clickToLock());
    }

    @Override
    protected void renderView() {
        pushTheme();
        try {
            Window.of(this, "MineGui Playground")
                    .flags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar)
                    .initPos(160, 140)
                    .initDimensions(560, 520)
                    .render(this::renderTabs);

        } finally {
            popTheme();
        }
    }

    private void renderTabs() {
        if (!ImGui.beginTabBar("minegui_playground_tabs")) {
            return;
        }
        for (PlaygroundSection section : sections) {
            String label = scopedTabLabel(section);
            if (ImGui.beginTabItem(label)) {
                section.render(this);
                ImGui.endTabItem();
            }
        }
        ImGui.endTabBar();
    }

    private String scopedTabLabel(PlaygroundSection section) {
        String label = section.tabLabel();
        if (label == null || label.isBlank()) {
            label = section.getClass().getSimpleName();
        }
        if (label.contains("##")) {
            return label;
        }
        return label + "##" + section.tabId();
    }

    private void pushTheme() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 12f);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 8f, 6f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 8f, 6f);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.07f, 0.08f, 0.11f, 0.97f);
        ImGui.pushStyleColor(ImGuiCol.Header, 0.18f, 0.38f, 0.72f, 0.90f);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.24f, 0.52f, 0.90f, 0.95f);
        ImGui.pushStyleColor(ImGuiCol.Tab, 0.12f, 0.20f, 0.32f, 0.95f);
        ImGui.pushStyleColor(ImGuiCol.TabHovered, 0.23f, 0.50f, 0.92f, 1f);
        ImGui.pushStyleColor(ImGuiCol.TabActive, 0.17f, 0.36f, 0.68f, 1f);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.18f, 0.40f, 0.78f, 1f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.23f, 0.54f, 0.94f, 1f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.31f, 0.60f, 1f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.11f, 0.14f, 0.21f, 1f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.18f, 0.22f, 0.32f, 1f);
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 0.36f, 0.74f, 1f, 1f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, 0.36f, 0.74f, 1f, 1f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, 0.23f, 0.52f, 0.90f, 1f);
    }

    private void popTheme() {
        ImGui.popStyleColor(STYLE_COLOR_PUSH_COUNT);
        ImGui.popStyleVar(STYLE_VAR_PUSH_COUNT);
    }
}
