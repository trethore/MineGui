package tytoo.minegui.style;

import imgui.ImFont;
import tytoo.minegui.util.ResourceId;

import java.util.Objects;

public final class Fonts {

    private Fonts() {
    }

    public static ImFont ensure(ResourceId key) {
        Objects.requireNonNull(key, "key");
        FontLibrary library = FontLibrary.getInstance();
        return library.ensureFont(key, null);
    }
}
