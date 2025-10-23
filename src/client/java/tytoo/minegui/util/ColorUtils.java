package tytoo.minegui.util;

public final class ColorUtils {
    private static final float INV_255 = 1.0f / 255.0f;

    private ColorUtils() {
    }

    public static float clampUnit(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        if (value <= 0.0f) {
            return 0.0f;
        }
        return Math.min(value, 1.0f);
    }

    public static int packRgb(float[] color) {
        float r = channel(color, 0, 0.0f);
        float g = channel(color, 1, 0.0f);
        float b = channel(color, 2, 0.0f);
        return packRgb(r, g, b);
    }

    public static int packRgb(float r, float g, float b) {
        int ri = toByte(r);
        int gi = toByte(g);
        int bi = toByte(b);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    public static int packRgba(float[] color) {
        float r = channel(color, 0, 0.0f);
        float g = channel(color, 1, 0.0f);
        float b = channel(color, 2, 0.0f);
        float a = channel(color, 3, 1.0f);
        return packRgba(r, g, b, a);
    }

    public static int packRgba(float r, float g, float b, float a) {
        int ri = toByte(r);
        int gi = toByte(g);
        int bi = toByte(b);
        int ai = toByte(a);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    public static void unpackRgb(int rgb, float[] dest) {
        ensure(dest, 3);
        dest[0] = ((rgb >> 16) & 0xFF) * INV_255;
        dest[1] = ((rgb >> 8) & 0xFF) * INV_255;
        dest[2] = (rgb & 0xFF) * INV_255;
    }

    public static void unpackRgba(int rgba, float[] dest) {
        ensure(dest, 4);
        dest[0] = ((rgba >> 16) & 0xFF) * INV_255;
        dest[1] = ((rgba >> 8) & 0xFF) * INV_255;
        dest[2] = (rgba & 0xFF) * INV_255;
        dest[3] = ((rgba >>> 24) & 0xFF) * INV_255;
    }

    public static void toRgba(float[] source, int components, float[] target) {
        ensure(target, 4);
        float r = components > 0 && source != null ? source[0] : 0.0f;
        float g = components > 1 && source != null ? source[1] : 0.0f;
        float b = components > 2 && source != null ? source[2] : 0.0f;
        float a = components > 3 && source != null ? source[3] : 1.0f;
        target[0] = clampUnit(r);
        target[1] = clampUnit(g);
        target[2] = clampUnit(b);
        target[3] = clampUnit(a);
    }

    private static float channel(float[] color, int index, float fallback) {
        if (color == null || index >= color.length) {
            return fallback;
        }
        return color[index];
    }

    private static int toByte(float value) {
        float clamped = clampUnit(value);
        return Math.round(clamped * 255.0f);
    }

    private static void ensure(float[] array, int length) {
        if (array == null || array.length < length) {
            throw new IllegalArgumentException("Array length must be >= " + length);
        }
    }
}
