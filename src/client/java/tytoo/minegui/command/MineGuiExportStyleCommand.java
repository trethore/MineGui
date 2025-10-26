package tytoo.minegui.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import tytoo.minegui.runtime.MineGuiNamespaceContext;
import tytoo.minegui.runtime.MineGuiNamespaces;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MineGuiExportStyleCommand {
    private MineGuiExportStyleCommand() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builder() {
        LiteralArgumentBuilder<FabricClientCommandSource> export = ClientCommandManager.literal("export");
        export.then(ClientCommandManager.literal("style")
                .executes(context -> execute(context.getSource(), null, false))
                .then(ClientCommandManager.literal("force")
                        .executes(context -> execute(context.getSource(), null, true)))
                .then(ClientCommandManager.literal("namespace")
                        .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                .executes(context -> execute(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "namespace"),
                                        false
                                ))
                                .then(ClientCommandManager.literal("force")
                                        .executes(context -> execute(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "namespace"),
                                                true
                                        ))))));
        return export;
    }

    private static int execute(FabricClientCommandSource source, String namespace, boolean forceRewrite) {
        List<MineGuiNamespaceContext> targets = collectTargets(namespace);
        if (targets.isEmpty()) {
            if (namespace == null) {
                source.sendFeedback(Text.literal("MineGui has no registered namespaces to export styles from."));
            } else {
                source.sendError(Text.literal("MineGui namespace '" + namespace + "' is not registered."));
            }
            return 0;
        }
        MinecraftClient.getInstance().execute(() -> {
            int totalExports = 0;
            for (MineGuiNamespaceContext context : targets) {
                totalExports += context.viewSaves().exportStyles(forceRewrite);
            }
            String namespaceList = targets.stream()
                    .map(MineGuiNamespaceContext::namespace)
                    .collect(Collectors.joining(", "));
            boolean multiple = targets.size() > 1;
            String resolvedList = multiple ? namespaceList : "'" + namespaceList + "'";
            String message;
            if (totalExports > 0) {
                message = (forceRewrite ? "MineGui force-exported " : "MineGui exported ")
                        + totalExports
                        + " style file(s) for namespace"
                        + (multiple ? "s: " : ": ")
                        + resolvedList
                        + ".";
            } else {
                message = "MineGui found no style snapshots to export for namespace"
                        + (multiple ? "s: " : ": ")
                        + resolvedList
                        + ".";
            }
            source.sendFeedback(Text.literal(message));
        });
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
}
