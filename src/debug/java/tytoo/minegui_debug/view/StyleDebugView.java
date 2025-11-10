package tytoo.minegui_debug.view;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.style.NamedStyleRegistry;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StyleDebugView extends View {
    private ResourceId selectedKey;

    public StyleDebugView() {
        setId("minegui/style_debug");
        setPersistent(false);
    }

    @Override
    protected void renderView() {
        if (selectedKey == null) {
            selectedKey = ResourceId.of(MineGuiCore.ID, "default");
        }
        ImGui.setNextWindowSize(360.0f, 320.0f, ImGuiCond.FirstUseEver);
        if (!ImGui.begin(scopedWindowTitle("Style Inspector"), ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.end();
            return;
        }

        NamedStyleRegistry registry = NamedStyleRegistry.getInstance();
        List<ResourceId> keys = new ArrayList<>(registry.keys());
        keys.sort(Comparator.comparing(ResourceId::toString));
        StyleManager manager = resolveStyleManager();
        ResourceId globalKey = manager.getGlobalStyleKey();

        if (keys.isEmpty()) {
            ImGui.text("No registered styles.");
            ImGui.end();
            return;
        }

        ImGui.text("Registered Styles");
        ImGui.separator();
        for (ResourceId key : keys) {
            boolean selected = key.equals(selectedKey);
            if (ImGui.selectable(key.toString(), selected)) {
                selectedKey = key;
            }
            if (key.equals(globalKey)) {
                ImGui.sameLine();
                ImGui.textDisabled("(global)");
            }
        }

        if (selectedKey != null) {
            ImGui.separator();
            if (ImGui.button("Preview View")) {
                setStyleKey(selectedKey);
            }
            ImGui.sameLine();
            if (ImGui.button("Apply Global")) {
                applyGlobal(manager, selectedKey);
            }
            ImGui.sameLine();
            if (ImGui.button("Reset Global")) {
                applyGlobal(manager, null);
            }

            registry.getDescriptor(selectedKey).ifPresent(this::renderDescriptorDetails);
        }

        ImGui.end();
    }

    private void applyGlobal(StyleManager manager, ResourceId key) {
        if (manager == null) {
            return;
        }
        String namespace = getNamespace();
        if (namespace != null && !namespace.isBlank()) {
            UIManager uiManager = UIManager.get(namespace);
            for (View view : uiManager.getViews()) {
                view.setStyleKey(key);
            }
        } else {
            manager.setGlobalStyleKey(key);
            manager.apply();
        }
    }
    private StyleManager resolveStyleManager() {
        String namespace = getNamespace();
        if (namespace == null || namespace.isBlank()) {
            return StyleManager.getInstance();
        }
        return StyleManager.get(namespace);
    }

    private void renderDescriptorDetails(StyleDescriptor descriptor) {
        ImGui.separator();
        ImGui.text("Metrics");
        ImGui.text("Window Padding: %.2f x %.2f".formatted(descriptor.getWindowPadding().x(), descriptor.getWindowPadding().y()));
        ImGui.text("Frame Padding: %.2f x %.2f".formatted(descriptor.getFramePadding().x(), descriptor.getFramePadding().y()));
        ImGui.text("Item Spacing: %.2f x %.2f".formatted(descriptor.getItemSpacing().x(), descriptor.getItemSpacing().y()));
        ImGui.text("Window Rounding: %.2f".formatted(descriptor.getWindowRounding()));
        ImGui.text("Frame Rounding: %.2f".formatted(descriptor.getFrameRounding()));
        if (descriptor.getFontKey() != null) {
            ImGui.text("Font: %s (%.1f pt)".formatted(descriptor.getFontKey(), descriptor.getFontSize() != null ? descriptor.getFontSize() : 0.0f));
        }
    }
}
