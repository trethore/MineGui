package tytoo.mineguidebug.view;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import tytoo.minegui.helper.window.Window;
import tytoo.minegui.style.ColorPalette;
import tytoo.minegui.style.StyleDelta;
import tytoo.minegui.style.StyleScope;
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
        try (StyleScope ignored = StyleScope.push(theme())) {
            Window.of(this, "MineGui Playground")
                    .flags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar)
                    .initPos(160, 140)
                    .initDimensions(560, 520)
                    .render(this::renderTabs);

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

    private StyleDelta theme() {
        ColorPalette.Builder colors = ColorPalette.builder();
        colors.set(ImGuiCol.WindowBg, ImGui.getColorU32(0.07f, 0.08f, 0.11f, 0.97f));
        colors.set(ImGuiCol.Header, ImGui.getColorU32(0.18f, 0.38f, 0.72f, 0.90f));
        colors.set(ImGuiCol.HeaderHovered, ImGui.getColorU32(0.24f, 0.52f, 0.90f, 0.95f));
        colors.set(ImGuiCol.Tab, ImGui.getColorU32(0.12f, 0.20f, 0.32f, 0.95f));
        colors.set(ImGuiCol.TabHovered, ImGui.getColorU32(0.23f, 0.50f, 0.92f, 1f));
        colors.set(ImGuiCol.TabActive, ImGui.getColorU32(0.17f, 0.36f, 0.68f, 1f));
        colors.set(ImGuiCol.Button, ImGui.getColorU32(0.18f, 0.40f, 0.78f, 1f));
        colors.set(ImGuiCol.ButtonHovered, ImGui.getColorU32(0.23f, 0.54f, 0.94f, 1f));
        colors.set(ImGuiCol.ButtonActive, ImGui.getColorU32(0.16f, 0.31f, 0.60f, 1f));
        colors.set(ImGuiCol.FrameBg, ImGui.getColorU32(0.11f, 0.14f, 0.21f, 1f));
        colors.set(ImGuiCol.FrameBgHovered, ImGui.getColorU32(0.18f, 0.22f, 0.32f, 1f));
        colors.set(ImGuiCol.CheckMark, ImGui.getColorU32(0.36f, 0.74f, 1f, 1f));
        colors.set(ImGuiCol.SliderGrab, ImGui.getColorU32(0.36f, 0.74f, 1f, 1f));
        colors.set(ImGuiCol.SliderGrabActive, ImGui.getColorU32(0.23f, 0.52f, 0.90f, 1f));

        return StyleDelta.builder()
                .windowRounding(12f)
                .frameRounding(6f)
                .framePadding(8f, 6f)
                .itemSpacing(8f, 6f)
                .colorPalette(colors.build())
                .build();
    }
}
