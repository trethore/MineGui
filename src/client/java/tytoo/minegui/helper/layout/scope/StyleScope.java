package tytoo.minegui.helper.layout.scope;

import imgui.ImGui;

public final class StyleScope implements StyleHandle {
    private final int count;
    private boolean closed;

    public StyleScope(int count) {
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
