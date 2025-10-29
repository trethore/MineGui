package tytoo.minegui_debug.view;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.util.Identifier;
import tytoo.minegui.helper.UI;
import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.window.MGWindow;
import tytoo.minegui.style.MGFontLibrary;
import tytoo.minegui.style.MGFonts;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.MGView;
import tytoo.minegui.view.cursor.MGCursorPolicies;

public final class TestView extends MGView {
    private static final Identifier IMGUI_ICON = Identifier.of("minegui", "icon.png");
    private static final float LABEL_WIDTH = 90f;
    private static final float FIELD_WIDTH = 220f;
    private static final float BUTTON_WIDTH = 120f;
    private final ImString nameValue = new ImString("Alex", 64);
    private final ImString emailValue = new ImString("alex@example.com", 96);
    private final ImString notesValue = new ImString("Collect layout feedback here.", 512);
    private boolean clearFocusOnOpen;
    private String lastAction = "Awaiting input";
    private ImFont jetbrainsMono;
    private String jetbrainsStatus = "JetBrains Mono pending";

    public TestView() {
        super("minegui_debug:test_view", true);
        setCursorPolicy(MGCursorPolicies.clickToLock());
    }

    @Override
    protected void onOpen() {
        clearFocusOnOpen = true;
    }

    @Override
    protected void renderView() {
        MGWindow.of(this, "VStack Demo")
                .flags(ImGuiWindowFlags.NoFocusOnAppearing)
                .render(() -> {
                    resetFocusIfPending();
                    UI.withVStack(new VStack.Options().spacing(12f).fillMode(VStack.FillMode.MATCH_WIDEST), layout -> {
                        renderIntro(layout);
                        renderJetbrainsPreview(layout);
                        renderIconPreview(layout);
                        renderInput(layout, "Name", nameValue, "##vstack_name");
                        renderInput(layout, "Email", emailValue, "##vstack_email");
                        renderNotes(layout);
                        renderActions(layout);
                        renderStatus(layout);
                    });
                });
    }

    private void ensureJetbrainsMono() {
        if (jetbrainsMono != null) {
            jetbrainsStatus = "JetBrains Mono ready";
            return;
        }
        ImFont resolved = MGFonts.ensureJetbrainsMono();
        if (resolved != null) {
            jetbrainsMono = resolved;
            jetbrainsStatus = "JetBrains Mono ready";
        } else if (MGFontLibrary.getInstance().isRegistrationLocked()) {
            jetbrainsStatus = "JetBrains Mono unavailable; restart after registering fonts.";
        } else {
            jetbrainsStatus = "JetBrains Mono loading...";
        }
    }

    private void resetFocusIfPending() {
        if (!clearFocusOnOpen) {
            return;
        }
        ImGui.setWindowFocus(null);
        clearFocusOnOpen = false;
    }

    private void renderIntro(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ImGui.text("Vertical stack layout");
            ImGui.separator();
            ImGui.textWrapped("VStack scopes arrange content vertically with consistent spacing and optional width matching.");
        });
    }

    private void renderJetbrainsPreview(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ensureJetbrainsMono();
            ImGui.text(jetbrainsStatus);
            if (jetbrainsMono != null) {
                ImGui.pushFont(jetbrainsMono);
                ImGui.text("JetBrains Mono sample -- 0123456789 ABC xyz");
                ImGui.popFont();
            }
        });
    }

    private void renderIconPreview(VStack layout) {
        UI.withVStackItem(layout, () -> {
            ImGui.text("ImGui icon preview");
            ImGui.spacing();
            float cursorX = ImGui.getCursorScreenPosX();
            float cursorY = ImGui.getCursorScreenPosY();
            float size = 64f;
            ImGuiImageUtils.drawImage(IMGUI_ICON, cursorX, cursorY, cursorX + size, cursorY + size, 0, false, 0xFFFFFFFF);
            ImGui.dummy(size, size);
        });
    }

    private void renderInput(VStack layout, String label, ImString value, String id) {
        UI.withVStackItem(layout, () -> {
            float fieldHeight = ImGui.getFrameHeight();
            UI.withHStack(new HStack.Options().spacing(10f).alignment(HStack.Alignment.CENTER), row -> {
                UI.withHItem(row, new HStack.ItemRequest().estimateWidth(LABEL_WIDTH).estimateHeight(fieldHeight), () -> {
                    ImGui.alignTextToFramePadding();
                    ImGui.text(label);
                });
                UI.withHItem(row, new HStack.ItemRequest().estimateWidth(FIELD_WIDTH).estimateHeight(fieldHeight), () -> {
                    SizeHints.itemWidth(FIELD_WIDTH);
                    ImGui.inputText(id, value);
                });
            });
        });
    }

    private void renderNotes(VStack layout) {
        UI.withVStackItem(layout, new VStack.ItemRequest().estimateHeight(140f), () -> {
            ImGui.text("Notes");
            ImGui.separator();
            ImGui.inputTextMultiline("##vstack_notes", notesValue, -1f, 110f);
        });
    }

    private void renderActions(VStack layout) {
        float buttonHeight = ImGui.getFrameHeight();
        UI.withVStackItem(layout, new VStack.ItemRequest().estimateHeight(buttonHeight), () -> UI.withHStack(new HStack.Options().spacing(10f).alignment(HStack.Alignment.CENTER), row -> {
            UI.withHItem(row, new HStack.ItemRequest().estimateWidth(BUTTON_WIDTH).estimateHeight(buttonHeight), () -> {
                if (ImGui.button("Submit", BUTTON_WIDTH, buttonHeight)) {
                    lastAction = "Submit clicked";
                }
            });
            UI.withHItem(row, new HStack.ItemRequest().estimateWidth(BUTTON_WIDTH).estimateHeight(buttonHeight), () -> {
                if (ImGui.button("Reset", BUTTON_WIDTH, buttonHeight)) {
                    resetFields();
                    lastAction = "Fields cleared";
                }
            });
        }));
    }

    private void renderStatus(VStack layout) {
        UI.withVStackItem(layout, () -> ImGui.text("Last action: " + lastAction));
    }

    private void resetFields() {
        nameValue.set("");
        emailValue.set("");
        notesValue.set("");
    }
}
