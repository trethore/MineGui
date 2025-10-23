package tytoo.minegui.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
public final class InputHelper {

    private InputHelper() {
    }

    public static boolean isKeyPressed(int targetKeyCode) {
        if (targetKeyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        int targetFinalKeyCode = remapToQwerty(targetKeyCode);
        long handle = McUtils.getMc()
                .map(mc -> mc.getWindow().getHandle())
                .orElse(-1L);
        return handle != -1L && GLFW.glfwGetKey(handle, targetFinalKeyCode) == GLFW.GLFW_PRESS;
    }

    public static int toQwerty(int keyCode) {
        return remapToQwerty(keyCode);
    }

    private static int remapToQwerty(int localKeyCode) {
        if (localKeyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        String keyName = GLFW.glfwGetKeyName(localKeyCode, 0);
        if (keyName == null || keyName.isEmpty()) {
            return localKeyCode;
        }
        return switch (keyName) {
            case "0" -> GLFW.GLFW_KEY_0;
            case "1" -> GLFW.GLFW_KEY_1;
            case "2" -> GLFW.GLFW_KEY_2;
            case "3" -> GLFW.GLFW_KEY_3;
            case "4" -> GLFW.GLFW_KEY_4;
            case "5" -> GLFW.GLFW_KEY_5;
            case "6" -> GLFW.GLFW_KEY_6;
            case "7" -> GLFW.GLFW_KEY_7;
            case "8" -> GLFW.GLFW_KEY_8;
            case "9" -> GLFW.GLFW_KEY_9;
            case "A", "a" -> GLFW.GLFW_KEY_A;
            case "B", "b" -> GLFW.GLFW_KEY_B;
            case "C", "c" -> GLFW.GLFW_KEY_C;
            case "D", "d" -> GLFW.GLFW_KEY_D;
            case "E", "e" -> GLFW.GLFW_KEY_E;
            case "F", "f" -> GLFW.GLFW_KEY_F;
            case "G", "g" -> GLFW.GLFW_KEY_G;
            case "H", "h" -> GLFW.GLFW_KEY_H;
            case "I", "i" -> GLFW.GLFW_KEY_I;
            case "J", "j" -> GLFW.GLFW_KEY_J;
            case "K", "k" -> GLFW.GLFW_KEY_K;
            case "L", "l" -> GLFW.GLFW_KEY_L;
            case "M", "m" -> GLFW.GLFW_KEY_M;
            case "N", "n" -> GLFW.GLFW_KEY_N;
            case "O", "o" -> GLFW.GLFW_KEY_O;
            case "P", "p" -> GLFW.GLFW_KEY_P;
            case "Q", "q" -> GLFW.GLFW_KEY_Q;
            case "R", "r" -> GLFW.GLFW_KEY_R;
            case "S", "s" -> GLFW.GLFW_KEY_S;
            case "T", "t" -> GLFW.GLFW_KEY_T;
            case "U", "u" -> GLFW.GLFW_KEY_U;
            case "V", "v" -> GLFW.GLFW_KEY_V;
            case "W", "w" -> GLFW.GLFW_KEY_W;
            case "X", "x" -> GLFW.GLFW_KEY_X;
            case "Y", "y" -> GLFW.GLFW_KEY_Y;
            case "Z", "z" -> GLFW.GLFW_KEY_Z;
            default -> localKeyCode;
        };
    }

    public static boolean isUndo() {
        return isKeyPressed(GLFW.GLFW_KEY_Z) && isControlDown() && !isShiftDown() && !isAltDown();
    }

    public static boolean isRedo() {
        return isKeyPressed(GLFW.GLFW_KEY_Y) && isControlDown() && !isShiftDown() && !isAltDown();
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
        if (MinecraftClient.IS_SYSTEM_MAC) {
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
