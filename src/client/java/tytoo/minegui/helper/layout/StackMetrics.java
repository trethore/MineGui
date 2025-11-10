package tytoo.minegui.helper.layout;

final class StackMetrics {
    private StackMetrics() {
    }

    static float sanitizeLength(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, value);
    }

    static float sanitizeLength(Float value) {
        if (value == null) {
            return 0f;
        }
        return sanitizeLength(value.floatValue());
    }
}
