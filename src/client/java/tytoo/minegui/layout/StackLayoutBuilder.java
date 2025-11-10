package tytoo.minegui.layout;

import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class StackLayoutBuilder {
    private final LayoutNodes.StackOrientation orientation;
    private final List<LayoutNodes.LayoutSlot> slots = new ArrayList<>();
    private Float spacing;
    private LayoutNodes.Insets padding;
    private VStack.FillMode fillMode;
    private Float uniformWidth;
    private HStack.Alignment alignment;
    private Boolean equalizeHeight;
    private Float uniformHeight;

    StackLayoutBuilder(LayoutNodes.StackOrientation orientation) {
        this.orientation = orientation;
    }

    public StackLayoutBuilder spacing(float spacing) {
        this.spacing = Float.isFinite(spacing) ? spacing : null;
        return this;
    }

    public StackLayoutBuilder padding(float uniform) {
        this.padding = LayoutNodes.Insets.uniform(uniform);
        return this;
    }

    public StackLayoutBuilder padding(float top, float right, float bottom, float left) {
        this.padding = new LayoutNodes.Insets(top, right, bottom, left);
        return this;
    }

    public StackLayoutBuilder fillMode(VStack.FillMode fillMode) {
        ensureVertical("fillMode");
        this.fillMode = fillMode;
        return this;
    }

    public StackLayoutBuilder uniformWidth(float width) {
        ensureVertical("uniformWidth");
        this.uniformWidth = width;
        return this;
    }

    public StackLayoutBuilder alignment(HStack.Alignment alignment) {
        ensureHorizontal("alignment");
        this.alignment = alignment;
        return this;
    }

    public StackLayoutBuilder equalizeHeight(boolean equalizeHeight) {
        ensureHorizontal("equalizeHeight");
        this.equalizeHeight = equalizeHeight;
        return this;
    }

    public StackLayoutBuilder uniformHeight(float height) {
        ensureHorizontal("uniformHeight");
        this.uniformHeight = height;
        return this;
    }

    public StackLayoutBuilder child(LayoutRenderable renderable) {
        Objects.requireNonNull(renderable, "renderable");
        slots.add(new LayoutNodes.LayoutSlot(LayoutNodes.render(renderable), null, null, false));
        return this;
    }

    public StackLayoutBuilder child(LayoutTemplate template) {
        Objects.requireNonNull(template, "template");
        slots.add(new LayoutNodes.LayoutSlot(template.root(), null, null, false));
        return this;
    }

    public StackLayoutBuilder child(Consumer<LayoutSlotBuilder> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        LayoutSlotBuilder builder = new LayoutSlotBuilder();
        customizer.accept(builder);
        slots.add(builder.build());
        return this;
    }

    public LayoutTemplate build() {
        return new LayoutTemplate(new LayoutNodes.StackNode(
                orientation,
                spacing,
                padding,
                fillMode,
                uniformWidth,
                alignment,
                equalizeHeight,
                uniformHeight,
                slots
        ));
    }

    private void ensureVertical(String method) {
        if (orientation != LayoutNodes.StackOrientation.VERTICAL) {
            throw new UnsupportedOperationException(method + " is only valid for vertical layouts.");
        }
    }

    private void ensureHorizontal(String method) {
        if (orientation != LayoutNodes.StackOrientation.HORIZONTAL) {
            throw new UnsupportedOperationException(method + " is only valid for row layouts.");
        }
    }
}
