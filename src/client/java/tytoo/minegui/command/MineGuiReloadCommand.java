package tytoo.minegui.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import tytoo.minegui.config.GlobalConfigManager;
import tytoo.minegui.config.NamespaceConfig;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;
import tytoo.minegui.style.StyleDescriptor;
import tytoo.minegui.style.StyleManager;
import tytoo.minegui.util.McClientBridge;
import tytoo.minegui.util.MineGuiText;
import tytoo.minegui.util.ResourceId;

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
                source.sendFeedback(MineGuiText.literal("MineGui has no registered namespaces to reload."));
            } else {
                source.sendError(MineGuiText.literal("MineGui namespace '" + namespace + "' is not registered."));
            }
            return 0;
        }
        McClientBridge.execute(() -> targets.forEach(MineGuiReloadCommand::reloadNamespace));
        String message = namespace == null
                ? "MineGui reloaded namespaces: " + targets.stream().map(MineGuiNamespaceContext::namespace).collect(Collectors.joining(", "))
                : "MineGui namespace '" + namespace + "' reloaded.";
        source.sendFeedback(MineGuiText.literal(message));
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
        context.config().reload();
        applyConfiguredStyle(context);
    }

    private static void applyConfiguredStyle(MineGuiNamespaceContext context) {
        NamespaceConfig config = context.config().current();
        ResourceId styleKey = config.globalStyleKey();
        StyleManager styleManager = context.style();
        if (styleManager.getGlobalDescriptor().isEmpty()) {
            StyleManager.get(GlobalConfigManager.getDefaultNamespace())
                    .getGlobalDescriptor()
                    .map(descriptor -> StyleDescriptor.builder().fromDescriptor(descriptor).build())
                    .ifPresent(styleManager::setGlobalDescriptor);
        }
        styleManager.setGlobalStyleKey(styleKey);
        styleManager.apply();
    }

}
