package tytoo.minegui;

import tytoo.minegui.runtime.MineGuiContext;

public final class MineGui {

    private MineGui() {
    }

    public static MineGuiContext setup(String namespace) {
        return MineGuiCore.init(MineGuiOptions.of(namespace));
    }

    public static MineGuiContext setupLite(String namespace) {
        return MineGuiCore.init(MineGuiOptions.lite(namespace));
    }

    public static MineGuiContext setup(MineGuiOptions options) {
        return MineGuiCore.init(options);
    }
}
