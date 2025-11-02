package tytoo.minegui.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record ResourceId(String namespace, String path) {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9/._-]+");

    public static ResourceId of(String namespace, String path) {
        String normalizedNamespace = normalizeNamespace(namespace);
        String normalizedPath = normalizePath(path);
        return new ResourceId(normalizedNamespace, normalizedPath);
    }

    public static ResourceId parse(String value) {
        Objects.requireNonNull(value, "value");
        String trimmed = value.trim();
        int separator = trimmed.indexOf(':');
        if (separator <= 0 || separator == trimmed.length() - 1) {
            throw new IllegalArgumentException("Invalid resource identifier: '" + value + "'");
        }
        String namespace = trimmed.substring(0, separator);
        String path = trimmed.substring(separator + 1);
        return of(namespace, path);
    }

    public static ResourceId tryParse(String value) {
        try {
            return parse(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String normalizeNamespace(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        String trimmed = namespace.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be blank");
        }
        if (!NAMESPACE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid namespace '" + namespace + "'");
        }
        return trimmed;
    }

    private static String normalizePath(String path) {
        Objects.requireNonNull(path, "path");
        String trimmed = path.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be blank");
        }
        if (!PATH_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        return trimmed;
    }

    @Override
    public @NotNull String toString() {
        return namespace + ":" + path;
    }
}
