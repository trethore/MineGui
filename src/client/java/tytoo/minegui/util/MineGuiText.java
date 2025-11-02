package tytoo.minegui.util;

import net.minecraft.text.Text;

import java.util.Objects;

public final class MineGuiText {
    private MineGuiText() {
    }

    public static Text literal(String message) {
        Objects.requireNonNull(message, "message");
        return Text.literal(message);
    }
}
