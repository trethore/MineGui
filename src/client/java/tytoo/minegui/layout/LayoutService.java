package tytoo.minegui.layout;

import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.Margin;
import tytoo.minegui.helper.layout.VStack;
import tytoo.minegui.helper.layout.experimental.GridLayout;

import java.util.function.*;

public final class LayoutService implements LayoutApi {
    @Override
    public StackLayoutBuilder vertical() {
        return new StackLayoutBuilder(LayoutNodes.StackOrientation.VERTICAL);
    }

    @Override
    public StackLayoutBuilder row() {
        return new StackLayoutBuilder(LayoutNodes.StackOrientation.HORIZONTAL);
    }

    @Override
    public GridLayoutBuilder grid() {
        return new GridLayoutBuilder();
    }

    @Override
    public void render(LayoutTemplate template) {
        render(template, null);
    }

    @Override
    public void render(LayoutTemplate template, Runnable body) {
        if (template != null) {
            renderNode(template.root());
        }
        if (body != null) {
            body.run();
        }
    }

    private void renderNode(LayoutNodes.LayoutNode node) {
        if (node instanceof LayoutNodes.StackNode stackNode) {
            renderStack(stackNode);
            return;
        }
        if (node instanceof LayoutNodes.GridNode(LayoutNodes.GridDefinition definition)) {
            renderGrid(definition);
            return;
        }
        if (node instanceof LayoutNodes.RenderNode(LayoutRenderable renderable)) {
            renderNode(renderable);
        }
    }

    private void renderNode(LayoutRenderable renderable) {
        if (renderable == null) {
            return;
        }
        renderable.render();
    }

    private void renderStack(LayoutNodes.StackNode node) {
        Margin.Scope scope = LayoutNodes.applyPadding(node.padding());
        try {
            if (node.orientation() == LayoutNodes.StackOrientation.VERTICAL) {
                renderVerticalStack(node);
            } else {
                renderHorizontalStack(node);
            }
        } finally {
            LayoutNodes.close(scope);
        }
    }

    private void renderVerticalStack(LayoutNodes.StackNode node) {
        VStack.Options options = new VStack.Options();
        if (node.spacing() != null) {
            options.spacing(node.spacing());
        }
        if (node.fillMode() != null) {
            options.fillMode(node.fillMode());
        }
        if (node.uniformWidth() != null) {
            options.uniformWidth(node.uniformWidth());
        }
        try (VStack stack = VStack.begin(options)) {
            renderStackChildren(
                    node.children(),
                    this::createVStackRequest,
                    VStack::next,
                    VStack::next,
                    stack
            );
        }
    }

    private void renderHorizontalStack(LayoutNodes.StackNode node) {
        HStack.Options options = new HStack.Options();
        if (node.spacing() != null) {
            options.spacing(node.spacing());
        }
        if (node.alignment() != null) {
            options.alignment(node.alignment());
        }
        if (node.equalizeHeight() != null) {
            options.equalizeHeight(node.equalizeHeight());
        }
        if (node.uniformHeight() != null) {
            options.uniformHeight(node.uniformHeight());
        }
        try (HStack stack = HStack.begin(options)) {
            renderStackChildren(
                    node.children(),
                    this::createHStackRequest,
                    HStack::next,
                    HStack::next,
                    stack
            );
        }
    }

    private void renderGrid(LayoutNodes.GridDefinition definition) {
        GridLayout.Options options = new GridLayout.Options();
        if (definition.columns() != null) {
            options.columns(definition.columns());
        }
        if (definition.rows() != null) {
            options.rows(definition.rows());
        }
        if (definition.columnSpacing() != null) {
            options.columnSpacing(definition.columnSpacing());
        }
        if (definition.rowSpacing() != null) {
            options.rowSpacing(definition.rowSpacing());
        }
        try (GridLayout layout = GridLayout.begin(options)) {
            for (LayoutNodes.GridCell cell : definition.cells()) {
                GridLayout.CellRequest request = createGridCellRequest(cell);
                if (request == null) {
                    try (GridLayout.CellScope ignored = layout.cell(cell.column(), cell.row())) {
                        renderNode(cell.node());
                    }
                    continue;
                }
                try (GridLayout.CellScope ignored = layout.cell(cell.column(), cell.row(), request)) {
                    renderNode(cell.node());
                }
            }
        }
    }

    private VStack.ItemRequest createVStackRequest(LayoutNodes.LayoutSlot slot) {
        return createStackRequest(
                slot,
                VStack.ItemRequest::new,
                request -> request.fillWidth(true),
                VStack.ItemRequest::estimateWidth,
                VStack.ItemRequest::estimateHeight
        );
    }

    private HStack.ItemRequest createHStackRequest(LayoutNodes.LayoutSlot slot) {
        return createStackRequest(
                slot,
                HStack.ItemRequest::new,
                request -> request.fillWidth(true),
                HStack.ItemRequest::estimateWidth,
                HStack.ItemRequest::estimateHeight
        );
    }

    private <R> R createStackRequest(
            LayoutNodes.LayoutSlot slot,
            Supplier<R> factory,
            Consumer<R> fillWidth,
            BiConsumer<R, Float> estimateWidth,
            BiConsumer<R, Float> estimateHeight
    ) {
        boolean needsRequest = slot.estimatedWidth() != null
                || slot.estimatedHeight() != null
                || slot.fillWidth();
        if (!needsRequest) {
            return null;
        }
        R request = factory.get();
        if (slot.fillWidth()) {
            fillWidth.accept(request);
        }
        if (slot.estimatedWidth() != null) {
            estimateWidth.accept(request, slot.estimatedWidth());
        }
        if (slot.estimatedHeight() != null) {
            estimateHeight.accept(request, slot.estimatedHeight());
        }
        return request;
    }

    private GridLayout.CellRequest createGridCellRequest(LayoutNodes.GridCell cell) {
        boolean needsRequest = cell.columnSpan() != 1
                || cell.rowSpan() != 1
                || cell.estimatedWidth() != null
                || cell.estimatedHeight() != null
                || cell.fillWidth();
        if (!needsRequest) {
            return null;
        }
        GridLayout.CellRequest request = new GridLayout.CellRequest()
                .columnSpan(cell.columnSpan())
                .rowSpan(cell.rowSpan());
        if (cell.estimatedWidth() != null) {
            request.estimateWidth(cell.estimatedWidth());
        }
        if (cell.estimatedHeight() != null) {
            request.estimateHeight(cell.estimatedHeight());
        }
        if (cell.fillWidth()) {
            request.fillWidth(true);
        }
        return request;
    }

    private <S, Scope extends AutoCloseable, R> void renderStackChildren(
            Iterable<LayoutNodes.LayoutSlot> slots,
            Function<LayoutNodes.LayoutSlot, R> requestFactory,
            Function<S, Scope> withoutRequest,
            BiFunction<S, R, Scope> withRequest,
            S stack
    ) {
        for (LayoutNodes.LayoutSlot slot : slots) {
            R request = requestFactory.apply(slot);
            Scope scope = request == null ? withoutRequest.apply(stack) : withRequest.apply(stack, request);
            try {
                renderNode(slot.node());
            } finally {
                LayoutNodes.close(scope);
            }
        }
    }
}
