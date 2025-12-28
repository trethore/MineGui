package tytoo.mineguidebug.view;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import tytoo.minegui.helper.LayoutHelper;
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
                    .flags(ImGuiWindowFlags.NoCollapse)
                    .initPos(160, 140)
                    .initDimensions(640, 560)
                    .render(() -> {
                        renderTabs();
                        renderFooter();
                    });

        }
    }

    private void renderFooter() {
        LayoutHelper.verticalSpace(4f);
        ImGui.separator();
        ImGui.textDisabled("MineGui Debug Mode");
        ImGui.sameLine();
        float width = ImGui.getWindowContentRegionMaxX();
        String fps = "%.1f FPS".formatted(ImGui.getIO().getFramerate());
        float textWidth = ImGui.calcTextSize(fps).x;
        ImGui.setCursorPosX(width - textWidth - 8f);
        ImGui.textDisabled(fps);
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
        // Modern Dark Theme
        colors.set(ImGuiCol.WindowBg, ImGui.getColorU32(0.10f, 0.11f, 0.13f, 0.98f));
        colors.set(ImGuiCol.Header, ImGui.getColorU32(0.20f, 0.25f, 0.30f, 1.00f));
        colors.set(ImGuiCol.HeaderHovered, ImGui.getColorU32(0.26f, 0.32f, 0.38f, 1.00f));
        colors.set(ImGuiCol.HeaderActive, ImGui.getColorU32(0.18f, 0.22f, 0.27f, 1.00f));
        colors.set(ImGuiCol.Tab, ImGui.getColorU32(0.15f, 0.18f, 0.22f, 1.00f));
        colors.set(ImGuiCol.TabHovered, ImGui.getColorU32(0.24f, 0.40f, 0.75f, 1.00f));
        colors.set(ImGuiCol.TabActive, ImGui.getColorU32(0.20f, 0.35f, 0.65f, 1.00f));
        colors.set(ImGuiCol.Button, ImGui.getColorU32(0.20f, 0.35f, 0.65f, 0.90f));
        colors.set(ImGuiCol.ButtonHovered, ImGui.getColorU32(0.24f, 0.40f, 0.75f, 1.00f));
        colors.set(ImGuiCol.ButtonActive, ImGui.getColorU32(0.16f, 0.30f, 0.55f, 1.00f));
        colors.set(ImGuiCol.FrameBg, ImGui.getColorU32(0.15f, 0.18f, 0.22f, 1.00f));
        colors.set(ImGuiCol.FrameBgHovered, ImGui.getColorU32(0.20f, 0.25f, 0.30f, 1.00f));
        colors.set(ImGuiCol.FrameBgActive, ImGui.getColorU32(0.18f, 0.22f, 0.27f, 1.00f));
        colors.set(ImGuiCol.CheckMark, ImGui.getColorU32(0.35f, 0.65f, 1.00f, 1.00f));
        colors.set(ImGuiCol.SliderGrab, ImGui.getColorU32(0.35f, 0.65f, 1.00f, 1.00f));
        colors.set(ImGuiCol.SliderGrabActive, ImGui.getColorU32(0.24f, 0.52f, 0.90f, 1.00f));
        colors.set(ImGuiCol.Text, ImGui.getColorU32(0.90f, 0.90f, 0.92f, 1.00f));
        colors.set(ImGuiCol.TextDisabled, ImGui.getColorU32(0.50f, 0.55f, 0.60f, 1.00f));
        colors.set(ImGuiCol.Border, ImGui.getColorU32(0.25f, 0.30f, 0.35f, 0.50f));

        return StyleDelta.builder()
                .windowRounding(8f)
                .frameRounding(4f)
                .popupRounding(4f)
                .scrollbarRounding(8f)
                .grabRounding(4f)
                .tabRounding(4f)
                .framePadding(10f, 6f)
                .itemSpacing(10f, 8f)
                .scrollbarSize(14f)
                .colorPalette(colors.build())
                .build();
    }
}

