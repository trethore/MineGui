package tytoo.minegui.imgui.scope;

import imgui.ImGui;

public final class StyleVarScope implements AutoCloseable {
    private final int count;

    private StyleVarScope(int count) {
        this.count = count;
    }

    public static StyleVarScope of(int idx, float val) {
        ImGui.pushStyleVar(idx, val);
        return new StyleVarScope(1);
    }

    public static StyleVarScope of(int idx, float x, float y) {
        ImGui.pushStyleVar(idx, x, y);
        return new StyleVarScope(1);
    }

    public StyleVarScope and(int idx, float val) {
        ImGui.pushStyleVar(idx, val);
        return new StyleVarScope(count + 1);
    }

    public StyleVarScope and(int idx, float x, float y) {
        ImGui.pushStyleVar(idx, x, y);
        return new StyleVarScope(count + 1);
    }

    @Override
    public void close() {
        ImGui.popStyleVar(count);
    }
}
