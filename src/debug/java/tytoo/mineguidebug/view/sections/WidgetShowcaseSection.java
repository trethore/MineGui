package tytoo.mineguidebug.view.sections;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import tytoo.minegui.imgui.ref.FloatRef;
import tytoo.minegui.imgui.scope.DisabledScope;
import tytoo.minegui.imgui.scope.IdScope;
import tytoo.minegui.helper.LayoutHelper;
import tytoo.minegui.view.View;

public final class WidgetShowcaseSection implements PlaygroundSection {
    private static final int SAMPLE_CAPACITY = 90;
    private static final String[] THEME_PRESETS = {"Night Drive", "Terminal", "Sandstone"};
    private final ImBoolean animate = new ImBoolean(true);
    private final ImBoolean showPlot = new ImBoolean(true);
    private final ImBoolean showTable = new ImBoolean(true);
    private final ImBoolean showPalette = new ImBoolean(true);
    private final ImString commandBuffer = new ImString("/minegui debug", 128);
    private final ImString noteBuffer = new ImString("ImGui excels at live edits. Try pasting text.", 256);
    private final float[] throughputSamples = new float[SAMPLE_CAPACITY];
    private final float[] accentColor = new float[]{0.24f, 0.62f, 1f, 1f};
    private final float[] uvClamp = new float[]{0.15f, 0.85f};
    private final ImInt themeIndex = new ImInt(0);
    private final FloatRef progress = new FloatRef(0.35f);
    private final FloatRef sliderValue = new FloatRef(48f);
    private final FloatRef commandDelay = new FloatRef(0.18f);
    private int sampleIndex;
    private String widgetStatus = "Idle";

    @Override
    public String tabLabel() {
        return "Widgets";
    }

    @Override
    public void render(View parent) {
        renderIntro();
        LayoutHelper.sectionGap();
        renderControls();
        LayoutHelper.sectionGap();
        renderWidgetGrid();
        LayoutHelper.sectionGap();
        renderPlotSection();
        LayoutHelper.sectionGap();
        renderTableSection();
    }

    private void renderIntro() {
        ImGui.text("MineGui keeps ImGui primitives unwrapped so you can mix helpers with raw calls.");
        ImGui.textWrapped("Toggle widgets live to feel immediate-mode feedback: every change here mutates local state fields instead of rebuilding layouts.");
    }

    private void renderControls() {
        updateAnimation();
        ImGui.checkbox("Animate values", animate);
        ImGui.sameLine();
        ImGui.checkbox("Show plot", showPlot);
        ImGui.sameLine();
        ImGui.checkbox("Show widget table", showTable);
        ImGui.sameLine();
        ImGui.checkbox("Show palette", showPalette);
        ImGui.progressBar(progress.get(), -1f, 0f, "Streaming preview");
        if (sliderValue.sliderFloat("Chunk radius", 16f, 96f, "%.0f blocks")) {
            widgetStatus = "Chunk radius set to %.0f".formatted(sliderValue.get());
        }
        if (commandDelay.dragFloat("Sync delay", 0.01f, 0f, 0.4f, "%.2f s")) {
            widgetStatus = "Sync delay %.2fs".formatted(commandDelay.get());
        }
        if (ImGui.combo("Preset", themeIndex, THEME_PRESETS)) {
            widgetStatus = "%s preset staged".formatted(THEME_PRESETS[themeIndex.get()]);
        }
        if (ImGui.inputTextWithHint("Command buffer", "Immediate mode keeps buffers hot", commandBuffer)) {
            widgetStatus = "Command updated";
        }
        ImGui.text("Status: %s".formatted(widgetStatus));
    }

    private void renderWidgetGrid() {
        int flags = ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.BordersInnerV | ImGuiTableFlags.RowBg;
        if (ImGui.beginTable("widget_grid", 2, flags)) {
            ImGui.tableSetupColumn("Inputs", ImGuiTableColumnFlags.WidthStretch, 0.55f);
            ImGui.tableSetupColumn("Preview", ImGuiTableColumnFlags.WidthStretch, 0.45f);
            ImGui.tableNextRow();
            ImGui.tableSetColumnIndex(0);
            renderInputColumn();
            ImGui.tableSetColumnIndex(1);
            renderPreviewColumn();
            ImGui.endTable();
        }
    }

    private void renderInputColumn() {
        ImGui.text("Selectors");
        ImGui.separator();
        progress.dragFloat("Progress trim", 0.01f, 0f, 1f, "%.2f");
        float[] uvHolder = {uvClamp[0], uvClamp[1]};
        if (ImGui.dragFloat2("UV clamp", uvHolder, 0.01f, 0f, 1f, "%.2f")) {
            uvClamp[0] = uvHolder[0];
            uvClamp[1] = uvHolder[1];
        }
        if (showPalette.get() && ImGui.colorEdit4("Accent color", accentColor)) {
            widgetStatus = "Accent color tuned";
        }
        if (ImGui.inputTextMultiline("Notes", noteBuffer, 240f, 80f)) {
            widgetStatus = "Notes edited";
        }
    }

    private void renderPreviewColumn() {
        ImGui.text("Live preview");
        ImGui.separator();
        ImGui.textColored(accentColor[0], accentColor[1], accentColor[2], 1f, "Preset: %s".formatted(THEME_PRESETS[themeIndex.get()]));
        ImGui.bulletText("UV clamp: %.2f -> %.2f".formatted(uvClamp[0], uvClamp[1]));
        ImGui.bulletText("Delay: %.2fs".formatted(commandDelay.get()));
        ImGui.separator();
        if (ImGui.treeNode("Action stack")) {
            ImGui.text("- Apply preset");
            ImGui.text("- Update command buffer");
            ImGui.text("- Stream to MineGui overlay");
            ImGui.treePop();
        }
        LayoutHelper.tinyGap();
        ImGui.text("Command buffer");
        try (IdScope ignored = IdScope.of("command_preview");
             DisabledScope ignored2 = DisabledScope.of()) {
            ImGui.inputText("##command", commandBuffer);
        }
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
            ImGui.tableSetupColumn("Widget", ImGuiTableColumnFlags.WidthFixed, LayoutHelper.TABLE_LABEL_WIDTH_SMALL);
            ImGui.tableSetupColumn("Purpose");
            ImGui.tableSetupColumn("MineGui tip");
            renderRow("ImBoolean", "Wrap toggles and checkboxes.", "Keep them as fields; ImGui pulls values by reference.");
            renderRow("ImString", "Text input buffers.", "Reuse buffers and seed defaults in constructors.");
            renderRow("Tables", "Structured data", "RowBg + Borders read well over dark overlays.");
            renderRow("Plot lines", "Runtime graphs", "Feed ring buffers to keep allocations flat.");
            renderRow("ColorEdit", "Live styling", "Pair with NamedStyleRegistry to drive palettes.");
            renderRow("Combo/Drag", "Preset swaps", "Use holder arrays to mutate numeric state inline.");
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
            float currentProgress = progress.get();
            currentProgress += delta * 0.25f;
            if (currentProgress > 1f) {
                currentProgress -= 1f;
            }
            progress.set(currentProgress);
            float sample = 0.25f + (float) (Math.sin(ImGui.getTime()) * 0.25f) + currentProgress * 0.5f;
            throughputSamples[sampleIndex] = Math.min(1f, Math.max(0f, sample));
            sampleIndex = (sampleIndex + 1) % throughputSamples.length;
        }
    }

}
