package tytoo.minegui.imgui.ref;

import imgui.ImGui;

public final class IntRef {
    private int value;
    private final int[] holder = new int[1];

    public IntRef(int initial) {
        this.value = initial;
        this.holder[0] = initial;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
        this.holder[0] = value;
    }

    public int[] asArray() {
        holder[0] = value;
        return holder;
    }

    public boolean sync() {
        if (holder[0] != value) {
            value = holder[0];
            return true;
        }
        return false;
    }

    public boolean sliderInt(String label, int min, int max) {
        return sliderInt(label, min, max, "%d");
    }

    public boolean sliderInt(String label, int min, int max, String format) {
        holder[0] = value;
        if (ImGui.sliderInt(label, holder, min, max, format)) {
            value = holder[0];
            return true;
        }
        return false;
    }

    public boolean dragInt(String label, float speed, int min, int max) {
        return dragInt(label, speed, min, max, "%d");
    }

    public boolean dragInt(String label, float speed, int min, int max, String format) {
        holder[0] = value;
        if (ImGui.dragInt(label, holder, speed, min, max, format)) {
            value = holder[0];
            return true;
        }
        return false;
    }
}
