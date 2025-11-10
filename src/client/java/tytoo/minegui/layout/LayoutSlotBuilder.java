package tytoo.minegui.layout;

import java.util.Objects;

public final class LayoutSlotBuilder {
    private LayoutNodes.LayoutNode node;
    private Float width;
    private Float height;
    private boolean fillWidth;

    public LayoutSlotBuilder content(LayoutRenderable renderable) {
        this.node = LayoutNodes.render(Objects.requireNonNull(renderable, "renderable"));
        return this;
    }

    public LayoutSlotBuilder template(LayoutTemplate template) {
        Objects.requireNonNull(template, "template");
        this.node = template.root();
        return this;
    }

    public LayoutSlotBuilder width(float width) {
        if (Float.isFinite(width) && width > 0f) {
            this.width = width;
        }
        return this;
    }

    public LayoutSlotBuilder height(float height) {
        if (Float.isFinite(height) && height > 0f) {
            this.height = height;
        }
        return this;
    }

    public LayoutSlotBuilder fillWidth(boolean fill) {
        this.fillWidth = fill;
        return this;
    }

    LayoutNodes.LayoutSlot build() {
        if (node == null) {
            throw new IllegalStateException("Layout slot is missing content");
        }
        return new LayoutNodes.LayoutSlot(node, width, height, fillWidth);
    }
}
