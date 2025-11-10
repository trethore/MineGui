package tytoo.minegui_debug.view.sections;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImBoolean;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.layout.LayoutApi;
import tytoo.minegui.layout.LayoutTemplate;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.util.ResourceId;
import tytoo.minegui.view.View;

public final class ResourcePreviewSection implements PlaygroundSection {
    private static final ResourceId IMGUI_ICON = ResourceId.of(MineGuiCore.ID, "icon.png");
    private static final float[] WHITE_TINT = new float[]{1f, 1f, 1f, 1f};
    private final ImBoolean showQuad = new ImBoolean(true);
    private final ImBoolean showOutline = new ImBoolean(true);
    private final ImBoolean enableTint = new ImBoolean(false);
    private final float[] tint = new float[]{0.2f, 0.7f, 1f, 1f};
    private float previewSize = 128f;
    private float rotationSteps;
    private String resourceStatus = "Texture metadata populates after the first draw call.";

    @Override
    public String tabLabel() {
        return "Resources";
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
                .child(slot -> slot.height(previewSize + 40f).fillWidth(true).content(this::renderPreview))
                .child(slot -> slot.content(this::renderStatusLine))
                .build();
        layoutApi.render(template);
    }

    private void renderIntro() {
        ImGui.text("ImGuiImageUtils bridges Minecraft textures directly into ImGui draw lists.");
    }

    private void renderControls() {
        ImGui.checkbox("Render image quad", showQuad);
        ImGui.sameLine();
        ImGui.checkbox("Draw outline", showOutline);
        ImGui.checkbox("Apply tint", enableTint);
        if (enableTint.get()) {
            ImGui.colorEdit4("Tint rgba", tint);
        }
        float[] sizeHolder = {previewSize};
        if (ImGui.sliderFloat("Preview size", sizeHolder, 64f, 196f, "%.0f px")) {
            previewSize = sizeHolder[0];
        }
        float[] rotationHolder = {rotationSteps};
        if (ImGui.sliderFloat("Rotation", rotationHolder, 0f, 3f, "%.0f quarter turns")) {
            rotationSteps = rotationHolder[0];
        }
    }

    private void renderPreview() {
        if (!showQuad.get()) {
            ImGui.textDisabled("Enable the quad render to preview textures.");
            return;
        }
        renderImageQuad();
    }

    private void renderImageQuad() {
        float startX = ImGui.getCursorScreenPosX();
        float startY = ImGui.getCursorScreenPosY();
        float endX = startX + previewSize;
        float endY = startY + previewSize;
        int rotation = Math.round(rotationSteps) % 4;
        float[] appliedTint = enableTint.get() ? tint : WHITE_TINT;
        ImGuiImageUtils.drawImage(IMGUI_ICON, startX, startY, endX, endY, rotation, false, appliedTint);
        if (showOutline.get()) {
            ImDrawList drawList = ImGui.getWindowDrawList();
            drawList.addRect(startX - 2f, startY - 2f, endX + 2f, endY + 2f, ImGui.getColorU32(0.2f, 0.7f, 1f, 1f), 6f, 0, 2f);
            drawList.addText(startX, endY + 6f, ImGui.getColorU32(0.8f, 0.8f, 0.8f, 1f), "Custom draw commands stay in sync with ImGui.");
        }
        ImGui.dummy(previewSize, previewSize + 24f);
        ImGuiImageUtils.TextureInfo info = ImGuiImageUtils.getTextureInfo(IMGUI_ICON);
        resourceStatus = "GL id %d - %dx%d px".formatted(info.textureId(), info.width(), info.height());
    }

    private void renderStatusLine() {
        ImGui.text(resourceStatus);
    }

    private void renderLayoutUnavailable() {
        ImGui.text("Layout service unavailable");
        ImGui.textWrapped("Attach MineGui Playground to a namespace to explore resource previews.");
    }
}
