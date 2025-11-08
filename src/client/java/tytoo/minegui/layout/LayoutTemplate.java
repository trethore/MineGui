package tytoo.minegui.layout;

import java.util.Objects;

public record LayoutTemplate(LayoutNodes.LayoutNode root) {
    public LayoutTemplate {
        Objects.requireNonNull(root, "root");
    }

    public static LayoutTemplate empty() {
        return new LayoutTemplate(LayoutNodes.render(() -> {
        }));
    }
}
