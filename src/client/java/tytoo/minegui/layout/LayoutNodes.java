package tytoo.minegui.layout;

import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.Margin;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.experimental.GridLayout.ColumnDefinition;
import tytoo.minegui.helper.layout.experimental.GridLayout.RowDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class LayoutNodes {
    private LayoutNodes() {
    }

    static LayoutNode render(LayoutRenderable renderable) {
        return new RenderNode(renderable != null ? renderable : () -> {
        });
    }

    static void close(AutoCloseable scope) {
        if (scope == null) {
            return;
        }
        try {
            scope.close();
        } catch (Exception ignored) {
        }
    }

    static Margin.Scope applyPadding(Insets padding) {
        if (padding == null) {
            return null;
        }
        return Margin.apply(padding.top(), padding.right(), padding.bottom(), padding.left());
    }

    enum StackOrientation {
        VERTICAL,
        HORIZONTAL
    }

    sealed interface LayoutNode permits StackNode, GridNode, RenderNode {
    }

    record RenderNode(LayoutRenderable renderable) implements LayoutNode {
    }

    record Insets(float top, float right, float bottom, float left) {
        Insets {
            top = sanitize(top);
            right = sanitize(right);
            bottom = sanitize(bottom);
            left = sanitize(left);
        }

        private static float sanitize(float value) {
            if (!Float.isFinite(value)) {
                return 0f;
            }
            return Math.max(0f, value);
        }

        static Insets uniform(float value) {
            return new Insets(value, value, value, value);
        }
    }

    record StackNode(
            StackOrientation orientation,
            Float spacing,
            Insets padding,
            VStack.FillMode fillMode,
            Float uniformWidth,
            HStack.Alignment alignment,
            Boolean equalizeHeight,
            Float uniformHeight,
            List<LayoutSlot> children
    ) implements LayoutNode {
        StackNode {
            children = Collections.unmodifiableList(new ArrayList<>(children));
        }
    }

    record LayoutSlot(
            LayoutNode node,
            Float estimatedWidth,
            Float estimatedHeight,
            boolean fillWidth
    ) {
    }

    record GridNode(GridDefinition definition) implements LayoutNode {
    }

    record GridDefinition(
            ColumnDefinition[] columns,
            RowDefinition[] rows,
            Float columnSpacing,
            Float rowSpacing,
            List<GridCell> cells) {
        GridDefinition {
            columns = columns != null ? columns.clone() : null;
            rows = rows != null ? rows.clone() : null;
            cells = Collections.unmodifiableList(new ArrayList<>(cells));
        }
    }

    record GridCell(
            LayoutNode node,
            int column,
            int row,
            int columnSpan,
            int rowSpan,
            Float estimatedWidth,
            Float estimatedHeight,
            boolean fillWidth
    ) {
    }
}
