package tytoo.minegui.util;

import imgui.ImGui;
import net.minecraft.client.util.Window;

import java.util.ArrayDeque;
import java.util.Deque;

@SuppressWarnings("unused")
public final class ImGuiUtils {
    private static final ImGui IM_GUI_INSTANCE = new ImGui();
    private static final ThreadLocal<Deque<Float>> WINDOW_FONT_SCALE_STACK =
            ThreadLocal.withInitial(() -> {
                Deque<Float> stack = new ArrayDeque<>();
                stack.push(1.0f);
                return stack;
            });

    private ImGuiUtils() {
    }

    public static double mouseX() {
        return ImGui.getIO().getMousePosX();
    }

    public static double mouseY() {
        return ImGui.getIO().getMousePosY();
    }

    public static double toImGuiX(Window window, double mcX) {
        double scale = window != null ? window.getScaleFactor() : ImGui.getIO().getDisplayFramebufferScaleX();
        return mcX / Math.max(1e-6f, scale);
    }

    public static double toImGuiY(Window window, double mcY) {
        double scale = window != null ? window.getScaleFactor() : ImGui.getIO().getDisplayFramebufferScaleY();
        return mcY / Math.max(1e-6f, scale);
    }

    public static void pushWindowFontScale(float scaleMultiplier) {
        Deque<Float> stack = WINDOW_FONT_SCALE_STACK.get();
        Float currentValue = stack.peek();
        float current = currentValue != null ? currentValue : 1.0f;
        float target = current * scaleMultiplier;
        stack.push(target);
        IM_GUI_INSTANCE.setWindowFontScale(target);
    }

    public static void popWindowFontScale() {
        Deque<Float> stack = WINDOW_FONT_SCALE_STACK.get();
        if (stack.size() <= 1) {
            IM_GUI_INSTANCE.setWindowFontScale(1.0f);
            return;
        }
        stack.pop();
        Float restoredValue = stack.peek();
        float restored = restoredValue != null ? restoredValue : 1.0f;
        IM_GUI_INSTANCE.setWindowFontScale(restored);
    }
}
