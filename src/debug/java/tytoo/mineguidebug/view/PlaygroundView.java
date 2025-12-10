package tytoo.mineguidebug.view;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import tytoo.minegui.helper.window.Window;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.mineguidebug.view.sections.*;

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
        super("playground_view");
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
}
