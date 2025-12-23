package tytoo.minegui;

import tytoo.minegui.runtime.MineGuiContext;

public final class MineGui {

    private MineGui() {
    }

    public static MineGuiContext setup(String namespace) {
        return MineGuiCore.init(MineGuiInitializationOptions.defaults(namespace));
    }

    public static MineGuiContext setupSimple(String namespace) {
        return MineGuiCore.init(MineGuiInitializationOptions.skipGlobalConfig().withNamespace(namespace));
    }
}