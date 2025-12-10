package tytoo.mineguidebug.view.sections;

import imgui.ImGui;
import tytoo.minegui.view.View;

public final class LayoutShowcaseSection implements PlaygroundSection {

    @Override
    public String tabLabel() {
        return "Layouts";
    }

    @Override
    public void render(View parent) {
        ImGui.text("Layout Engine Removed");
        ImGui.separator();
        ImGui.textWrapped("The experimental constraint-based layout engine (VStack, HStack, GridLayout) has been removed in favor of standard ImGui immediate mode layouting.");
        ImGui.textWrapped("Use standard ImGui functions like ImGui.beginChild(), ImGui.sameLine(), ImGui.dummy() etc. to build your layouts.");
    }
}
