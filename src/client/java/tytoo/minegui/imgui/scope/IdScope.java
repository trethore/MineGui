package tytoo.minegui.imgui.scope;

import imgui.ImGui;

public final class IdScope implements AutoCloseable {

    private IdScope() {
    }

    public static IdScope of(String id) {
        ImGui.pushID(id);
        return new IdScope();
    }

    public static IdScope of(int id) {
        ImGui.pushID(id);
        return new IdScope();
    }

    @Override
    public void close() {
        ImGui.popID();
    }
}
