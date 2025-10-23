package tytoo.minegui_debug.view;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.MGView;
import tytoo.minegui_debug.MineGuiDebugCore;

public final class TestView extends MGView {
    private static final Identifier ICON = Identifier.of(MineGuiCore.ID, "icon.png");
    private final ImString textValue = new ImString("", 256);
    private boolean clearFocusOnOpen;

    public TestView() {
        super("minegui_debug:test_view", true);
    }

    @Override
    protected void onOpen() {
        clearFocusOnOpen = true;
    }

    @Override
    protected void renderView() {
        if (!ImGui.begin(scopedWindowTitle("TestWindow"), ImGuiWindowFlags.NoFocusOnAppearing)) {
            ImGui.end();
            return;
        }

        if (clearFocusOnOpen) {
            ImGui.setWindowFocus(null);
            clearFocusOnOpen = false;
        }

        ImGui.text("Immediate-mode debug view");
        ImGui.separator();
        ImGui.inputText("Text value", textValue);
        ImGui.spacing();

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
