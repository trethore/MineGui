package tytoo.minegui_debug.view;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import tytoo.minegui.helper.window.Window;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui_debug.MineGuiDebugCore;
import tytoo.minegui_debug.view.sections.*;

import java.util.List;

public final class PlaygroundView extends View {
    private final List<PlaygroundSection> sections = List.of(
            new OverviewSection(),
            new LayoutShowcaseSection(),
            new WidgetShowcaseSection(),
            new StyleWorkflowSection(),
            new ResourcePreviewSection()
    );

    public PlaygroundView() {
        super(MineGuiDebugCore.ID, "playground_view");
        setCursorPolicy(CursorPolicies.clickToLock());
    }

    @Override
    protected void renderView() {
        Window.of(this, "MineGui Playground")
                .flags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar)
                .render(this::renderTabs);
    }

    private void renderTabs() {
        if (!ImGui.beginTabBar("minegui_playground_tabs")) {
            return;
        }
        LayoutApi api = layoutApi();
        for (PlaygroundSection section : sections) {
            String label = scopedTabLabel(section);
            if (ImGui.beginTabItem(label)) {
                section.render(this, api);
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
}
