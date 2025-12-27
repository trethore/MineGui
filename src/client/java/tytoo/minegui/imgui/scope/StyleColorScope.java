package tytoo.minegui.imgui.scope;

import imgui.ImGui;

public final class StyleColorScope implements AutoCloseable {
    private final int count;

    private StyleColorScope(int count) {
        this.count = count;
    }

    public static StyleColorScope of(int idx, int color) {
        ImGui.pushStyleColor(idx, color);
        return new StyleColorScope(1);
    }

    public static StyleColorScope of(int idx, float r, float g, float b, float a) {
        ImGui.pushStyleColor(idx, r, g, b, a);
        return new StyleColorScope(1);
    }

    public StyleColorScope and(int idx, int color) {
        ImGui.pushStyleColor(idx, color);
        return new StyleColorScope(count + 1);
    }

    public StyleColorScope and(int idx, float r, float g, float b, float a) {
        ImGui.pushStyleColor(idx, r, g, b, a);
        return new StyleColorScope(count + 1);
    }

    @Override
    public void close() {
        ImGui.popStyleColor(count);
    }
}
