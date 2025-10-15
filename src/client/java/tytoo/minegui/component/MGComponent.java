package tytoo.minegui.component;

import imgui.ImGui;
import tytoo.minegui.component.behavior.Behavior;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MGComponent<T extends MGComponent<T>> {
    protected final Constraints constraints = new Constraints(this);
    protected final List<Behavior<? super T>> behaviors = new ArrayList<>();
    protected float measuredWidth;
    protected float measuredHeight;
    private Runnable afterRenderHook;

    protected MGComponent() {
        measuredWidth = 0f;
        measuredHeight = 0f;
        afterRenderHook = null;
    }

    protected void resetRootState() {
        detachBehaviors();
        constraints.reset();
        measuredWidth = 0f;
        measuredHeight = 0f;
    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    public float getMeasuredWidth() {
        return measuredWidth;
    }

    public float getMeasuredHeight() {
        return measuredHeight;
    }

    protected void setMeasuredSize(float width, float height) {
        measuredWidth = width;
        measuredHeight = height;
    }

    public void render() {
        beginRenderLifecycle();
        try {
            renderComponent();
        } finally {
            endRenderLifecycle();
            afterRender();
        }
    }

    @SuppressWarnings("unchecked")
    protected final void beginRenderLifecycle() {
        behaviors.forEach(b -> b.preRender((T) this));
        preRender();
    }

    @SuppressWarnings("unchecked")
    protected final void endRenderLifecycle() {
        postRender();
        behaviors.forEach(b -> b.postRender((T) this));
    }

    protected void renderComponent() {
    }

    protected void preRender() {
    }

    protected void postRender() {
    }

    protected void renderChildren() {
    }

    protected float getParentWidth() {
        return ImGui.getContentRegionAvailX();
    }

    protected float getParentHeight() {
        return ImGui.getContentRegionAvailY();
    }

    public Constraints constraints() {
        return constraints;
    }

    protected final LayoutScope applyLayoutBeforeDraw(float preferredWidth, float preferredHeight) {
        float parentWidth = Math.max(0f, getParentWidth());
        float parentHeight = Math.max(0f, getParentHeight());
        float baselineWidth = normalizePreferred(preferredWidth);
        float baselineHeight = normalizePreferred(preferredHeight);

        float requestedWidth = constraints.computeWidth(parentWidth);
        float requestedHeight = constraints.computeHeight(parentHeight);

        float width = resolveSize(requestedWidth, baselineWidth, parentWidth);
        float height = resolveSize(requestedHeight, baselineHeight, parentHeight);

        if (constraints.getHeightConstraint() instanceof AspectRatioConstraint(float ratio)) {
            if (ratio > 0f) {
                float target = width / ratio;
                height = resolveSize(target, baselineHeight, parentHeight);
            }
        }

        if (constraints.getWidthConstraint() instanceof AspectRatioConstraint(float ratio)) {
            if (ratio > 0f) {
                float target = height * ratio;
                width = resolveSize(target, baselineWidth, parentWidth);
            }
        }

        setMeasuredSize(width, height);
        float cursorX = constraints.computeX(parentWidth, width);
        float cursorY = constraints.computeY(parentHeight, height);
        ImGui.setCursorPos(cursorX, cursorY);
        boolean pushedWidth = width > 0f;
        if (pushedWidth) {
            ImGui.pushItemWidth(width);
            ImGui.setNextItemWidth(width);
        }
        return new LayoutScope(width, height, pushedWidth);
    }

    protected final LayoutScope applyLayoutBeforeDraw() {
        return applyLayoutBeforeDraw(measuredWidth, measuredHeight);
    }

    protected final void withLayout(float preferredWidth, float preferredHeight, LayoutConsumer consumer) {
        try (LayoutScope scope = applyLayoutBeforeDraw(preferredWidth, preferredHeight)) {
            if (consumer != null) {
                consumer.accept(scope.width(), scope.height());
            }
        }
    }

    protected final void withLayout(LayoutConsumer consumer) {
        try (LayoutScope scope = applyLayoutBeforeDraw()) {
            if (consumer != null) {
                consumer.accept(scope.width(), scope.height());
            }
        }
    }

    public T x(XConstraint constraint) {
        constraints.setX(constraint);
        return self();
    }

    public T y(YConstraint constraint) {
        constraints.setY(constraint);
        return self();
    }

    public T pos(float x, float y) {
        return x(Constraints.pixels(x)).y(Constraints.pixels(y));
    }

    public T center() {
        return x(Constraints.center()).y(Constraints.center());
    }

    @SuppressWarnings("unchecked")
    public T behavior(Behavior<? super T> behavior) {
        behaviors.add(behavior);
        behavior.onAttach((T) this);
        return self();
    }

    private float normalizePreferred(float preferred) {
        if (!Float.isFinite(preferred) || preferred <= 0f) {
            return 1f;
        }
        return preferred;
    }

    private float resolveSize(float requested, float fallback, float parentExtent) {
        if (Float.isFinite(requested) && requested > 0f) {
            return requested;
        }
        if (Float.isFinite(fallback) && fallback > 0f) {
            return fallback;
        }
        if (Float.isFinite(parentExtent) && parentExtent > 0f) {
            return parentExtent;
        }
        return 1f;
    }

    @SuppressWarnings("unchecked")
    protected final void detachBehaviors() {
        if (behaviors.isEmpty()) {
            return;
        }
        List<Behavior<? super T>> snapshot = List.copyOf(behaviors);
        behaviors.clear();
        for (Behavior<? super T> behavior : snapshot) {
            behavior.onDetach((T) this);
        }
    }

    protected void afterRender() {
        if (afterRenderHook != null) {
            afterRenderHook.run();
            afterRenderHook = null;
        }
    }

    protected final void setAfterRenderHook(Runnable hook) {
        afterRenderHook = hook;
    }

    @FunctionalInterface
    protected interface LayoutConsumer {
        void accept(float width, float height);
    }

    protected static final class LayoutScope implements AutoCloseable {
        private final float width;
        private final float height;
        private final boolean popWidth;
        private boolean closed;

        private LayoutScope(float width, float height, boolean popWidth) {
            this.width = width;
            this.height = height;
            this.popWidth = popWidth;
            this.closed = false;
        }

        public float width() {
            return width;
        }

        public float height() {
            return height;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            if (popWidth) {
                ImGui.popItemWidth();
            }
            closed = true;
        }
    }
}
