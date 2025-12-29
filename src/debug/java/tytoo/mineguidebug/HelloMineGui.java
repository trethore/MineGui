package tytoo.mineguidebug;

import imgui.ImGui;
import tytoo.minegui.MineGui;
import tytoo.minegui.runtime.MineGuiContext;

public final class HelloMineGui {

    private HelloMineGui() {
    }

    public static void run() {
        MineGuiContext context = MineGui.setupLite("hello_minegui");

        context.ui().registerRenderCallback(() -> {
            ImGui.begin("Hello MineGui");
            ImGui.text("Welcome to the immediate mode!");
            ImGui.text("Current time: " + System.currentTimeMillis());
            ImGui.end();
        });
    }
}
