package tytoo.minegui.util;

public final class NamespaceIds {
    private NamespaceIds() {
    }

    public static String make(String namespace, String path) {
        return ResourceId.of(namespace, path).toString();
    }
}
