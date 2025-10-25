package tytoo.minegui_debug.view;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.window.MGWindow;
import tytoo.minegui.view.MGView;

public final class TestView extends MGView {
    private final ImString nameValue = new ImString("Alex", 64);
    private final ImString emailValue = new ImString("alex@example.com", 96);
    private final ImString notesValue = new ImString("Collect layout feedback here.", 512);
    private boolean clearFocusOnOpen;
    private String lastAction = "Awaiting input";

    public TestView() {
        super("minegui_debug:test_view", true);
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
                    if (clearFocusOnOpen) {
                        ImGui.setWindowFocus(null);
                        clearFocusOnOpen = false;
                    }

                    ImGui.text("Vertical stack layout");
                    ImGui.separator();

                    try (VStack layout = VStack.begin(new VStack.Options().spacing(12f).fillMode(VStack.FillMode.MATCH_WIDEST))) {
                        try (VStack.ItemScope intro = layout.next()) {
                            ImGui.textWrapped("VStack scopes arrange content vertically with consistent spacing and optional width matching.");
                        }

                        try (VStack.ItemScope nameRow = layout.next()) {
                            float labelWidth = 90f;
                            float fieldWidth = 220f;
                            float fieldHeight = ImGui.getFrameHeight();
                            try (HStack row = HStack.begin(new HStack.Options().spacing(10f).alignment(HStack.Alignment.CENTER))) {
                                try (HStack.ItemScope label = row.next(new HStack.ItemRequest().estimateWidth(labelWidth).estimateHeight(fieldHeight))) {
                                    ImGui.alignTextToFramePadding();
                                    ImGui.text("Name");
                                }
                                try (HStack.ItemScope field = row.next(new HStack.ItemRequest().estimateWidth(fieldWidth).estimateHeight(fieldHeight))) {
                                    SizeHints.itemWidth(fieldWidth);
                                    ImGui.inputText("##vstack_name", nameValue);
                                }
                            }
                        }

                        try (VStack.ItemScope emailRow = layout.next()) {
                            float labelWidth = 90f;
                            float fieldWidth = 220f;
                            float fieldHeight = ImGui.getFrameHeight();
                            try (HStack row = HStack.begin(new HStack.Options().spacing(10f).alignment(HStack.Alignment.CENTER))) {
                                try (HStack.ItemScope label = row.next(new HStack.ItemRequest().estimateWidth(labelWidth).estimateHeight(fieldHeight))) {
                                    ImGui.alignTextToFramePadding();
                                    ImGui.text("Email");
                                }
                                try (HStack.ItemScope field = row.next(new HStack.ItemRequest().estimateWidth(fieldWidth).estimateHeight(fieldHeight))) {
                                    SizeHints.itemWidth(fieldWidth);
                                    ImGui.inputText("##vstack_email", emailValue);
                                }
                            }
                        }

                        try (VStack.ItemScope notesBlock = layout.next(new VStack.ItemRequest().estimateHeight(140f))) {
                            ImGui.text("Notes");
                            ImGui.separator();
                            ImGui.inputTextMultiline("##vstack_notes", notesValue, -1f, 110f);
                        }

                        float buttonHeight = ImGui.getFrameHeight();
                        try (VStack.ItemScope actionRow = layout.next(new VStack.ItemRequest().estimateHeight(buttonHeight))) {
                            float buttonWidth = 120f;
                            try (HStack row = HStack.begin(new HStack.Options().spacing(10f).alignment(HStack.Alignment.CENTER))) {
                                try (HStack.ItemScope submit = row.next(new HStack.ItemRequest().estimateWidth(buttonWidth).estimateHeight(buttonHeight))) {
                                    if (ImGui.button("Submit", buttonWidth, buttonHeight)) {
                                        lastAction = "Submit clicked";
                                    }
                                }
                                try (HStack.ItemScope reset = row.next(new HStack.ItemRequest().estimateWidth(buttonWidth).estimateHeight(buttonHeight))) {
                                    if (ImGui.button("Reset", buttonWidth, buttonHeight)) {
                                        nameValue.set("");
                                        emailValue.set("");
                                        notesValue.set("");
                                        lastAction = "Fields cleared";
                                    }
                                }
                            }
                        }

                        try (VStack.ItemScope status = layout.next()) {
                            ImGui.text("Last action: " + lastAction);
                        }
                    }
                });
    }
}
