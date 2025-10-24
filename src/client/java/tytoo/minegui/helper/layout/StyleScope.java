package tytoo.minegui.helper.layout;

import imgui.ImGui;

final class StyleScope implements StyleHandle {
    private final int count;
    private boolean closed;

    StyleScope(int count) {
        this.count = count;
    }

    @Override
    public void close() {
        if (closed || count <= 0) {
            return;
        }
        closed = true;
        ImGui.popStyleVar(count);
    }
}
