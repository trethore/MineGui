package tytoo.minegui.helper.layout.sizing;

public record SizeRange(Float min, Float max) {
    public SizeRange {
        if (min != null && (!Float.isFinite(min) || min < 0f)) {
            min = null;
        }
        if (max != null && (!Float.isFinite(max) || max < 0f)) {
            max = null;
        }
        if (min != null && max != null && min > max) {
            max = min;
        }
    }

    private static float sanitize(float value) {
        if (!Float.isFinite(value) || value < 0f) {
            return 0f;
        }
        return value;
    }

    public static SizeRange of(Float min, Float max) {
        return new SizeRange(min, max);
    }

    public float clamp(float value) {
        float sanitized = sanitize(value);
        if (min != null) {
            sanitized = Math.max(sanitized, min);
        }
        if (max != null) {
            sanitized = Math.min(sanitized, max);
        }
        return sanitized;
    }
}
