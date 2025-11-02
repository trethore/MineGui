package tytoo.minegui.util;

import net.minecraft.util.Identifier;

import java.util.Objects;

public final class MinecraftIdentifiers {
    private MinecraftIdentifiers() {
    }

    public static Identifier toMinecraft(ResourceId id) {
        Objects.requireNonNull(id, "id");
        return Identifier.of(id.namespace(), id.path());
    }

    public static ResourceId fromMinecraft(Identifier identifier) {
        Objects.requireNonNull(identifier, "identifier");
        return ResourceId.of(identifier.getNamespace(), identifier.getPath());
    }
}
