package tytoo.minegui.imgui.ref;

import imgui.ImGui;

public final class FloatRef {
    private float value;
    private final float[] holder = new float[1];

    public FloatRef(float initial) {
        this.value = initial;
        this.holder[0] = initial;
    }

    public float get() {
        return value;
    }

    public void set(float value) {
        this.value = value;
        this.holder[0] = value;
    }

    public float[] asArray() {
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

    public boolean sliderFloat(String label, float min, float max) {
        return sliderFloat(label, min, max, "%.3f");
    }

    public boolean sliderFloat(String label, float min, float max, String format) {
        holder[0] = value;
        if (ImGui.sliderFloat(label, holder, min, max, format)) {
            value = holder[0];
            return true;
        }
        return false;
    }

    public boolean dragFloat(String label, float speed, float min, float max) {
        return dragFloat(label, speed, min, max, "%.3f");
    }

    public boolean dragFloat(String label, float speed, float min, float max, String format) {
        holder[0] = value;
        if (ImGui.dragFloat(label, holder, speed, min, max, format)) {
            value = holder[0];
            return true;
        }
        return false;
    }
}
