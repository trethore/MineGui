package tytoo.minegui_debug.view;

import imgui.ImGui;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.MGView;
import tytoo.minegui_debug.MineGuiDebugCore;

public final class TestView extends MGView {
    private static final Identifier ICON = Identifier.of(MineGuiCore.ID, "icon.png");

    public TestView() {
        hide();
    }

    @Override
    protected void renderView() {
        if (!ImGui.begin("MineGui Icon")) {
            ImGui.end();
            return;
        }

        ImGui.text("Immediate-mode debug view");
        ImGui.separator();

        ImGuiImageUtils.TextureInfo info = ImGuiImageUtils.getTextureInfo(ICON);
        float width = Math.min(128.0f, info.width());
        float height = Math.min(128.0f, info.height());
        ImGui.image(info.textureId(), width, height);

        ImGui.spacing();
        if (ImGui.button("Log click")) {
            MineGuiDebugCore.LOGGER.info("Clicked MineGui icon button");
        }

        ImGui.end();
    }
}
