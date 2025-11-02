package tytoo.minegui.helper.layout;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import tytoo.minegui.helper.layout.scope.StyleHandle;
import tytoo.minegui.helper.layout.scope.StyleScope;
import tytoo.minegui.helper.layout.sizing.ScaleUnit;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.Vec2;

public final class Padding {
    private Padding() {
    }

    public static StyleHandle window(float x, float y) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, sanitize(x), sanitize(y));
        return new StyleScope(1);
    }

    public static StyleHandle window(float x, float y, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        return window(chosen.applyWidth(x), chosen.applyHeight(y));
    }

    public static StyleHandle window(StyleDescriptor descriptor) {
        if (descriptor == null) {
            return new StyleScope(0);
        }
        Vec2 padding = descriptor.getWindowPadding();
        return window(padding.x(), padding.y());
    }

    public static StyleHandle frame(float x, float y) {
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, sanitize(x), sanitize(y));
        return new StyleScope(1);
    }

    public static StyleHandle frame(float x, float y, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        return frame(chosen.applyWidth(x), chosen.applyHeight(y));
    }

    public static StyleHandle frame(StyleDescriptor descriptor) {
        if (descriptor == null) {
            return new StyleScope(0);
        }
        Vec2 padding = descriptor.getFramePadding();
        return frame(padding.x(), padding.y());
    }

    public static StyleHandle cell(float x, float y) {
        ImGui.pushStyleVar(ImGuiStyleVar.CellPadding, sanitize(x), sanitize(y));
        return new StyleScope(1);
    }

    public static StyleHandle cell(float x, float y, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        return cell(chosen.applyWidth(x), chosen.applyHeight(y));
    }

    public static StyleHandle cell(StyleDescriptor descriptor) {
        if (descriptor == null) {
            return new StyleScope(0);
        }
        Vec2 padding = descriptor.getCellPadding();
        return cell(padding.x(), padding.y());
    }

    private static float sanitize(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, value);
    }
}
