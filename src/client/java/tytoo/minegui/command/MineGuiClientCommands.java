package tytoo.minegui.command;

import imgui.ImGui;
import imgui.ImGuiStyle;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tytoo.minegui.MineGuiCore;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.style.*;

public final class MineGuiClientCommands {
    private MineGuiClientCommands() {
    }

    public static void register() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommandManager.literal("minegui")
                                .then(ClientCommandManager.literal("reload")
                                        .then(ClientCommandManager.literal("config")
                                                .executes(context -> {
                                                    MinecraftClient.getInstance().execute(() -> {
                                                        MineGuiCore.loadConfig();
                                                        applyConfiguredStyle();
                                                    });
                                                    context.getSource().sendFeedback(Text.literal("MineGui configuration reloaded."));
                                                    return 1;
                                                }))
                                        .then(ClientCommandManager.literal("style")
                                                .executes(context -> {
                                                    MinecraftClient.getInstance().execute(MineGuiClientCommands::reloadStyles);
                                                    context.getSource().sendFeedback(Text.literal("MineGui styles rebuilt."));
                                                    return 1;
                                                })))
                                .executes(context -> {
                                    context.getSource().sendFeedback(Text.literal("Usage: /minegui reload <config|style>"));
                                    return 1;
                                })
                )
        );
    }

    private static void reloadStyles() {
        StyleManager styleManager = StyleManager.getInstance();
        MGStyleDescriptor currentDescriptor = styleManager.getGlobalDescriptor().orElse(null);
        Identifier fontKey = currentDescriptor != null ? currentDescriptor.getFontKey() : MGFontLibrary.getInstance().getDefaultFontKey();
        Float fontSize = currentDescriptor != null ? currentDescriptor.getFontSize() : null;

        ImGuiStyle nativeStyle = ImGui.getStyle();
        MGStyleDescriptor refreshedDescriptor = MGStyleDescriptor.capture(
                nativeStyle,
                MGColorPalette.fromStyle(nativeStyle),
                fontKey,
                fontSize
        );

        styleManager.setGlobalDescriptor(refreshedDescriptor);
        NamedStyleRegistry.getInstance().registerBasePresets(refreshedDescriptor);
        applyConfiguredStyle();
    }

    private static void applyConfiguredStyle() {
        GlobalConfig config = GlobalConfigManager.getConfig(MineGuiCore.getConfigNamespace());
        String configured = config.getGlobalStyleKey();
        Identifier styleKey = (configured == null || configured.isBlank()) ? null : Identifier.tryParse(configured);
        StyleManager styleManager = StyleManager.getInstance();
        styleManager.setGlobalStyleKey(styleKey);
        styleManager.apply();
        // when styleKey is null, StyleManager persists clearing via setGlobalStyleKey
    }
}
