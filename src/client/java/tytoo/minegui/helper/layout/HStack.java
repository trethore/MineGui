package tytoo.minegui.helper.layout;

import imgui.ImGui;
import imgui.ImGuiStyle;
import tytoo.minegui.helper.layout.cursor.LayoutContext;
import tytoo.minegui.helper.layout.cursor.LayoutCursor;
import tytoo.minegui.helper.layout.sizing.ScaleUnit;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.layout.sizing.SizeRange;

import java.util.Objects;

public final class HStack implements AutoCloseable {
    private final LayoutContext context;
    private final float originX;
    private final float originY;
    private final float spacing;
    private final Alignment alignment;
    private final boolean equalizeHeight;
    private final float uniformHeight;
    private float cursorOffsetX;
    private float lineHeight;
    private boolean hasItems;
    private boolean closed;

    private HStack(Options options, LayoutContext context) {
        Options resolved = options != null ? options : new Options();
        LayoutContext chosenContext = context != null ? context : LayoutContext.capture();
        if (resolved.placement != null) {
            LayoutCursor.moveTo(resolved.placement, chosenContext);
        }
        this.context = chosenContext;
        this.originX = ImGui.getCursorPosX();
        this.originY = ImGui.getCursorPosY();
        this.spacing = resolveSpacing(resolved);
        this.alignment = resolved.alignment != null ? resolved.alignment : Alignment.TOP;
        this.equalizeHeight = resolved.equalizeHeight;
        this.uniformHeight = StackMetrics.sanitizeLength(resolved.uniformHeight);
        this.cursorOffsetX = 0f;
        this.lineHeight = this.uniformHeight;
    }

    public static HStack begin() {
        return begin(new Options(), null);
    }

    public static HStack begin(Options options) {
        return begin(options, null);
    }

    public static HStack begin(Options options, LayoutContext context) {
        return new HStack(options, context);
    }

    private static float resolveSpacing(Options options) {
        Float configured = options.spacing;
        ScaleUnit unit = options.spacingUnit != null ? options.spacingUnit : ScaleUnit.RAW;
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

    public ItemScope next() {
        return next(new ItemRequest());
    }

    public ItemScope next(ItemRequest request) {
        ensureOpen();
        prepareNextItem();
        ItemRequest resolved = request != null ? request : new ItemRequest();
        SizeHints.NextSize plannedSize = null;
        if (resolved.useSizeHints && resolved.constraints != null) {
            plannedSize = SizeHints.itemSize(resolved.constraints, resolved.widthRange, resolved.heightRange, context);
        }
        float plannedWidth = plannedSize != null ? StackMetrics.sanitizeLength(plannedSize.width()) : 0f;
        float plannedHeight = plannedSize != null ? StackMetrics.sanitizeLength(plannedSize.height()) : 0f;
        if (plannedWidth <= 0f && resolved.estimatedWidth != null) {
            plannedWidth = StackMetrics.sanitizeLength(resolved.estimatedWidth);
        }
        if (plannedHeight <= 0f && resolved.estimatedHeight != null) {
            plannedHeight = StackMetrics.sanitizeLength(resolved.estimatedHeight);
        }
        boolean widthApplied = plannedSize != null && plannedSize.width() > 0f;
        if (resolved.fillAvailableWidth) {
            ImGui.setNextItemWidth(-1f);
            widthApplied = true;
        }
        if (!widthApplied && plannedWidth > 0f) {
            SizeHints.itemWidth(plannedWidth);
        }
        float targetHeight = computeTargetHeight(plannedHeight);
        float offsetY = computeAlignmentOffset(plannedHeight, targetHeight);
        LayoutCursor.moveTo(originX + cursorOffsetX, originY + offsetY);
        ImGui.beginGroup();
        return new ItemScope(this, plannedWidth, plannedHeight, offsetY);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (!hasItems) {
            return;
        }
        float finalHeight = Math.max(lineHeight, uniformHeight);
        LayoutCursor.moveTo(originX, originY + finalHeight);
    }

    private void prepareNextItem() {
        if (hasItems && spacing > 0f) {
            cursorOffsetX += spacing;
        }
    }

    private void onItemClosed(float width, float height, float offsetY) {
        float resolvedWidth = StackMetrics.sanitizeLength(width);
        float resolvedHeight = StackMetrics.sanitizeLength(height);
        cursorOffsetX += resolvedWidth;
        float baseline = Math.max(uniformHeight, lineHeight);
        float extent = Math.max(resolvedHeight + offsetY, resolvedHeight);
        float candidate = Math.max(baseline, extent);
        lineHeight = equalizeHeight ? Math.max(candidate, resolvedHeight) : candidate;
        lineHeight = Math.max(lineHeight, uniformHeight);
        hasItems = true;
    }

    private float computeTargetHeight(float plannedHeight) {
        float sanitized = StackMetrics.sanitizeLength(plannedHeight);
        float candidate = Math.max(lineHeight, sanitized);
        candidate = Math.max(candidate, uniformHeight);
        return candidate;
    }

    private float computeAlignmentOffset(float plannedHeight, float targetHeight) {
        float sanitizedHeight = StackMetrics.sanitizeLength(plannedHeight);
        float sanitizedTarget = Math.max(StackMetrics.sanitizeLength(targetHeight), sanitizedHeight);
        return switch (alignment) {
            case TOP -> 0f;
            case CENTER -> Math.max(0f, (sanitizedTarget - sanitizedHeight) * 0.5f);
            case BOTTOM -> Math.max(0f, sanitizedTarget - sanitizedHeight);
        };
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Stack already closed");
        }
    }

    public enum Alignment {
        TOP,
        CENTER,
        BOTTOM
    }

    public static final class Options {
        private LayoutConstraints placement;
        private Float spacing;
        private ScaleUnit spacingUnit = ScaleUnit.RAW;
        private Alignment alignment = Alignment.TOP;
        private boolean equalizeHeight;
        private Float uniformHeight;

        public Options placement(LayoutConstraints placement) {
            this.placement = placement;
            return this;
        }

        public Options spacing(float spacing) {
            this.spacing = spacing;
            return this;
        }

        public Options spacing(float spacing, ScaleUnit unit) {
            this.spacing = spacing;
            this.spacingUnit = unit;
            return this;
        }

        public Options alignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Options equalizeHeight(boolean equalizeHeight) {
            this.equalizeHeight = equalizeHeight;
            return this;
        }

        public Options uniformHeight(float uniformHeight) {
            this.uniformHeight = uniformHeight;
            return this;
        }
    }

    public static final class ItemRequest {
        private LayoutConstraints constraints;
        private SizeRange widthRange;
        private SizeRange heightRange;
        private boolean useSizeHints = true;
        private Float estimatedWidth;
        private Float estimatedHeight;
        private boolean fillAvailableWidth;

        public ItemRequest constraints(LayoutConstraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public ItemRequest widthRange(SizeRange range) {
            this.widthRange = range;
            return this;
        }

        public ItemRequest heightRange(SizeRange range) {
            this.heightRange = range;
            return this;
        }

        public ItemRequest useSizeHints(boolean useSizeHints) {
            this.useSizeHints = useSizeHints;
            return this;
        }

        public ItemRequest estimateWidth(float width) {
            this.estimatedWidth = width;
            return this;
        }

        public ItemRequest estimateHeight(float height) {
            this.estimatedHeight = height;
            return this;
        }

        public ItemRequest fillWidth(boolean fill) {
            this.fillAvailableWidth = fill;
            return this;
        }
    }

    public static final class ItemScope implements AutoCloseable {
        private final HStack parent;
        private final float plannedWidth;
        private final float plannedHeight;
        private final float offsetY;
        private boolean closed;

        private ItemScope(HStack parent, float plannedWidth, float plannedHeight, float offsetY) {
            this.parent = Objects.requireNonNull(parent, "parent");
            this.plannedWidth = plannedWidth;
            this.plannedHeight = plannedHeight;
            this.offsetY = offsetY;
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
            parent.onItemClosed(width, height, offsetY);
        }
    }
}
