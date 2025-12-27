package tytoo.minegui.imgui;

import imgui.ImGui;
import imgui.internal.ImGuiContext;
import tytoo.minegui.MineGuiCore;

import java.util.Optional;
import java.util.function.Supplier;

public final class ContextGuard {

    private ContextGuard() {
    }

    public static boolean isReady() {
        if (!MineGuiCore.isInitialized()) {
            return false;
        }
        return hasValidContext();
    }

    public static boolean hasValidContext() {
        ImGuiContext context = ImGui.getCurrentContext();
        return context != null && !context.isNotValidPtr();
    }

    public static void ifReady(Runnable action) {
        if (isReady()) {
            action.run();
        }
    }

    public static <T> Optional<T> whenReady(Supplier<T> supplier) {
        if (isReady()) {
            return Optional.ofNullable(supplier.get());
        }
        return Optional.empty();
    }
}
