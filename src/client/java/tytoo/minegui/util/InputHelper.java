package tytoo.minegui.util;

import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unused")
public final class InputHelper {
    private static final int NO_CACHE = Integer.MIN_VALUE;
    private static final int[] KEY_REMAP_CACHE = new int[GLFW.GLFW_KEY_LAST + 1];

    private static final Map<String, Integer> KEY_NAME_MAP = Map.<String, Integer>ofEntries(
            Map.entry("0", GLFW.GLFW_KEY_0),
            Map.entry("1", GLFW.GLFW_KEY_1),
            Map.entry("2", GLFW.GLFW_KEY_2),
            Map.entry("3", GLFW.GLFW_KEY_3),
            Map.entry("4", GLFW.GLFW_KEY_4),
            Map.entry("5", GLFW.GLFW_KEY_5),
            Map.entry("6", GLFW.GLFW_KEY_6),
            Map.entry("7", GLFW.GLFW_KEY_7),
            Map.entry("8", GLFW.GLFW_KEY_8),
            Map.entry("9", GLFW.GLFW_KEY_9),
            Map.entry("A", GLFW.GLFW_KEY_A),
            Map.entry("a", GLFW.GLFW_KEY_A),
            Map.entry("B", GLFW.GLFW_KEY_B),
            Map.entry("b", GLFW.GLFW_KEY_B),
            Map.entry("C", GLFW.GLFW_KEY_C),
            Map.entry("c", GLFW.GLFW_KEY_C),
            Map.entry("D", GLFW.GLFW_KEY_D),
            Map.entry("d", GLFW.GLFW_KEY_D),
            Map.entry("E", GLFW.GLFW_KEY_E),
            Map.entry("e", GLFW.GLFW_KEY_E),
            Map.entry("F", GLFW.GLFW_KEY_F),
            Map.entry("f", GLFW.GLFW_KEY_F),
            Map.entry("G", GLFW.GLFW_KEY_G),
            Map.entry("g", GLFW.GLFW_KEY_G),
            Map.entry("H", GLFW.GLFW_KEY_H),
            Map.entry("h", GLFW.GLFW_KEY_H),
            Map.entry("I", GLFW.GLFW_KEY_I),
            Map.entry("i", GLFW.GLFW_KEY_I),
            Map.entry("J", GLFW.GLFW_KEY_J),
            Map.entry("j", GLFW.GLFW_KEY_J),
            Map.entry("K", GLFW.GLFW_KEY_K),
            Map.entry("k", GLFW.GLFW_KEY_K),
            Map.entry("L", GLFW.GLFW_KEY_L),
            Map.entry("l", GLFW.GLFW_KEY_L),
            Map.entry("M", GLFW.GLFW_KEY_M),
            Map.entry("m", GLFW.GLFW_KEY_M),
            Map.entry("N", GLFW.GLFW_KEY_N),
            Map.entry("n", GLFW.GLFW_KEY_N),
            Map.entry("O", GLFW.GLFW_KEY_O),
            Map.entry("o", GLFW.GLFW_KEY_O),
            Map.entry("P", GLFW.GLFW_KEY_P),
            Map.entry("p", GLFW.GLFW_KEY_P),
            Map.entry("Q", GLFW.GLFW_KEY_Q),
            Map.entry("q", GLFW.GLFW_KEY_Q),
            Map.entry("R", GLFW.GLFW_KEY_R),
            Map.entry("r", GLFW.GLFW_KEY_R),
            Map.entry("S", GLFW.GLFW_KEY_S),
            Map.entry("s", GLFW.GLFW_KEY_S),
            Map.entry("T", GLFW.GLFW_KEY_T),
            Map.entry("t", GLFW.GLFW_KEY_T),
            Map.entry("U", GLFW.GLFW_KEY_U),
            Map.entry("u", GLFW.GLFW_KEY_U),
            Map.entry("V", GLFW.GLFW_KEY_V),
            Map.entry("v", GLFW.GLFW_KEY_V),
            Map.entry("W", GLFW.GLFW_KEY_W),
            Map.entry("w", GLFW.GLFW_KEY_W),
            Map.entry("X", GLFW.GLFW_KEY_X),
            Map.entry("x", GLFW.GLFW_KEY_X),
            Map.entry("Y", GLFW.GLFW_KEY_Y),
            Map.entry("y", GLFW.GLFW_KEY_Y),
            Map.entry("Z", GLFW.GLFW_KEY_Z),
            Map.entry("z", GLFW.GLFW_KEY_Z)
    );

    static {
        Arrays.fill(KEY_REMAP_CACHE, NO_CACHE);
    }

    private InputHelper() {
    }

    public static boolean isKeyPressed(int targetKeyCode) {
        if (targetKeyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        int targetFinalKeyCode = remapToQwerty(targetKeyCode);
        long handle = McClientBridge.windowHandle();
        return handle != -1L && GLFW.glfwGetKey(handle, targetFinalKeyCode) == GLFW.GLFW_PRESS;
    }

    public static int toQwerty(int keyCode) {
        return remapToQwerty(keyCode);
    }

    private static int remapToQwerty(int localKeyCode) {
        if (localKeyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        if (localKeyCode >= 0 && localKeyCode < KEY_REMAP_CACHE.length) {
            int cached = KEY_REMAP_CACHE[localKeyCode];
            if (cached != NO_CACHE) {
                return cached;
            }
            int mapped = computeQwertyMapping(localKeyCode);
            KEY_REMAP_CACHE[localKeyCode] = mapped;
            return mapped;
        }
        return computeQwertyMapping(localKeyCode);
    }

    private static int computeQwertyMapping(int localKeyCode) {
        String keyName = GLFW.glfwGetKeyName(localKeyCode, 0);
        if (keyName == null || keyName.isEmpty()) {
            return localKeyCode;
        }
        return KEY_NAME_MAP.getOrDefault(keyName, localKeyCode);
    }

    public static boolean isUndo() {
        return isKeyPressed(GLFW.GLFW_KEY_Z) && isControlDown() && !isShiftDown() && !isAltDown();
    }

    public static boolean isRedo() {
        boolean standardRedo = isKeyPressed(GLFW.GLFW_KEY_Y) && isControlDown() && !isShiftDown() && !isAltDown();
        boolean macRedo = McClientBridge.isOnMac()
                && isKeyPressed(GLFW.GLFW_KEY_Z)
                && isControlDown()
                && isShiftDown()
                && !isAltDown();
        return standardRedo || macRedo;
    }

    public static boolean isSelectAll() {
        return isKeyPressed(GLFW.GLFW_KEY_A) && isControlDown() && !isShiftDown() && !isAltDown();
    }

    public static boolean isCopy() {
        return isKeyPressed(GLFW.GLFW_KEY_C) && isControlDown() && !isShiftDown() && !isAltDown();
    }

    public static boolean isPaste() {
        return isKeyPressed(GLFW.GLFW_KEY_V) && isControlDown() && !isShiftDown() && !isAltDown();
    }

    public static boolean isCut() {
        return isKeyPressed(GLFW.GLFW_KEY_X) && isControlDown() && !isShiftDown() && !isAltDown();
    }

    public static boolean isControlDown() {
        if (McClientBridge.isOnMac()) {
            return isKeyPressed(GLFW.GLFW_KEY_LEFT_SUPER) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SUPER);
        }
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean isShiftDown() {
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean isAltDown() {
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_ALT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_ALT);
    }

    public static boolean isMetaDown() {
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_SUPER) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SUPER);
    }
}
