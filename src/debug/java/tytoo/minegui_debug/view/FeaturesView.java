package tytoo.minegui_debug.view;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.util.Identifier;
import tytoo.minegui.helper.UI;
import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.window.Window;
import tytoo.minegui.style.ColorPalette;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.View;
import tytoo.minegui.view.cursor.CursorPolicies;
import tytoo.minegui_debug.MineGuiDebugCore;

public final class FeaturesView extends View {
    private static final Identifier IMGUI_ICON = Identifier.of("minegui", "icon.png");
    private static final float WINDOW_WIDTH = 560.0f;
    private static final float WINDOW_HEIGHT = 540.0f;
    private static final int SAMPLE_CAPACITY = 64;
    private static final String[] HIGHLIGHT_TITLES = {
            "Immediate mode core",
            "Lightweight helpers",
            "Fabric lifecycle integration",
            "Style and palette bridge",
            "Cursor control policies"
    };
    private static final String[] HIGHLIGHT_DETAILS = {
            "MineGui keeps the entire render lifecycle immediate. Each MGView runs inline with the client tick, so authored ImGui commands stay deterministic and easy to reason about even when Minecraft screens change.",
            "Helpers like VStack, HStack, and MGWindow add just enough structure for complex tools. Chain them when needed, or fall back to raw ImGui calls without fighting a retained tree or hidden state.",
            "Initialization flows piggyback Fabric events to manage ImGui contexts, namespaces, and dockspace wiring. That keeps overlays live across dimension loads and reconnects without manual plumbing.",
            "Styles translate Dear ImGui metrics, rounding, and palettes into MineGui descriptors. Capture, persist, and reuse themes while respecting namespaces or apply them globally with one call.",
            "Cursor policies are first-class. Flip between screen-unlocked overlays, docked editors, or input capture without rewriting GLFW hooks—and extend the registry when a tool needs bespoke behaviour."
    };
    private static final String[] LAYOUT_FEATURES = {
            "Stacks compose deterministic spacing without sacrificing raw ImGui flexibility.",
            "Constraint helpers pin overlays to safe areas, centers, or pixel offsets.",
            "MGWindow reuses scoped titles and placement so tools stay consistent after reloads.",
            "Dockspace customizers adjust central nodes, padding, and window rules per namespace.",
            "Layout APIs remain opt-in—drop back to ImGui.beginChild when bespoke flow is faster."
    };
    private static final String[] TOOLING_FEATURES = {
            "Namespace-aware view registry with toggle helpers.",
            "Style descriptors captured from live ImGui sessions.",
            "Font library bootstrap with JetBrains Mono fallback.",
            "Image utilities that reuse Minecraft textures safely.",
            "Save manager hooks for opt-in persistence."
    };
    private static final String[][] WIDGET_TABLE_ROWS = {
            {"Progress bar", "Render instantaneous feedback", "Pair with ImGuiIO.getDeltaTime() for smooth animation"},
            {"Plot lines", "Sample frame throughput or tick timing", "Wrap data in ring buffers to avoid allocations"},
            {"Input text", "Collect immediate user commands", "ImString keeps buffers reusable between frames"},
            {"Tables", "Lay out editor data grids", "Enable RowBg and Borders for readable debug panes"}
    };

    private final ImBoolean animateWidgets = new ImBoolean(true);
    private final ImBoolean showOverlay = new ImBoolean(true);
    private final ImBoolean showTexture = new ImBoolean(true);
    private final ImBoolean pinWindow = new ImBoolean(false);
    private final ImString scratchInput = new ImString(256);
    private final float[] throughputSamples = new float[SAMPLE_CAPACITY];
    private final float[] accentColor = {0.0f, 0.47058824f, 0.84313726f, 1.0f};
    private int throughputIndex;
    private float accentPhase;
    private float progressValue = 0.35f;
    private float sliderValue = 32.0f;
    private int selectedHighlight;

    public FeaturesView() {
        super(MineGuiDebugCore.ID, "features_view", false);
        setCursorPolicy(CursorPolicies.screen());
        scratchInput.set("Type here to test ImGui input relays.");
    }

    private static ColorPalette createWindarkPalette() {
        ColorPalette.Builder builder = ColorPalette.builder();
        builder.set(ImGuiCol.Text, color(1.0f, 1.0f, 1.0f, 1.0f));
        builder.set(ImGuiCol.TextDisabled, color(0.6f, 0.6f, 0.6f, 1.0f));
        builder.set(ImGuiCol.WindowBg, color(0.1254902f, 0.1254902f, 0.1254902f, 1.0f));
        builder.set(ImGuiCol.ChildBg, color(0.1254902f, 0.1254902f, 0.1254902f, 1.0f));
        builder.set(ImGuiCol.PopupBg, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.Border, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.BorderShadow, color(0.0f, 0.0f, 0.0f, 0.0f));
        builder.set(ImGuiCol.FrameBg, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.FrameBgHovered, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.FrameBgActive, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.TitleBg, color(0.1254902f, 0.1254902f, 0.1254902f, 1.0f));
        builder.set(ImGuiCol.TitleBgActive, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.TitleBgCollapsed, color(0.1254902f, 0.1254902f, 0.1254902f, 1.0f));
        builder.set(ImGuiCol.MenuBarBg, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.ScrollbarBg, color(0.1254902f, 0.1254902f, 0.1254902f, 1.0f));
        builder.set(ImGuiCol.ScrollbarGrab, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.ScrollbarGrabHovered, color(0.3019608f, 0.3019608f, 0.3019608f, 1.0f));
        builder.set(ImGuiCol.ScrollbarGrabActive, color(0.34901962f, 0.34901962f, 0.34901962f, 1.0f));
        builder.set(ImGuiCol.CheckMark, color(0.0f, 0.47058824f, 0.84313726f, 1.0f));
        builder.set(ImGuiCol.SliderGrab, color(0.0f, 0.47058824f, 0.84313726f, 1.0f));
        builder.set(ImGuiCol.SliderGrabActive, color(0.0f, 0.32941177f, 0.6f, 1.0f));
        builder.set(ImGuiCol.Button, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.ButtonHovered, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.ButtonActive, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.Header, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.HeaderHovered, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.HeaderActive, color(0.3019608f, 0.3019608f, 0.3019608f, 1.0f));
        builder.set(ImGuiCol.Separator, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.SeparatorHovered, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.SeparatorActive, color(0.3019608f, 0.3019608f, 0.3019608f, 1.0f));
        builder.set(ImGuiCol.ResizeGrip, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.ResizeGripHovered, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.ResizeGripActive, color(0.3019608f, 0.3019608f, 0.3019608f, 1.0f));
        builder.set(ImGuiCol.Tab, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.TabHovered, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.TabActive, color(0.2509804f, 0.2509804f, 0.2509804f, 1.0f));
        builder.set(ImGuiCol.TabUnfocused, color(0.16862746f, 0.16862746f, 0.16862746f, 1.0f));
        builder.set(ImGuiCol.TabUnfocusedActive, color(0.21568628f, 0.21568628f, 0.21568628f, 1.0f));
        builder.set(ImGuiCol.PlotLines, color(0.0f, 0.47058824f, 0.84313726f, 1.0f));
        builder.set(ImGuiCol.PlotLinesHovered, color(0.0f, 0.32941177f, 0.6f, 1.0f));
        builder.set(ImGuiCol.PlotHistogram, color(0.0f, 0.47058824f, 0.84313726f, 1.0f));
        builder.set(ImGuiCol.PlotHistogramHovered, color(0.0f, 0.32941177f, 0.6f, 1.0f));
        builder.set(ImGuiCol.TableHeaderBg, color(0.1882353f, 0.1882353f, 0.2f, 1.0f));
        builder.set(ImGuiCol.TableBorderStrong, color(0.30980393f, 0.30980393f, 0.34901962f, 1.0f));
        builder.set(ImGuiCol.TableBorderLight, color(0.22745098f, 0.22745098f, 0.24705882f, 1.0f));
        builder.set(ImGuiCol.TableRowBg, color(0.0f, 0.0f, 0.0f, 0.0f));
        builder.set(ImGuiCol.TableRowBgAlt, color(1.0f, 1.0f, 1.0f, 0.06f));
        builder.set(ImGuiCol.TextSelectedBg, color(0.0f, 0.47058824f, 0.84313726f, 1.0f));
        builder.set(ImGuiCol.DragDropTarget, color(1.0f, 1.0f, 0.0f, 0.9f));
        builder.set(ImGuiCol.NavHighlight, color(0.25882354f, 0.5882353f, 0.9764706f, 1.0f));
        builder.set(ImGuiCol.NavWindowingHighlight, color(1.0f, 1.0f, 1.0f, 0.7f));
        builder.set(ImGuiCol.NavWindowingDimBg, color(0.8f, 0.8f, 0.8f, 0.2f));
        builder.set(ImGuiCol.ModalWindowDimBg, color(0.8f, 0.8f, 0.8f, 0.35f));
        return builder.build();
    }

    private static int color(float r, float g, float b, float a) {
        return ImGui.getColorU32(r, g, b, a);
    }

    @Override
    public StyleDescriptor configureBaseStyle(StyleDescriptor descriptor) {
        var builder = StyleDescriptor.builder();
        if (descriptor != null) {
            builder.fromDescriptor(descriptor);
        } else {
            builder.fromStyle(ImGui.getStyle());
        }
        builder.alpha(1.0f)
                .disabledAlpha(0.6f)
                .windowPadding(8.0f, 8.0f)
                .windowRounding(8.4f)
                .windowBorderSize(1.0f)
                .windowMinSize(32.0f, 32.0f)
                .windowTitleAlign(0.0f, 0.5f)
                .windowMenuButtonPosition(ImGuiDir.Right)
                .childRounding(3.0f)
                .childBorderSize(1.0f)
                .popupRounding(3.0f)
                .popupBorderSize(1.0f)
                .framePadding(4.0f, 3.0f)
                .frameRounding(3.0f)
                .frameBorderSize(1.0f)
                .itemSpacing(8.0f, 4.0f)
                .itemInnerSpacing(4.0f, 4.0f)
                .cellPadding(4.0f, 2.0f)
                .indentSpacing(21.0f)
                .columnsMinSpacing(6.0f)
                .scrollbarSize(5.6f)
                .scrollbarRounding(18.0f)
                .grabMinSize(10.0f)
                .grabRounding(3.0f)
                .tabRounding(3.0f)
                .tabBorderSize(0.0f)
                .tabMinWidthForCloseButton(0.0f)
                .colorButtonPosition(ImGuiDir.Right)
                .buttonTextAlign(0.5f, 0.5f)
                .selectableTextAlign(0.0f, 0.0f)
                .colorPalette(createWindarkPalette());
        return builder.build();
    }

    @Override
    protected void renderView() {
        advanceAnimation();
        Window.of(this, "ImGui Features")
                .initDimensions(WINDOW_WIDTH, WINDOW_HEIGHT)
                .flags(resolveWindowFlags())
                .render(() -> UI.withVStack(new VStack.Options().spacing(10.0f).fillMode(VStack.FillMode.MATCH_WIDEST), layout -> {
                    renderHeader(layout);
                    renderControls(layout);
                    renderTabs(layout);
                    renderFooter(layout);
                }));
        renderOverlay();
    }

    private void advanceAnimation() {
        if (!animateWidgets.get()) {
            return;
        }
        ImGuiIO io = ImGui.getIO();
        float delta = io.getDeltaTime();
        accentPhase = (accentPhase + delta * 0.65f) % 1.0f;
        float cycle = (float) Math.sin(accentPhase * 6.2831855f) * 0.5f + 0.5f;
        progressValue = cycle;
        throughputSamples[throughputIndex] = cycle;
        throughputIndex = (throughputIndex + 1) % SAMPLE_CAPACITY;
    }

    private int resolveWindowFlags() {
        int flags = ImGuiWindowFlags.NoCollapse;
        if (pinWindow.get()) {
            flags |= ImGuiWindowFlags.NoMove;
        }
        return flags;
    }

    private void renderHeader(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ImGui.text("Immediate-mode UI playbook");
            ImGui.sameLine();
            ImGui.textDisabled("Press J to toggle");
            ImGui.separator();
            ImGui.textWrapped("Explore MineGui features live. Combine stack-based helpers with raw ImGui commands, preview styling, and surface runtime diagnostics without leaving the client.");
        });
    }

    private void renderControls(VStack layout) {
        UI.withVStackItem(layout, () -> {
            float spacing = ImGui.getStyle().getItemSpacingX();
            ImGui.checkbox("Animate metrics", animateWidgets);
            ImGui.sameLine(0.0f, spacing);
            ImGui.checkbox("Runtime overlay", showOverlay);
            ImGui.sameLine(0.0f, spacing);
            ImGui.checkbox("Texture preview", showTexture);
            ImGui.sameLine(0.0f, spacing);
            ImGui.checkbox("Pin window position", pinWindow);
        });
    }

    private void renderTabs(VStack layout) {
        UI.withVStackItem(layout, () -> {
            if (ImGui.beginTabBar("features_tabbar")) {
                if (ImGui.beginTabItem("Overview")) {
                    renderOverviewTab();
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Layouts")) {
                    renderLayoutTab();
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Widgets")) {
                    renderWidgetsTab();
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Assets")) {
                    renderAssetsTab();
                    ImGui.endTabItem();
                }
                ImGui.endTabBar();
            }
        });
    }

    private void renderOverviewTab() {
        ImGui.text("Pick a highlight to learn more");
        ImGui.spacing();
        float childHeight = 140.0f;
        if (ImGui.beginChild("highlight_list", 0.0f, childHeight, true)) {
            for (int i = 0; i < HIGHLIGHT_TITLES.length; i++) {
                boolean selected = i == selectedHighlight;
                if (ImGui.selectable(HIGHLIGHT_TITLES[i], selected)) {
                    selectedHighlight = i;
                }
            }
        }
        ImGui.endChild();
        ImGui.separator();
        ImGui.textWrapped(HIGHLIGHT_DETAILS[selectedHighlight]);
        ImGui.spacing();
        ImGui.separator();
        ImGui.text("Tooling quick hits");
        for (String entry : TOOLING_FEATURES) {
            ImGui.bulletText(entry);
        }
    }

    private void renderLayoutTab() {
        UI.withVStack(new VStack.Options().spacing(8.0f).fillMode(VStack.FillMode.MATCH_WIDEST), inner -> {
            UI.withVStackItem(inner, () -> {
                ImGui.text("Layout sampler");
                ImGui.textWrapped("Stack helpers, constraints, and windows layer together so you can sketch tools quickly. Use stacks for rhythm, constraints for placement, and MGWindow to preserve state.");
            });
            UI.withVStackItem(inner, new VStack.ItemRequest().estimateHeight(200.0f), () -> UI.withHStack(new HStack.Options().spacing(16.0f).alignment(HStack.Alignment.TOP), row -> {
                UI.withHItem(row, 220.0f, () -> {
                    ImGui.text("Stacks");
                    ImGui.textWrapped("Compose vertical and horizontal regions with consistent spacing and alignment hints. Use VStack and HStack together when you need predictable rhythm without losing immediacy.");
                    ImGui.separator();
                    ImGui.text("Constraints");
                    ImGui.textWrapped("Anchor overlays relative to viewport safe areas or absolute pixels. Pixel constraints keep HUD tools glued in place while remaining reactive to resolution changes.");
                });
                UI.withHItem(row, () -> {
                    if (ImGui.beginChild("layout_preview_panel", 0.0f, 160.0f, true)) {
                        ImGui.text("Toolbar preview");
                        ImGui.spacing();
                        UI.withHStack(new HStack.Options().spacing(8.0f).alignment(HStack.Alignment.CENTER), previewRow -> {
                            UI.withHItem(previewRow, () -> ImGui.button("Primary", 110.0f, 0.0f));
                            UI.withHItem(previewRow, () -> ImGui.button("Secondary", 100.0f, 0.0f));
                            UI.withHItem(previewRow, () -> ImGui.button("Ghost", 90.0f, 0.0f));
                        });
                        ImGui.separator();
                        ImGui.text("Anchor preview");
                        ImGui.spacing();
                        ImGui.text("• Top-left: X=16 px, Y=24 px");
                        ImGui.text("• Bottom-right: cling to viewport safe area");
                        ImGui.spacing();
                        ImGui.textDisabled("MGWindow remembers placement across reloads.");
                    }
                    ImGui.endChild();
                });
            }));
            UI.withVStackItem(inner, () -> {
                ImGui.separator();
                ImGui.text("Layout checklist");
                for (String feature : LAYOUT_FEATURES) {
                    ImGui.bulletText(feature);
                }
            });
        });
    }

    private void renderWidgetsTab() {
        ImGui.text("Live metrics");
        float[] sliderHolder = {sliderValue};
        if (ImGui.sliderFloat("Buffer size", sliderHolder, 8.0f, 96.0f, "%.0f px")) {
            sliderValue = sliderHolder[0];
        }
        float plotWidth = ImGui.getContentRegionAvailX();
        if (plotWidth <= 0.0f) {
            plotWidth = 120.0f;
        }
        ImGui.progressBar(progressValue, plotWidth, 0.0f, "%.0f%%".formatted(progressValue * 100.0f));
        ImGui.plotLines("Throughput", throughputSamples, throughputSamples.length, throughputIndex, "", 0.0f, 1.0f, plotWidth, 60.0f);
        ImGui.separator();
        ImGui.text("Scratch input");
        ImGui.inputText("Command palette", scratchInput);
        ImGui.separator();
        ImGui.text("Widget recipes");
        boolean tableOpen = ImGui.beginTable("widget_matrix", 3, ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp);
        if (tableOpen) {
            ImGui.tableSetupColumn("Widget");
            ImGui.tableSetupColumn("Purpose");
            ImGui.tableSetupColumn("Tip");
            ImGui.tableHeadersRow();
            for (String[] row : WIDGET_TABLE_ROWS) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.text(row[0]);
                ImGui.tableNextColumn();
                ImGui.textWrapped(row[1]);
                ImGui.tableNextColumn();
                ImGui.textWrapped(row[2]);
            }
            ImGui.endTable();
        }
        ImGui.separator();
        ImGui.text("Accent color");
        if (ImGui.colorEdit4("##accent_color_edit", accentColor)) {
            ImGui.textColored(accentColor[0], accentColor[1], accentColor[2], accentColor[3], "Color updated");
        } else {
            ImGui.textColored(accentColor[0], accentColor[1], accentColor[2], accentColor[3], "Preview");
        }
    }

    private void renderAssetsTab() {
        ImGui.text("Texture preview via ImGuiImageUtils");
        if (showTexture.get()) {
            float size = 96.0f;
            float cursorX = ImGui.getCursorScreenPosX();
            float cursorY = ImGui.getCursorScreenPosY();
            ImGuiImageUtils.drawImage(IMGUI_ICON, cursorX, cursorY, cursorX + size, cursorY + size, 0, false, ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f));
            ImGui.dummy(size, size);
        }
        ImGui.text("Identifier");
        ImGui.textDisabled(IMGUI_ICON.toString());
    }

    private void renderFooter(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ImGui.separator();
            ImGui.textDisabled("Namespace minegui_debug · Hotkey J");
        });
    }

    private void renderOverlay() {
        if (!showOverlay.get()) {
            return;
        }
        ImGuiViewport viewport = ImGui.getMainViewport();
        float posX = viewport.getWorkPosX() + viewport.getWorkSizeX() - 230.0f;
        float posY = viewport.getWorkPosY() + 30.0f;
        ImGui.setNextWindowPos(posX, posY, ImGuiCond.Always);
        ImGui.setNextWindowBgAlpha(0.55f);
        int flags = ImGuiWindowFlags.NoDecoration
                | ImGuiWindowFlags.AlwaysAutoResize
                | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoDocking;
        if (!ImGui.begin(scopedWindowTitle("MineGui Overlay"), flags)) {
            ImGui.end();
            return;
        }
        ImGui.text("Frame %.1f FPS".formatted(ImGui.getIO().getFramerate()));
        ImGui.text("Progress %.0f%%".formatted(progressValue * 100.0f));
        ImGui.text("Buffer %.0f px".formatted(sliderValue));
        ImGui.end();
    }
}
