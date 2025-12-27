package tytoo.minegui.imgui.scope;

import imgui.ImGui;

public final class DisabledScope implements AutoCloseable {
    private final boolean wasDisabled;

    private DisabledScope(boolean disabled) {
        this.wasDisabled = disabled;
        if (disabled) {
            ImGui.beginDisabled();
        }
    }

    public static DisabledScope of() {
        return new DisabledScope(true);
    }

    public static DisabledScope when(boolean condition) {
        return new DisabledScope(condition);
    }

    @Override
    public void close() {
        if (wasDisabled) {
            ImGui.endDisabled();
        }
    }
}
