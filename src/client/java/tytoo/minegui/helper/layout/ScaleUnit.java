package tytoo.minegui.helper.layout;

import imgui.ImGui;

public enum ScaleUnit {
    RAW {
        @Override
        float resolveX(float value) {
            return sanitize(value);
        }

        @Override
        float resolveY(float value) {
            return sanitize(value);
        }
    },
    SCALED {
        @Override
        float resolveX(float value) {
            return sanitize(value) * scaleX();
        }

        @Override
        float resolveY(float value) {
            return sanitize(value) * scaleY();
        }
    };

    private static float sanitize(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return value;
    }

    private static float scaleX() {
        return Math.max(1e-6f, ImGui.getIO().getDisplayFramebufferScaleX());
    }

    private static float scaleY() {
        return Math.max(1e-6f, ImGui.getIO().getDisplayFramebufferScaleY());
    }

    public float applyWidth(float value) {
        return Math.max(0f, resolveX(value));
    }

    public float applyHeight(float value) {
        return Math.max(0f, resolveY(value));
    }

    abstract float resolveX(float value);

    abstract float resolveY(float value);
}
