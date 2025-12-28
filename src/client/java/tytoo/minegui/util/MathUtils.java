package tytoo.minegui.util;

public final class MathUtils {

    private MathUtils() {
    }

    public static float clampFinite(float value, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return value;
    }

    public static float clampNonNegative(float value, float fallback) {
        if (!Float.isFinite(value) || value < 0.0f) {
            return fallback;
        }
        return value;
    }
}
