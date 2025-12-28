package tytoo.minegui;

import tytoo.minegui.config.ConfigRegistry;
import tytoo.minegui.runtime.MineGuiContext;

public final class MineGui {

    private MineGui() {
    }

    /**
     * Initializes MineGui with standard configuration settings.
     * <p>
     * Configuration files (styles, views) will be stored in {@code config/<namespace>/}.
     * Use this for full-featured mods requiring persistence and custom styling.
     *
     * @param namespace The unique namespace for the mod/context.
     * @return The initialized MineGui context.
     */
    public static MineGuiContext setup(String namespace) {
        return MineGuiCore.init(MineGuiInitializationOptions.defaults(namespace));
    }

    /**
     * Initializes MineGui in "simple" mode, ideal for debug overlays or lightweight usage.
     * <p>
     * Global configuration loading is skipped to speed up startup.
     * To prevent clutter, configuration files are stored in a shared directory:
     * {@code config/minegui/simple/<namespace>/}.
     *
     * @param namespace The unique namespace for the mod/context.
     * @return The initialized MineGui context.
     */
    public static MineGuiContext setupSimple(String namespace) {
        return MineGuiCore.init(
                MineGuiInitializationOptions.skipGlobalConfig(namespace)
                        .withConfigRoot(ConfigRegistry.configRoot().resolve(MineGuiCore.ID).resolve("simple"))
        );
    }
}
