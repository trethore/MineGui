package tytoo.minegui_debug.view;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.helper.constraint.constraints.Constraints;
import tytoo.minegui.helper.layout.*;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.MGView;
import tytoo.minegui_debug.MineGuiDebugCore;

public final class TestView extends MGView {
    private static final Identifier ICON = Identifier.of(MineGuiCore.ID, "icon.png");
    private final ImString textValue = new ImString("", 256);
    private final ImString widthDemoValue = new ImString("Scaled width", 128);
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
        SizeHints.windowSize(420f, 260f);
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
        SizeHints.itemWidth(220f, ScaleUnit.SCALED);
        ImGui.inputText("Scaled width input", widthDemoValue);
        ImGui.spacing();

        ImGuiImageUtils.TextureInfo info = ImGuiImageUtils.getTextureInfo(ICON);
        float width = Math.min(128.0f, info.width());
        float height = Math.min(128.0f, info.height());
        ImGui.image(info.textureId(), width, height);

        ImGui.spacing();
        if (ImGui.button("Log click")) {
            MineGuiDebugCore.LOGGER.info("Clicked MineGui icon button");
        }

        ImGui.separator();
        ImGui.text("Layout cursor demo");
        ImGui.setNextItemWidth(-1f);
        if (ImGui.beginChild("MineGuiLayoutDemo", 0f, 180f, true, ImGuiWindowFlags.NoScrollbar)) {
            LayoutContext layoutContext = LayoutContext.capture();

            LayoutCursor.moveTo(LayoutConstraints.builder().rawX(16f).rawY(32f).build(), layoutContext);
            ImGui.text("Raw move to (16, 32)");

            Constraints centeredConstraints = new Constraints(layoutContext.constraintTarget());
            centeredConstraints.setX(Constraints.relative(0.5f, -60f));
            centeredConstraints.setY(Constraints.relative(0.5f, -15f));
            LayoutCursor.moveTo(
                    LayoutConstraints.builder()
                            .constraints(centeredConstraints)
                            .width(120f)
                            .height(30f)
                            .build(),
                    layoutContext
            );
            ImGui.button("Centered button", 120f, 30f);

            LayoutCursor.moveBy(0f, 40f);
            ImGui.text("Offset by moveBy(0, 40)");

            ImGui.separator();
            ImGui.text("Size hints");
            Constraints halfWidthConstraints = new Constraints(layoutContext.constraintTarget());
            halfWidthConstraints.setWidth(Constraints.relative(0.5f, -20f));
            LayoutConstraints sizeRequest = LayoutConstraints.builder()
                    .constraints(halfWidthConstraints)
                    .height(36f)
                    .build();
            SizeRange widthRange = SizeRange.of(120f, 260f);
            SizeHints.NextSize buttonSize = SizeHints.itemSize(sizeRequest, widthRange, null, layoutContext);
            float buttonHeight = buttonSize.height() > 0f ? buttonSize.height() : 36f;
            ImGui.button("Half width button", buttonSize.width(), buttonHeight);
            ImGui.text("Computed width: " + buttonSize.width());

            ImGui.separator();
            ImGui.text("Spacing and padding");
            try (StyleHandle spacing = Spacing.stack(12f, 8f)) {
                try (StyleHandle padding = Padding.frame(6f, 6f)) {
                    ImGui.text("Scoped spacing 12x8 with frame padding 6x6");
                }
                ImGui.text("Spacing restored after padding scope");
            }

            try (Margin.Scope margin = Margin.apply(4f, 12f, 10f, 12f)) {
                ImGui.text("Block with margin top 4, right 12, bottom 10, left 12");
            }
        }
        ImGui.endChild();

        ImGui.end();
    }
}
