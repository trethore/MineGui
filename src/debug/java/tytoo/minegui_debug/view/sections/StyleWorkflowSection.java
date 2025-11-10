package tytoo.minegui_debug.view.sections;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.layout.LayoutTemplate;
import tytoo.minegui.style.ColorPalette;
import tytoo.minegui.style.NamedStyleRegistry;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;
import tytoo.minegui_debug.MineGuiDebugCore;

public final class StyleWorkflowSection implements PlaygroundSection {
    private static final ResourceId MINIMAL_STYLE = ResourceId.of(MineGuiDebugCore.ID, "playground_minimal_style");
    private static final ResourceId ACCENT_STYLE = ResourceId.of(MineGuiDebugCore.ID, "playground_accent_style");
    private final ImBoolean applyNamespaceWide = new ImBoolean(true);
    private boolean descriptorsRegistered;
    private ResourceId selectedKey = MINIMAL_STYLE;

    @Override
    public String tabLabel() {
        return "Styles";
    }

    @Override
    public void render(View parent, LayoutApi layoutApi) {
        if (layoutApi == null) {
            renderLayoutUnavailable();
            return;
        }
        ensureDescriptors();
        LayoutTemplate template = layoutApi.vertical()
                .spacing(6f)
                .child(slot -> slot.content(this::renderIntro))
                .child(slot -> slot.content(this::renderSelector))
                .child(slot -> slot.content(() -> renderDescriptorSection(parent)))
                .build();
        layoutApi.render(template);
    }

    private void renderIntro() {
        ImGui.text("Capture ImGui styles into descriptors, register them, then reuse per view or namespace.");
    }

    private void renderSelector() {
        if (ImGui.radioButton("Minimal preset", MINIMAL_STYLE.equals(selectedKey))) {
            selectedKey = MINIMAL_STYLE;
        }
        ImGui.sameLine();
        if (ImGui.radioButton("Accent preset", ACCENT_STYLE.equals(selectedKey))) {
            selectedKey = ACCENT_STYLE;
        }
        ImGui.checkbox("Apply to namespace dockspace", applyNamespaceWide);
    }

    private void renderDescriptorSection(View parent) {
        ImGui.separator();
        NamedStyleRegistry registry = NamedStyleRegistry.getInstance();
        registry.getDescriptor(selectedKey).ifPresent(descriptor -> renderDescriptorDetails(parent, descriptor));
    }

    private void renderDescriptorDetails(View parent, StyleDescriptor descriptor) {
        ImGui.text("Window padding: %.1f x %.1f".formatted(descriptor.getWindowPadding().x(), descriptor.getWindowPadding().y()));
        ImGui.text("Frame rounding: %.1f".formatted(descriptor.getFrameRounding()));
        ImGui.text("Grab min size: %.1f".formatted(descriptor.getGrabMinSize()));
        ImGui.separator();
        if (ImGui.button("Preview on this view")) {
            parent.useStyle(selectedKey);
        }
        ImGui.sameLine();
        if (ImGui.button("Reset view style")) {
            parent.useStyle((ResourceId) null);
        }
        ImGui.sameLine();
        if (ImGui.button("Apply to namespace")) {
            applyToNamespace(parent);
        }
        int tableFlags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp;
        if (ImGui.beginTable("style_breakdown", 2, tableFlags)) {
            ImGui.tableSetupColumn("Metric", ImGuiTableColumnFlags.WidthFixed, 140f);
            ImGui.tableSetupColumn("Value");
            renderMetricRow("Window rounding", "%.1f".formatted(descriptor.getWindowRounding()));
            renderMetricRow("Scrollbar size", "%.1f".formatted(descriptor.getScrollbarSize()));
            renderMetricRow("Item spacing", "%.1f x %.1f".formatted(descriptor.getItemSpacing().x(), descriptor.getItemSpacing().y()));
            renderMetricRow("Tab rounding", "%.1f".formatted(descriptor.getTabRounding()));
            renderMetricRow("Anti-aliased fill", Boolean.toString(descriptor.isAntiAliasedFill()));
            renderMetricRow("Palette entries", Integer.toString(descriptor.getColorPalette().getColors().size()));
            ImGui.endTable();
        }
    }

    private void applyToNamespace(View parent) {
        String namespace = parent.getNamespace();
        StyleManager manager = (namespace != null && !namespace.isBlank())
                ? StyleManager.get(namespace)
                : StyleManager.getInstance();
        if (manager == null) {
            return;
        }
        ResourceId key = applyNamespaceWide.get() ? selectedKey : null;
        manager.setGlobalStyleKey(key);
        manager.apply();
    }

    private void renderMetricRow(String label, String value) {
        ImGui.tableNextRow();
        ImGui.tableSetColumnIndex(0);
        ImGui.text(label);
        ImGui.tableSetColumnIndex(1);
        ImGui.text(value);
    }

    private void ensureDescriptors() {
        if (descriptorsRegistered) {
            return;
        }
        NamedStyleRegistry registry = NamedStyleRegistry.getInstance();
        registry.registerDescriptor(MINIMAL_STYLE, createMinimalDescriptor());
        registry.registerDescriptor(ACCENT_STYLE, createAccentDescriptor());
        descriptorsRegistered = true;
    }

    private StyleDescriptor createMinimalDescriptor() {
        ImGuiStyle baseStyle = ImGui.getStyle();
        StyleDescriptor.Builder builder = StyleDescriptor.builder().fromStyle(baseStyle);
        builder.windowPadding(10f, 8f);
        builder.windowRounding(6f);
        builder.framePadding(6f, 4f);
        builder.itemSpacing(8f, 4f);
        builder.tabRounding(4f);
        builder.colorPalette(ColorPalette.fromStyle(baseStyle));
        return builder.build();
    }

    private StyleDescriptor createAccentDescriptor() {
        ImGuiStyle baseStyle = ImGui.getStyle();
        ColorPalette basePalette = ColorPalette.fromStyle(baseStyle);
        ColorPalette.Builder overrides = ColorPalette.builder();
        overrides.set(ImGuiCol.Button, ImGui.getColorU32(0.2f, 0.55f, 0.95f, 1f));
        overrides.set(ImGuiCol.ButtonHovered, ImGui.getColorU32(0.2f, 0.65f, 0.98f, 1f));
        overrides.set(ImGuiCol.ButtonActive, ImGui.getColorU32(0.18f, 0.45f, 0.9f, 1f));
        overrides.set(ImGuiCol.FrameBg, ImGui.getColorU32(0.1f, 0.14f, 0.2f, 0.9f));
        ColorPalette palette = basePalette.mergedWith(overrides.build());
        StyleDescriptor.Builder builder = StyleDescriptor.builder().fromStyle(baseStyle);
        builder.windowPadding(14f, 12f);
        builder.frameRounding(5f);
        builder.grabMinSize(16f);
        builder.grabRounding(6f);
        builder.tabRounding(5f);
        builder.scrollbarRounding(12f);
        builder.colorPalette(palette);
        return builder.build();
    }

    private void renderLayoutUnavailable() {
        ImGui.text("Layout service unavailable");
        ImGui.textWrapped("Attach MineGui Playground to a namespace to evaluate style workflows.");
    }
}
