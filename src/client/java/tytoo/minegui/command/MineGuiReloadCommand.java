package tytoo.minegui.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tytoo.minegui.config.GlobalConfig;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.style.MGStyleDescriptor;
import tytoo.minegui.style.StyleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MineGuiReloadCommand {
    private MineGuiReloadCommand() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builder() {
        return ClientCommandManager.literal("reload")
                .executes(context -> execute(context.getSource(), null))
                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                        .executes(context -> execute(
                                context.getSource(),
                                StringArgumentType.getString(context, "namespace")
                        )));
    }

    private static int execute(FabricClientCommandSource source, String namespace) {
        List<MineGuiNamespaceContext> targets = collectTargets(namespace);
        if (targets.isEmpty()) {
            if (namespace == null) {
                source.sendFeedback(Text.literal("MineGui has no registered namespaces to reload."));
            } else {
                source.sendError(Text.literal("MineGui namespace '" + namespace + "' is not registered."));
            }
            return 0;
        }
        MinecraftClient.getInstance().execute(() -> targets.forEach(MineGuiReloadCommand::reloadNamespace));
        String message = namespace == null
                ? "MineGui reloaded namespaces: " + targets.stream().map(MineGuiNamespaceContext::namespace).collect(Collectors.joining(", "))
                : "MineGui namespace '" + namespace + "' reloaded.";
        source.sendFeedback(Text.literal(message));
        return targets.size();
    }

    private static List<MineGuiNamespaceContext> collectTargets(String namespace) {
        if (namespace == null) {
            return new ArrayList<>(MineGuiNamespaces.all());
        }
        MineGuiNamespaceContext context = MineGuiNamespaces.get(namespace);
        if (context == null) {
            return List.of();
        }
        return List.of(context);
    }

    private static void reloadNamespace(MineGuiNamespaceContext context) {
        String namespace = context.namespace();
        if (!GlobalConfigManager.isConfigIgnored(namespace)) {
            GlobalConfigManager.load(namespace);
        } else {
            GlobalConfigManager.ensureContext(namespace);
        }
        applyConfiguredStyle(context);
    }

    private static void applyConfiguredStyle(MineGuiNamespaceContext context) {
        GlobalConfig config = GlobalConfigManager.getConfig(context.namespace());
        String configured = config.getGlobalStyleKey();
        Identifier styleKey = (configured == null || configured.isBlank()) ? null : Identifier.tryParse(configured);
        StyleManager styleManager = context.style();
        if (styleManager.getGlobalDescriptor().isEmpty()) {
            StyleManager.get(GlobalConfigManager.getDefaultNamespace())
                    .getGlobalDescriptor()
                    .map(descriptor -> MGStyleDescriptor.builder().fromDescriptor(descriptor).build())
                    .ifPresent(styleManager::setGlobalDescriptor);
        }
        styleManager.setGlobalStyleKey(styleKey);
        styleManager.apply();
    }
}
