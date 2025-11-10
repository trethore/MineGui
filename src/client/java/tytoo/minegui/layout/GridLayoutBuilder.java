package tytoo.minegui.layout;

import tytoo.minegui.helper.layout.experimental.GridLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class GridLayoutBuilder {
    private final List<LayoutNodes.GridCell> cells = new ArrayList<>();
    private GridLayout.ColumnDefinition[] columns;
    private GridLayout.RowDefinition[] rows;
    private Float columnSpacing;
    private Float rowSpacing;

    GridLayoutBuilder() {
    }

    private static Float sanitizeSpacing(float spacing) {
        if (!Float.isFinite(spacing)) {
            return null;
        }
        return Math.max(0f, spacing);
    }

    public GridLayoutBuilder columns(GridLayout.ColumnDefinition... columns) {
        this.columns = columns;
        return this;
    }

    public GridLayoutBuilder rows(GridLayout.RowDefinition... rows) {
        this.rows = rows;
        return this;
    }

    public GridLayoutBuilder columnSpacing(float spacing) {
        this.columnSpacing = sanitizeSpacing(spacing);
        return this;
    }

    public GridLayoutBuilder rowSpacing(float spacing) {
        this.rowSpacing = sanitizeSpacing(spacing);
        return this;
    }

    public GridLayoutBuilder cell(int column, int row, LayoutRenderable renderable) {
        Objects.requireNonNull(renderable, "renderable");
        cells.add(new LayoutNodes.GridCell(LayoutNodes.render(renderable), column, row, 1, 1, null, null, false));
        return this;
    }

    public GridLayoutBuilder cell(int column, int row, LayoutTemplate template) {
        Objects.requireNonNull(template, "template");
        cells.add(new LayoutNodes.GridCell(template.root(), column, row, 1, 1, null, null, false));
        return this;
    }

    public GridLayoutBuilder cell(Consumer<GridCellBuilder> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        GridCellBuilder builder = new GridCellBuilder();
        customizer.accept(builder);
        cells.add(builder.build());
        return this;
    }

    public LayoutTemplate build() {
        LayoutNodes.GridDefinition definition = new LayoutNodes.GridDefinition(
                columns,
                rows,
                columnSpacing,
                rowSpacing,
                cells
        );
        return new LayoutTemplate(new LayoutNodes.GridNode(definition));
    }

    public static final class GridCellBuilder {
        private LayoutNodes.LayoutNode node;
        private int column;
        private int row;
        private int columnSpan = 1;
        private int rowSpan = 1;
        private Float width;
        private Float height;
        private boolean fillWidth;

        public GridCellBuilder content(LayoutRenderable renderable) {
            this.node = LayoutNodes.render(Objects.requireNonNull(renderable, "renderable"));
            return this;
        }

        public GridCellBuilder template(LayoutTemplate template) {
            Objects.requireNonNull(template, "template");
            this.node = template.root();
            return this;
        }

        public GridCellBuilder column(int column) {
            this.column = column;
            return this;
        }

        public GridCellBuilder row(int row) {
            this.row = row;
            return this;
        }

        public GridCellBuilder columnSpan(int span) {
            this.columnSpan = Math.max(1, span);
            return this;
        }

        public GridCellBuilder rowSpan(int span) {
            this.rowSpan = Math.max(1, span);
            return this;
        }

        public GridCellBuilder width(float width) {
            if (Float.isFinite(width) && width > 0f) {
                this.width = width;
            }
            return this;
        }

        public GridCellBuilder height(float height) {
            if (Float.isFinite(height) && height > 0f) {
                this.height = height;
            }
            return this;
        }

        public GridCellBuilder fillWidth(boolean fill) {
            this.fillWidth = fill;
            return this;
        }

        LayoutNodes.GridCell build() {
            if (node == null) {
                throw new IllegalStateException("Grid cell is missing content");
            }
            return new LayoutNodes.GridCell(node, column, row, columnSpan, rowSpan, width, height, fillWidth);
        }
    }
}
