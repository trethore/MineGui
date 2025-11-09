package tytoo.minegui.helper.layout;

import imgui.ImGui;
import imgui.ImGuiStyle;
import tytoo.minegui.helper.layout.cursor.LayoutContext;
import tytoo.minegui.helper.layout.cursor.LayoutCursor;
import tytoo.minegui.helper.layout.sizing.ScaleUnit;
import tytoo.minegui.helper.layout.sizing.SizeHints;
import tytoo.minegui.helper.layout.sizing.SizeRange;

import java.util.Objects;

public final class VStack implements AutoCloseable {
    private final LayoutContext context;
    private final float originX;
    private final float originY;
    private final float spacing;
    private final FillMode fillMode;
    private final float uniformWidth;
    private float cursorOffsetY;
    private float stackWidth;
    private float widestWidth;
    private boolean hasItems;
    private boolean closed;

    private VStack(Options options, LayoutContext context) {
        Options resolved = options != null ? options : new Options();
        LayoutContext chosenContext = context != null ? context : LayoutContext.capture();
        if (resolved.placement != null) {
            LayoutCursor.moveTo(resolved.placement, chosenContext);
        }
        this.context = chosenContext;
        this.originX = ImGui.getCursorPosX();
        this.originY = ImGui.getCursorPosY();
        this.spacing = resolveSpacing(resolved);
        this.fillMode = resolved.fillMode != null ? resolved.fillMode : FillMode.NONE;
        this.uniformWidth = sanitizeLength(resolved.uniformWidth);
        this.cursorOffsetY = 0f;
        this.stackWidth = 0f;
        this.widestWidth = this.uniformWidth;
    }

    public static VStack begin() {
        return begin(new Options(), null);
    }

    public static VStack begin(Options options) {
        return begin(options, null);
    }

    public static VStack begin(Options options, LayoutContext context) {
        return new VStack(options, context);
    }

    private static float resolveSpacing(Options options) {
        Float configured = options.spacing;
        ScaleUnit unit = options.spacingUnit != null ? options.spacingUnit : ScaleUnit.RAW;
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

    private static float sanitizeLength(Float value) {
        if (value == null) {
            return 0f;
        }
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, value);
    }

    private static float sanitizeLength(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, value);
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
        float plannedWidth = plannedSize != null ? sanitizeLength(plannedSize.width()) : 0f;
        float plannedHeight = plannedSize != null ? sanitizeLength(plannedSize.height()) : 0f;
        if (plannedWidth <= 0f && resolved.estimatedWidth != null) {
            plannedWidth = sanitizeLength(resolved.estimatedWidth);
        }
        if (plannedHeight <= 0f && resolved.estimatedHeight != null) {
            plannedHeight = sanitizeLength(resolved.estimatedHeight);
        }
        boolean widthApplied = plannedSize != null && plannedSize.width() > 0f;
        if (resolved.fillAvailableWidth) {
            ImGui.setNextItemWidth(-1f);
            widthApplied = true;
        } else if (uniformWidth > 0f) {
            SizeHints.itemWidth(uniformWidth);
            widthApplied = true;
            plannedWidth = Math.max(plannedWidth, uniformWidth);
        } else if (!widthApplied) {
            if (fillMode == FillMode.AVAILABLE) {
                ImGui.setNextItemWidth(-1f);
                widthApplied = true;
            } else if (fillMode == FillMode.MATCH_WIDEST && widestWidth > 0f) {
                SizeHints.itemWidth(widestWidth);
                widthApplied = true;
                plannedWidth = Math.max(plannedWidth, widestWidth);
            }
        }
        if (!widthApplied && plannedWidth > 0f) {
            SizeHints.itemWidth(plannedWidth);
        }
        LayoutCursor.moveTo(originX, originY + cursorOffsetY);
        ImGui.beginGroup();
        return new ItemScope(this, plannedWidth, plannedHeight);
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
        LayoutCursor.moveTo(originX, originY + cursorOffsetY);
    }

    private void prepareNextItem() {
        if (hasItems && spacing > 0f) {
            cursorOffsetY += spacing;
        }
    }

    private void onItemClosed(float width, float height) {
        float resolvedWidth = sanitizeLength(width);
        float resolvedHeight = sanitizeLength(height);
        cursorOffsetY += resolvedHeight;
        stackWidth = Math.max(stackWidth, resolvedWidth);
        widestWidth = Math.max(widestWidth, resolvedWidth);
        if (uniformWidth > 0f) {
            widestWidth = Math.max(widestWidth, uniformWidth);
        }
        hasItems = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Stack already closed");
        }
    }

    public enum FillMode {
        NONE,
        AVAILABLE,
        MATCH_WIDEST
    }

    public static final class Options {
        private LayoutConstraints placement;
        private Float spacing;
        private ScaleUnit spacingUnit = ScaleUnit.RAW;
        private FillMode fillMode = FillMode.NONE;
        private Float uniformWidth;

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

        public Options fillMode(FillMode fillMode) {
            this.fillMode = fillMode;
            return this;
        }

        public Options uniformWidth(float uniformWidth) {
            this.uniformWidth = uniformWidth;
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
        private final VStack parent;
        private final float plannedWidth;
        private final float plannedHeight;
        private boolean closed;

        private ItemScope(VStack parent, float plannedWidth, float plannedHeight) {
            this.parent = Objects.requireNonNull(parent, "parent");
            this.plannedWidth = plannedWidth;
            this.plannedHeight = plannedHeight;
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
            parent.onItemClosed(width, height);
        }
    }
}
