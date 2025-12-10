package tytoo.minegui.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.runtime.MineGuiContext;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.McClientBridge;
import tytoo.minegui.util.MineGuiText;
import tytoo.minegui.util.ResourceId;

public final class MineGuiReloadCommand {
    private MineGuiReloadCommand() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builder() {
        return ClientCommandManager.literal("reload")
                .executes(context -> execute(context.getSource()));
    }

    private static int execute(FabricClientCommandSource source) {
        McClientBridge.execute(() -> {
            MineGuiCore.loadConfig();
            for (MineGuiContext context : MineGuiCore.getAllContexts()) {
                applyConfiguredStyle(context);
            }
            MineGuiCore.requestReload();
        });
        source.sendFeedback(MineGuiText.literal("MineGui reloaded."));
        return 1;
    }

    private static void applyConfiguredStyle(MineGuiContext context) {
        StyleManager styleManager = context.style();
        NamespaceConfig config = context.config().current();
        ResourceId styleKey = config.globalStyleKey();

        styleManager.setGlobalStyleKey(styleKey);
        styleManager.apply();
    }
}
