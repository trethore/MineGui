package tytoo.minegui_debug.view;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.window.MGWindow;
import tytoo.minegui.style.MGFontLibrary;
import tytoo.minegui.util.ImGuiImageUtils;
import tytoo.minegui.view.MGView;
import tytoo.minegui.view.cursor.MGCursorPolicies;

public final class TestView extends MGView {
    private static final Identifier IMGUI_ICON = Identifier.of("minegui", "icon.png");
    private static final Identifier JETBRAINS_MONO_KEY = Identifier.of(MineGuiCore.ID, "jetbrains-mono");
    private static final float JETBRAINS_MONO_SIZE = 18.0f;
    private final ImString nameValue = new ImString("Alex", 64);
    private final ImString emailValue = new ImString("alex@example.com", 96);
    private final ImString notesValue = new ImString("Collect layout feedback here.", 512);
    private boolean clearFocusOnOpen;
    private String lastAction = "Awaiting input";
    private ImFont jetbrainsMono;
    private String jetbrainsStatus = "JetBrains Mono pending";

    public TestView() {
        super("minegui_debug:test_view", true);
        registerJetbrainsMono();
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

                        try (VStack.ItemScope fontRow = layout.next()) {
                            ensureJetbrainsMono();
                            ImGui.text(jetbrainsStatus);
                            if (jetbrainsMono != null) {
                                ImGui.pushFont(jetbrainsMono);
                                ImGui.text("JetBrains Mono sample -- 0123456789 ABC xyz");
                                ImGui.popFont();
                            }
                        }

                        try (VStack.ItemScope iconRow = layout.next()) {
                            ImGui.text("ImGui icon preview");
                            ImGui.spacing();
                            float cursorX = ImGui.getCursorScreenPosX();
                            float cursorY = ImGui.getCursorScreenPosY();
                            ImGuiImageUtils.TextureInfo info = ImGuiImageUtils.getTextureInfo(IMGUI_ICON);
                            float size = 64f;
                            ImGuiImageUtils.drawImage(IMGUI_ICON, cursorX, cursorY, cursorX + size, cursorY + size, 0, false, 0xFFFFFFFF);
                            ImGui.dummy(size, size);
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

    private void ensureJetbrainsMono() {
        if (jetbrainsMono != null) {
            jetbrainsStatus = "JetBrains Mono ready";
            return;
        }
        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        ImFont resolved = fontLibrary.ensureFont(JETBRAINS_MONO_KEY, JETBRAINS_MONO_SIZE);
        if (resolved != null) {
            jetbrainsMono = resolved;
            jetbrainsStatus = "JetBrains Mono ready";
        } else if (fontLibrary.isRegistrationLocked()) {
            jetbrainsStatus = "JetBrains Mono unavailable; restart after registering fonts.";
        } else {
            jetbrainsStatus = "JetBrains Mono loading...";
        }
    }

    private void registerJetbrainsMono() {
        MGFontLibrary fontLibrary = MGFontLibrary.getInstance();
        fontLibrary.registerFont(
                JETBRAINS_MONO_KEY,
                new MGFontLibrary.FontDescriptor(
                        MGFontLibrary.FontSource.asset("jetbrains-mono.ttf"),
                        JETBRAINS_MONO_SIZE,
                        null
                )
        );
    }
}
