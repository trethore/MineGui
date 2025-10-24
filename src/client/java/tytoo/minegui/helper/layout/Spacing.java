package tytoo.minegui.helper.layout;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.style.MGVec2;

public final class Spacing {
    private Spacing() {
    }

    public static StyleHandle stack(float x, float y) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, sanitize(x), sanitize(y));
        return new StyleScope(1);
    }

    public static StyleHandle stack(float x, float y, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        return stack(chosen.applyWidth(x), chosen.applyHeight(y));
    }

    public static StyleHandle stack(MGStyleDescriptor descriptor) {
        if (descriptor == null) {
            return new StyleScope(0);
        }
        MGVec2 spacing = descriptor.getItemSpacing();
        return stack(spacing.x(), spacing.y());
    }

    public static StyleHandle inner(float x, float y) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemInnerSpacing, sanitize(x), sanitize(y));
        return new StyleScope(1);
    }

    public static StyleHandle inner(float x, float y, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        return inner(chosen.applyWidth(x), chosen.applyHeight(y));
    }

    public static StyleHandle inner(MGStyleDescriptor descriptor) {
        if (descriptor == null) {
            return new StyleScope(0);
        }
        MGVec2 spacing = descriptor.getItemInnerSpacing();
        return inner(spacing.x(), spacing.y());
    }

    private static float sanitize(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, value);
    }
}
