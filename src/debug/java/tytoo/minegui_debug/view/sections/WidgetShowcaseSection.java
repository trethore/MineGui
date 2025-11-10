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

public final class WidgetShowcaseSection implements PlaygroundSection {
    private static final int SAMPLE_CAPACITY = 90;
    private final ImBoolean animate = new ImBoolean(true);
    private final ImBoolean showPlot = new ImBoolean(true);
    private final ImBoolean showTable = new ImBoolean(true);
    private final ImString commandBuffer = new ImString("/minegui debug", 128);
    private final float[] throughputSamples = new float[SAMPLE_CAPACITY];
    private float progress = 0.35f;
    private float sliderValue = 48f;
    private int sampleIndex;
    private String widgetStatus = "Idle";

    @Override
    public String tabLabel() {
        return "Widgets";
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
                .child(slot -> slot.content(this::renderControls))
                .child(slot -> slot.height(90f).fillWidth(true).content(this::renderPlotSection))
                .child(slot -> slot.content(this::renderTableSection))
                .build();
        layoutApi.render(template);
    }

    private void renderIntro() {
        ImGui.text("MineGui keeps ImGui primitives unwrapped so you can mix helpers with raw calls.");
    }

    private void renderControls() {
        updateAnimation();
        ImGui.checkbox("Animate values", animate);
        ImGui.sameLine();
        ImGui.checkbox("Show plot", showPlot);
        ImGui.sameLine();
        ImGui.checkbox("Show widget table", showTable);
        ImGui.progressBar(progress, -1f, 0f, "Streaming preview");
        float[] sliderHolder = {sliderValue};
        if (ImGui.sliderFloat("Chunk radius", sliderHolder, 16f, 96f, "%.0f blocks")) {
            sliderValue = sliderHolder[0];
            widgetStatus = "Chunk radius set to %.0f".formatted(sliderValue);
        }
        if (ImGui.inputTextWithHint("Command buffer", "Immediate mode keeps buffers hot", commandBuffer)) {
            widgetStatus = "Command updated";
        }
        ImGui.text("Status: %s".formatted(widgetStatus));
    }

    private void renderPlotSection() {
        if (!showPlot.get()) {
            ImGui.textDisabled("Enable plots to visualize live ImGuiIO samples.");
            return;
        }
        ImGui.plotLines("Frame samples", throughputSamples, throughputSamples.length, sampleIndex, "", 0f, 1f, 0f, 80f);
    }

    private void renderTableSection() {
        if (!showTable.get()) {
            ImGui.textDisabled("Enable the widget table to review helper tips.");
            return;
        }
        renderWidgetTable();
    }

    private void renderWidgetTable() {
        int flags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchSame;
        if (ImGui.beginTable("widget_best_practices", 3, flags)) {
            ImGui.tableSetupColumn("Widget", ImGuiTableColumnFlags.WidthFixed, 120f);
            ImGui.tableSetupColumn("Purpose");
            ImGui.tableSetupColumn("MineGui tip");
            renderRow("ImBoolean", "Wrap toggles and checkboxes.", "Hold instances per view to avoid GC churn.");
            renderRow("ImString", "Text input buffers.", "Reuse buffers and seed defaults in constructors.");
            renderRow("Tables", "Structured data", "Combine RowBg + Borders for clarity.");
            renderRow("Plot lines", "Runtime graphs", "Feed ring buffers to keep allocations flat.");
            renderRow("Progress bars", "Async feedback", "Drive fractions from delta time and clamp between 0-1.");
            ImGui.endTable();
        }
    }

    private void renderRow(String widget, String purpose, String tip) {
        ImGui.tableNextRow();
        ImGui.tableSetColumnIndex(0);
        ImGui.text(widget);
        ImGui.tableSetColumnIndex(1);
        ImGui.textWrapped(purpose);
        ImGui.tableSetColumnIndex(2);
        ImGui.textWrapped(tip);
    }

    private void updateAnimation() {
        ImGuiIO io = ImGui.getIO();
        if (animate.get()) {
            float delta = io.getDeltaTime();
            progress += delta * 0.25f;
            if (progress > 1f) {
                progress -= 1f;
            }
            float sample = 0.25f + (float) (Math.sin(ImGui.getTime()) * 0.25f) + progress * 0.5f;
            throughputSamples[sampleIndex] = Math.min(1f, Math.max(0f, sample));
            sampleIndex = (sampleIndex + 1) % throughputSamples.length;
        }
    }

    private void renderLayoutUnavailable() {
        ImGui.text("Layout service unavailable");
        ImGui.textWrapped("Attach MineGui Playground to a namespace to inspect widget helpers.");
    }
}
