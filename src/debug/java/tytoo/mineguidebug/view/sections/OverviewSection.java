package tytoo.mineguidebug.view.sections;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import tytoo.minegui.view.View;
import tytoo.mineguidebug.view.DebugLayout;

public final class OverviewSection implements PlaygroundSection {
    private final ImBoolean showRuntime = new ImBoolean(true);
    private final ImBoolean showPractices = new ImBoolean(true);
    private final ImString scratchPad = new ImString("Type inside to test MineGui input relays.", 256);
    private float averagedFps = -1f;
    private long frameCounter;

    @Override
    public String tabLabel() {
        return "Overview";
    }

    @Override
    public void render(View parent) {
        renderIntro();
        DebugLayout.sectionGap();
        renderRuntimeControls();
        DebugLayout.sectionGap();
        renderChecklistSection(parent);
        DebugLayout.sectionGap();
        renderScratchPad();
    }

    private void renderIntro() {
        ImGui.text("MineGui Playground");
        ImGui.textWrapped("""
                Tabs pack ImGui primitives and MineGui helpers in one place: try widgets, inspect styles, and preview resources without touching the stock ImGui demo window.
                Press G in-game (with no screen open) to toggle this view.
                """);
        ImGui.separator();
    }

    private void renderRuntimeControls() {
        ImGui.checkbox("Show runtime metrics", showRuntime);
        if (showRuntime.get()) {
            renderRuntime();
        }
    }

    private void renderRuntime() {
        ImGuiIO io = ImGui.getIO();
        float fps = io.getFramerate();
        if (!Float.isFinite(averagedFps) || averagedFps < 0f) {
            averagedFps = fps;
        } else {
            averagedFps = averagedFps * 0.9f + fps * 0.1f;
        }
        frameCounter++;
        ImGui.text("Instant FPS: %.1f".formatted(fps));
        ImGui.text("Smoothed FPS: %.1f".formatted(averagedFps));
        ImGui.text("Delta time: %.3f ms".formatted(io.getDeltaTime() * 1000f));
        ImGui.text("Frame count: %,d".formatted(frameCounter));
    }

    private void renderChecklistSection(View parent) {
        ImGui.checkbox("Show practice checklist", showPractices);
        int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp;
        if (!showPractices.get()) {
            ImGui.textDisabled("Enable the checklist to review MineGui best practices.");
            return;
        }
        if (ImGui.beginTable("playground_overview_practices", 2, flags)) {
            ImGui.tableSetupColumn("Topic", ImGuiTableColumnFlags.WidthFixed, DebugLayout.TABLE_LABEL_WIDTH);
            ImGui.tableSetupColumn("Guidance");
            renderPracticeRow("Immediate mode", "Hold ImBoolean/ImString fields in the section; ImGui pulls by reference each frame.");
            renderPracticeRow("Window helper", "Window.of(...) drives titles, flags, and docking in one call.");
            renderPracticeRow("Cursor policy", parent.hasExplicitCursorPolicy()
                    ? "Click-to-lock enabled for consistent focus."
                    : "Set an explicit policy when a view needs locked cursor behavior.");
            renderPracticeRow("Style registry", "NamedStyleRegistry keeps palettes reusable per view or namespace.");
            renderPracticeRow("Resource bridge", "ImGuiImageUtils pulls Minecraft textures straight into draw lists.");
            ImGui.endTable();
        }
    }

    private void renderScratchPad() {
        ImGui.separator();
        ImGui.inputTextMultiline("Scratch pad", scratchPad, 320.0f, 96.0f);
    }

    private void renderPracticeRow(String topic, String description) {
        ImGui.tableNextRow();
        ImGui.tableSetColumnIndex(0);
        ImGui.text(topic);
        ImGui.tableSetColumnIndex(1);
        ImGui.textWrapped(description);
    }

}
