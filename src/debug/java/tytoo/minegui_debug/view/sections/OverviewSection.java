package tytoo.minegui_debug.view.sections;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.layout.LayoutTemplate;
import tytoo.minegui.view.View;

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
    public void render(View parent, LayoutApi layoutApi) {
        if (layoutApi == null) {
            renderLayoutUnavailable();
            return;
        }
        LayoutTemplate template = layoutApi.vertical()
                .spacing(6f)
                .child(slot -> slot.content(this::renderIntro))
                .child(slot -> slot.content(this::renderRuntimeControls))
                .child(slot -> slot.content(() -> renderChecklistSection(parent)))
                .child(slot -> slot.content(this::renderScratchPad))
                .build();
        layoutApi.render(template);
    }

    private void renderIntro() {
        ImGui.text("MineGui Playground");
        ImGui.textWrapped("A single view keeps all debug samples together. Each tab highlights ImGui primitives alongside MineGui helpers so you can inspect best practices without juggling multiple overlays.");
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
            ImGui.tableSetupColumn("Topic", ImGuiTableColumnFlags.WidthFixed, 140f);
            ImGui.tableSetupColumn("Guidance");
            renderPracticeRow("Immediate mode", "Keep state in fields (ImBoolean, ImString) and mutate it inline each frame.");
            renderPracticeRow("View lifecycle", parent.hasExplicitCursorPolicy()
                    ? "Cursor policy locked to view for predictable focus."
                    : "Views inherit cursor policies; call setCursorPolicy when you need explicit capture.");
            renderPracticeRow("Layout helpers", "Stacks and windows remain optional. Mix raw ImGui with MineGui helpers as needed.");
            renderPracticeRow("Namespaces", parent.getNamespace() != null
                    ? "Attached to namespace '%s' so layout templates can resolve resources.".formatted(parent.getNamespace())
                    : "Attach the view to a namespace to access layout services.");
            renderPracticeRow("Input relays", "Reusing ImString buffers avoids allocations while dragging sliders or typing.");
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

    private void renderLayoutUnavailable() {
        ImGui.text("Layout service unavailable");
        ImGui.textWrapped("Attach MineGui Playground to a namespace to evaluate the overview tab.");
    }
}
