package tytoo.minegui.component;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.behavior.Behavior;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;
import tytoo.minegui.contraint.constraints.AspectRatioConstraint;
import tytoo.minegui.contraint.constraints.Constraints;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class MGComponent<T extends MGComponent<T>> {
    protected final Constraints constraints = new Constraints(this);
    protected final List<Behavior<? super T>> behaviors = new ArrayList<>();
    protected final Deque<MGComponent<?>> children = new LinkedList<>();
    protected MGComponent<?> parent;
    protected float measuredWidth = 0f;
    protected float measuredHeight = 0f;

    public MGComponent() {

    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    public float getMeasuredWidth() {
        return this.measuredWidth;
    }

    public float getMeasuredHeight() {
        return this.measuredHeight;
    }

    protected void setMeasuredSize(float width, float height) {
        this.measuredWidth = width;
        this.measuredHeight = height;
    }

    public void render() {
        beginRenderLifecycle();
        renderChildren();
        endRenderLifecycle();
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

    public void renderChildren() {
        for (MGComponent<?> child : children) {
            child.render();
        }
    }

    protected void preRender() {

    }

    protected float getParentWidth() {
        return ImGui.getContentRegionAvailX();
    }

    protected float getParentHeight() {
        return ImGui.getContentRegionAvailY();
    }

    public Constraints constraints() {
        return this.constraints;
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
        return applyLayoutBeforeDraw(this.measuredWidth, this.measuredHeight);
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

    protected void postRender() {

    }

    public List<MGComponent<?>> getChildren() {
        return List.copyOf(this.children);
    }

    public void forEachChild(Consumer<? super MGComponent<?>> consumer) {
        if (consumer == null) {
            return;
        }
        for (MGComponent<?> child : this.children) {
            consumer.accept(child);
        }
    }

    public void addChild(MGComponent<?> child) {
        if (child == null) {
            return;
        }
        if (child == this) {
            return;
        }
        if (child.parent == this) {
            if (!this.children.contains(child)) {
                this.children.addLast(child);
            }
        } else {
            if (child.parent != null) {
                child.parent.removeChild(child);
            }
            this.children.remove(child);
            this.children.addLast(child);
            child.parent = this;
        }
    }

    public void addChildren(List<MGComponent<?>> children) {
        if (children == null || children.isEmpty()) {
            return;
        }
        for (MGComponent<?> child : children) {
            addChild(child);
        }
    }

    public void removeChild(MGComponent<?> child) {
        if (child == null) {
            return;
        }
        this.children.remove(child);
        if (child.parent == this) {
            child.parent = null;
        }
    }

    public void removeAllChildren() {
        for (MGComponent<?> child : List.copyOf(this.children)) {
            removeChild(child);
        }
    }

    @Nullable
    public MGComponent<?> getParent() {
        return this.parent;
    }

    public T setParent(@Nullable MGComponent<?> parent) {
        if (this.parent == parent) {
            return self();
        }
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        if (parent != null) {
            parent.addChild(this);
        }
        return self();
    }

    public T parent(@Nullable MGComponent<?> parent) {
        return setParent(parent);
    }

    public T goFirst() {
        if (this.parent != null) {
            this.parent.children.remove(this);
            this.parent.children.addFirst(this);
        }
        return self();
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
