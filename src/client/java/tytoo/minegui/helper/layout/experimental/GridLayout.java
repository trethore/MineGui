package tytoo.minegui.helper.layout.experimental;

import imgui.ImGui;
import imgui.ImGuiStyle;
import tytoo.minegui.helper.constraint.ConstraintTarget;
import tytoo.minegui.helper.constraint.LayoutConstraintSolver;
import tytoo.minegui.helper.constraint.constraints.Constraints;
import tytoo.minegui.helper.layout.LayoutConstraints;
import tytoo.minegui.helper.layout.cursor.LayoutContext;
import tytoo.minegui.helper.layout.cursor.LayoutCursor;
import tytoo.minegui.helper.layout.sizing.ScaleUnit;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.layout.sizing.SizeRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class GridLayout implements AutoCloseable {
    private final float originX;
    private final float originY;
    private final float columnSpacing;
    private final float rowSpacing;
    private final ColumnPlan[] columns;
    private final float[] columnOffsets;
    private final List<RowState> rows;
    private final List<RowDefinition> rowDefinitions;
    private float lastAvailableWidth;
    private int usedRowBound;
    private int usedColumnBound;
    private boolean closed;

    private GridLayout(Options options, LayoutContext context) {
        Options resolved = options != null ? options : new Options();
        LayoutContext chosenContext = context != null ? context : LayoutContext.capture();
        if (resolved.placement != null) {
            LayoutCursor.moveTo(resolved.placement, chosenContext);
        }
        this.originX = ImGui.getCursorPosX();
        this.originY = ImGui.getCursorPosY();
        this.columnSpacing = resolveColumnSpacing(resolved);
        this.rowSpacing = resolveRowSpacing(resolved);
        ColumnDefinition[] normalizedColumns = normalizeColumnDefinitions(resolved.columns);
        this.columns = initializeColumnPlans(normalizedColumns);
        float initialAvailableWidth = Math.max(0f, Math.max(chosenContext.contentRegionWidth(), chosenContext.contentRegionAvailX()));
        assignColumnWidths(columns, initialAvailableWidth, columnSpacing);
        this.columnOffsets = computeColumnOffsets(columns, columnSpacing);
        this.lastAvailableWidth = initialAvailableWidth;
        this.rowDefinitions = resolved.rows != null ? Arrays.asList(resolved.rows.clone()) : List.of();
        this.rows = new ArrayList<>();
    }

    public static GridLayout begin() {
        return begin(new Options(), null);
    }

    public static GridLayout begin(Options options) {
        return begin(options, null);
    }

    public static GridLayout begin(Options options, LayoutContext context) {
        return new GridLayout(options, context);
    }

    private static float resolveColumnSpacing(Options options) {
        Float configured = options.columnSpacing;
        ScaleUnit unit = options.columnSpacingUnit != null ? options.columnSpacingUnit : ScaleUnit.RAW;
        float resolved = Float.NaN;
        if (configured != null && Float.isFinite(configured) && configured > 0f) {
            resolved = unit.applyWidth(configured);
        }
        if (Float.isNaN(resolved)) {
            ImGuiStyle style = ImGui.getStyle();
            resolved = style != null ? Math.max(0f, style.getItemSpacingX()) : 0f;
        }
        return Math.max(0f, resolved);
    }

    private static float resolveRowSpacing(Options options) {
        Float configured = options.rowSpacing;
        ScaleUnit unit = options.rowSpacingUnit != null ? options.rowSpacingUnit : ScaleUnit.RAW;
        float resolved = Float.NaN;
        if (configured != null && Float.isFinite(configured) && configured > 0f) {
            resolved = unit.applyHeight(configured);
        }
        if (Float.isNaN(resolved)) {
            ImGuiStyle style = ImGui.getStyle();
            resolved = style != null ? Math.max(0f, style.getItemSpacingY()) : 0f;
        }
        return Math.max(0f, resolved);
    }

    private static ColumnDefinition[] normalizeColumnDefinitions(ColumnDefinition[] definitions) {
        ColumnDefinition[] source = definitions != null && definitions.length > 0
                ? definitions.clone()
                : new ColumnDefinition[]{ColumnDefinition.auto()};
        for (int i = 0; i < source.length; i++) {
            if (source[i] == null) {
                source[i] = ColumnDefinition.auto();
            }
        }
        return source;
    }

    private static ColumnPlan[] initializeColumnPlans(ColumnDefinition[] definitions) {
        ColumnPlan[] plans = new ColumnPlan[definitions.length];
        for (int i = 0; i < definitions.length; i++) {
            plans[i] = new ColumnPlan(definitions[i]);
        }
        return plans;
    }

    private static void assignColumnWidths(ColumnPlan[] plans, float availableWidth, float spacing) {
        int count = plans.length;
        float spacingTotal = spacing * Math.max(0, count - 1);
        float budget = Math.max(0f, availableWidth - spacingTotal);
        float totalFixed = 0f;
        float totalWeight = 0f;
        int autoCount = 0;
        for (ColumnPlan plan : plans) {
            switch (plan.definition.type()) {
                case FIXED -> totalFixed += plan.definition.fixedWidth();
                case WEIGHT -> totalWeight += plan.definition.weight();
                case AUTO -> autoCount++;
            }
        }
        float remaining = Math.max(0f, budget - totalFixed);
        if (totalWeight > 0f) {
            float weightUnit = remaining / totalWeight;
            for (ColumnPlan plan : plans) {
                if (plan.definition.type() == ColumnType.WEIGHT) {
                    float width = sanitizeLength(plan.definition.weight() * weightUnit);
                    plan.width = width;
                    remaining -= width;
                }
            }
            remaining = Math.max(0f, remaining);
        }
        if (autoCount > 0) {
            float share = remaining > 0f ? remaining / autoCount : 0f;
            for (ColumnPlan plan : plans) {
                if (plan.definition.type() == ColumnType.AUTO) {
                    float candidate = Math.max(share, plan.definition.minWidth());
                    plan.width = sanitizeLength(candidate);
                }
            }
        }
        for (ColumnPlan plan : plans) {
            if (plan.definition.type() == ColumnType.FIXED) {
                plan.width = sanitizeLength(plan.definition.fixedWidth());
            }
            if (plan.width <= 0f) {
                if (plan.definition.type() == ColumnType.AUTO) {
                    plan.width = sanitizeLength(plan.definition.minWidth());
                } else if (plan.definition.type() == ColumnType.FIXED) {
                    plan.width = sanitizeLength(plan.definition.fixedWidth());
                }
            }
        }
    }

    private static float[] computeColumnOffsets(ColumnPlan[] plans, float spacing) {
        float[] offsets = new float[plans.length];
        float accumulated = 0f;
        for (int i = 0; i < plans.length; i++) {
            offsets[i] = accumulated;
            accumulated += plans[i].width;
            if (i + 1 < plans.length) {
                accumulated += spacing;
            }
        }
        return offsets;
    }

    private static int sanitizeIndex(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Index must be non-negative");
        }
        return value;
    }

    private static int sanitizeSpan(int value) {
        if (value <= 0) {
            return 1;
        }
        return value;
    }

    private static float sanitizeLength(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, value);
    }

    public CellScope cell(int column, int row) {
        return cell(column, row, null);
    }

    public CellScope cell(int column, int row, CellRequest request) {
        ensureOpen();
        int columnIndex = sanitizeIndex(column);
        int rowIndex = sanitizeIndex(row);
        int columnSpan = request != null ? sanitizeSpan(request.columnSpan) : 1;
        int rowSpan = request != null ? sanitizeSpan(request.rowSpan) : 1;
        if (columnIndex < 0 || columnIndex >= columns.length) {
            throw new IllegalArgumentException("Column out of bounds: " + columnIndex);
        }
        int lastColumn = columnIndex + columnSpan - 1;
        if (lastColumn >= columns.length) {
            throw new IllegalArgumentException("Column span exceeds column count");
        }
        ensureRow(rowIndex + rowSpan - 1);
        refreshColumnsIfNeeded();
        float posX = originX + columnOffsets[columnIndex];
        float posY = originY + rowOffset(rowIndex);
        LayoutCursor.moveTo(posX, posY);
        float availableWidth = spanWidth(columnIndex, columnSpan);
        float availableHeight = spanHeight(rowIndex, rowSpan);
        LayoutConstraints layoutConstraints = request != null ? request.constraints : null;
        Constraints direct = layoutConstraints != null ? layoutConstraints.directConstraints() : null;
        LayoutConstraintSolver.LayoutResult planned = null;
        if (direct != null) {
            float contentWidth = layoutConstraints.widthOverrideValue() != null ? layoutConstraints.widthOverrideValue() : 0f;
            float contentHeight = layoutConstraints.heightOverrideValue() != null ? layoutConstraints.heightOverrideValue() : 0f;
            LayoutConstraintSolver.LayoutFrame frame = new LayoutConstraintSolver.LayoutFrame(
                    availableWidth,
                    availableHeight,
                    contentWidth,
                    contentHeight,
                    ConstraintTarget.of(availableWidth, availableHeight)
            );
            planned = LayoutConstraintSolver.resolve(direct, frame);
        }
        float plannedWidth = planned != null ? sanitizeLength(planned.width()) : 0f;
        float plannedHeight = planned != null ? sanitizeLength(planned.height()) : 0f;
        if (plannedWidth <= 0f && layoutConstraints != null && layoutConstraints.widthOverrideValue() != null) {
            plannedWidth = sanitizeLength(layoutConstraints.widthOverrideValue());
        }
        if (plannedHeight <= 0f && layoutConstraints != null && layoutConstraints.heightOverrideValue() != null) {
            plannedHeight = sanitizeLength(layoutConstraints.heightOverrideValue());
        }
        Float estimatedWidth = request != null ? request.estimatedWidth : null;
        Float estimatedHeight = request != null ? request.estimatedHeight : null;
        if (plannedWidth <= 0f && estimatedWidth != null) {
            plannedWidth = sanitizeLength(estimatedWidth);
        }
        if (plannedHeight <= 0f && estimatedHeight != null) {
            plannedHeight = sanitizeLength(estimatedHeight);
        }
        SizeRange widthRange = request != null ? request.widthRange : null;
        SizeRange heightRange = request != null ? request.heightRange : null;
        if (widthRange != null && plannedWidth > 0f) {
            plannedWidth = widthRange.clamp(plannedWidth);
        }
        if (heightRange != null && plannedHeight > 0f) {
            plannedHeight = heightRange.clamp(plannedHeight);
        }
        boolean fillAvailableWidth = request != null && request.fillAvailableWidth;
        if (plannedWidth > 0f) {
            SizeHints.itemWidth(plannedWidth);
        } else if (fillAvailableWidth && availableWidth > 0f) {
            SizeHints.itemWidth(availableWidth);
        }
        ImGui.beginGroup();
        usedColumnBound = Math.max(usedColumnBound, lastColumn + 1);
        usedRowBound = Math.max(usedRowBound, rowIndex + rowSpan);
        return new CellScope(this, columnIndex, rowIndex, columnSpan, rowSpan, availableWidth, availableHeight, plannedWidth, plannedHeight);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        float totalHeight = 0f;
        for (int i = 0; i < usedRowBound; i++) {
            RowState state = rows.get(i);
            totalHeight += state.height;
            if (i + 1 < usedRowBound) {
                totalHeight += rowSpacing;
            }
        }
        LayoutCursor.moveTo(originX, originY + totalHeight);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Grid already closed");
        }
    }

    private void ensureRow(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Row index must be non-negative");
        }
        while (rows.size() <= index) {
            int nextIndex = rows.size();
            RowDefinition definition = nextIndex < rowDefinitions.size() ? rowDefinitions.get(nextIndex) : null;
            rows.add(RowState.fromDefinition(definition));
        }
    }

    private float rowOffset(int index) {
        float offset = 0f;
        for (int i = 0; i < index && i < rows.size(); i++) {
            RowState state = rows.get(i);
            offset += state.height;
            offset += rowSpacing;
        }
        return offset;
    }

    private float spanWidth(int column, int span) {
        float width = 0f;
        for (int i = 0; i < span; i++) {
            width += columns[column + i].width;
            if (i + 1 < span) {
                width += columnSpacing;
            }
        }
        return width;
    }

    private float spanHeight(int row, int span) {
        float height = 0f;
        for (int i = 0; i < span; i++) {
            RowState state = rows.get(row + i);
            height += state.height;
            if (i + 1 < span) {
                height += rowSpacing;
            }
        }
        return height;
    }

    private void onCellClosed(int column, int row, int columnSpan, int rowSpan, float measuredWidth, float measuredHeight) {
        float sanitizedWidth = sanitizeLength(measuredWidth);
        float spacingWidth = columnSpacing * Math.max(columnSpan - 1, 0);
        float contentWidth = Math.max(0f, sanitizedWidth - spacingWidth);
        if (contentWidth > 0f) {
            float share = contentWidth / columnSpan;
            boolean changed = false;
            for (int i = 0; i < columnSpan; i++) {
                ColumnPlan plan = columns[column + i];
                if (plan.definition.type() == ColumnType.FIXED) {
                    continue;
                }
                float candidate = Math.max(plan.width, Math.max(plan.definition.minWidth(), share));
                if (candidate > plan.width) {
                    plan.width = candidate;
                    changed = true;
                }
            }
            if (changed) {
                refreshColumnOffsets();
            }
        }
        float sanitizedHeight = sanitizeLength(measuredHeight);
        float spacingTotal = rowSpacing * Math.max(rowSpan - 1, 0);
        float contentHeight = Math.max(0f, sanitizedHeight - spacingTotal);
        float perRow = contentHeight / rowSpan;
        float remaining = contentHeight;
        int adjustable = 0;
        for (int i = 0; i < rowSpan; i++) {
            RowState state = rows.get(row + i);
            if (!state.fixed) {
                adjustable++;
            }
        }
        for (int i = 0; i < rowSpan; i++) {
            RowState state = rows.get(row + i);
            if (state.fixed) {
                remaining -= state.height;
                continue;
            }
            float candidate = Math.max(state.height, perRow);
            candidate = Math.max(candidate, state.minHeight);
            remaining -= candidate;
            state.height = candidate;
        }
        if (remaining > 0f && adjustable > 0) {
            float bonus = remaining / adjustable;
            for (int i = 0; i < rowSpan; i++) {
                RowState state = rows.get(row + i);
                if (state.fixed) {
                    continue;
                }
                state.height = Math.max(state.height, state.height + bonus);
            }
        }
    }

    private void refreshColumnOffsets() {
        float accumulated = 0f;
        for (int i = 0; i < columns.length; i++) {
            columnOffsets[i] = accumulated;
            accumulated += columns[i].width;
            if (i + 1 < columns.length) {
                accumulated += columnSpacing;
            }
        }
    }

    private void refreshColumnsIfNeeded() {
        float currentAvailable = currentAvailableWidth();
        if (Math.abs(currentAvailable - lastAvailableWidth) <= 0.5f) {
            return;
        }
        assignColumnWidths(columns, currentAvailable, columnSpacing);
        refreshColumnOffsets();
        lastAvailableWidth = currentAvailable;
    }

    private float currentAvailableWidth() {
        LayoutContext context = LayoutContext.capture();
        return Math.max(0f, Math.max(context.contentRegionWidth(), context.contentRegionAvailX()));
    }

    private void onScopeClosed(CellScope scope, float measuredWidth, float measuredHeight) {
        onCellClosed(scope.column, scope.row, scope.columnSpan, scope.rowSpan, measuredWidth, measuredHeight);
    }

    private enum ColumnType {
        FIXED,
        WEIGHT,
        AUTO
    }

    private static final class ColumnPlan {
        private final ColumnDefinition definition;
        private float width;

        private ColumnPlan(ColumnDefinition definition) {
            this.definition = definition;
        }
    }

    private static final class RowState {
        private final boolean fixed;
        private final float minHeight;
        private float height;

        private RowState(boolean fixed, float minHeight, float height) {
            this.fixed = fixed;
            this.minHeight = Math.max(0f, minHeight);
            this.height = Math.max(this.minHeight, height);
        }

        private static RowState fromDefinition(RowDefinition definition) {
            if (definition == null) {
                return new RowState(false, 0f, 0f);
            }
            if (definition.fixed()) {
                return new RowState(true, definition.minHeight(), definition.fixedHeight());
            }
            return new RowState(false, definition.minHeight(), definition.minHeight());
        }
    }

    public static final class Options {
        private ColumnDefinition[] columns;
        private RowDefinition[] rows;
        private LayoutConstraints placement;
        private Float columnSpacing;
        private ScaleUnit columnSpacingUnit = ScaleUnit.RAW;
        private Float rowSpacing;
        private ScaleUnit rowSpacingUnit = ScaleUnit.RAW;

        public Options columns(ColumnDefinition... columns) {
            this.columns = columns != null ? columns.clone() : null;
            return this;
        }

        public Options rows(RowDefinition... rows) {
            this.rows = rows != null ? rows.clone() : null;
            return this;
        }

        public Options placement(LayoutConstraints placement) {
            this.placement = placement;
            return this;
        }

        public Options columnSpacing(float spacing) {
            this.columnSpacing = spacing;
            return this;
        }

        public Options columnSpacing(float spacing, ScaleUnit unit) {
            this.columnSpacing = spacing;
            this.columnSpacingUnit = unit;
            return this;
        }

        public Options rowSpacing(float spacing) {
            this.rowSpacing = spacing;
            return this;
        }

        public Options rowSpacing(float spacing, ScaleUnit unit) {
            this.rowSpacing = spacing;
            this.rowSpacingUnit = unit;
            return this;
        }
    }

    public record ColumnDefinition(ColumnType type, float fixedWidth, float weight, float minWidth) {

        public static ColumnDefinition fixed(float width) {
            return fixed(width, ScaleUnit.RAW);
        }

        public static ColumnDefinition fixed(float width, ScaleUnit unit) {
            ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
            float resolved = sanitizeLength(chosen.applyWidth(width));
            return new ColumnDefinition(ColumnType.FIXED, resolved, 0f, resolved);
        }

        public static ColumnDefinition weight(float weight) {
            float sanitized = sanitizeLength(weight);
            return new ColumnDefinition(ColumnType.WEIGHT, 0f, sanitized, 0f);
        }

        public static ColumnDefinition auto() {
            return new ColumnDefinition(ColumnType.AUTO, 0f, 0f, 0f);
        }

        public static ColumnDefinition auto(float minWidth) {
            return auto(minWidth, ScaleUnit.RAW);
        }

        public static ColumnDefinition auto(float minWidth, ScaleUnit unit) {
            ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
            float resolved = sanitizeLength(chosen.applyWidth(minWidth));
            return new ColumnDefinition(ColumnType.AUTO, 0f, 0f, resolved);
        }
    }

    public record RowDefinition(boolean fixed, float fixedHeight, float minHeight) {

        public static RowDefinition fixed(float height) {
            return fixed(height, ScaleUnit.RAW);
        }

        public static RowDefinition fixed(float height, ScaleUnit unit) {
            ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
            float resolved = sanitizeLength(chosen.applyHeight(height));
            return new RowDefinition(true, resolved, resolved);
        }

        public static RowDefinition auto() {
            return new RowDefinition(false, 0f, 0f);
        }

        public static RowDefinition auto(float minHeight) {
            return auto(minHeight, ScaleUnit.RAW);
        }

        public static RowDefinition auto(float minHeight, ScaleUnit unit) {
            ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
            float resolved = sanitizeLength(chosen.applyHeight(minHeight));
            return new RowDefinition(false, 0f, resolved);
        }
    }

    public static final class CellRequest {
        private LayoutConstraints constraints;
        private SizeRange widthRange;
        private SizeRange heightRange;
        private int columnSpan = 1;
        private int rowSpan = 1;
        private Float estimatedWidth;
        private Float estimatedHeight;
        private boolean fillAvailableWidth;

        public CellRequest constraints(LayoutConstraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public CellRequest widthRange(SizeRange range) {
            this.widthRange = range;
            return this;
        }

        public CellRequest heightRange(SizeRange range) {
            this.heightRange = range;
            return this;
        }

        public CellRequest columnSpan(int span) {
            this.columnSpan = span;
            return this;
        }

        public CellRequest rowSpan(int span) {
            this.rowSpan = span;
            return this;
        }

        public CellRequest estimateWidth(float width) {
            this.estimatedWidth = width;
            return this;
        }

        public CellRequest estimateHeight(float height) {
            this.estimatedHeight = height;
            return this;
        }

        public CellRequest fillWidth(boolean fill) {
            this.fillAvailableWidth = fill;
            return this;
        }
    }

    public static final class CellScope implements AutoCloseable {
        private final GridLayout parent;
        private final int column;
        private final int row;
        private final int columnSpan;
        private final int rowSpan;
        private final float availableWidth;
        private final float availableHeight;
        private final float plannedWidth;
        private final float plannedHeight;
        private boolean closed;

        private CellScope(
                GridLayout parent,
                int column,
                int row,
                int columnSpan,
                int rowSpan,
                float availableWidth,
                float availableHeight,
                float plannedWidth,
                float plannedHeight) {
            this.parent = Objects.requireNonNull(parent, "parent");
            this.column = column;
            this.row = row;
            this.columnSpan = columnSpan;
            this.rowSpan = rowSpan;
            this.availableWidth = availableWidth;
            this.availableHeight = availableHeight;
            this.plannedWidth = plannedWidth;
            this.plannedHeight = plannedHeight;
        }

        public float availableWidth() {
            return availableWidth;
        }

        public float availableHeight() {
            return availableHeight;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            ImGui.endGroup();
            float width = ImGui.getItemRectSizeX();
            float height = ImGui.getItemRectSizeY();
            if ((!Float.isFinite(width) || width <= 0f) && plannedWidth > 0f) {
                width = plannedWidth;
            }
            if ((!Float.isFinite(height) || height <= 0f) && plannedHeight > 0f) {
                height = plannedHeight;
            }
            parent.onScopeClosed(this, width, height);
        }
    }
}
