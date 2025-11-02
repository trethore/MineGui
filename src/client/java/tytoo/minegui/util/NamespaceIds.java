package tytoo.minegui.util;

import java.util.Objects;

public final class NamespaceIds {
    private NamespaceIds() {
    }

    public static String make(String namespace, String path) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");
        String trimmedNamespace = namespace.trim();
        String trimmedPath = path.trim();
        if (trimmedNamespace.isEmpty()) {
            throw new IllegalArgumentException("namespace cannot be blank");
        }
        if (trimmedPath.isEmpty()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        return trimmedNamespace + ":" + trimmedPath;
    }
}
