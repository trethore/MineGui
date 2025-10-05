package tytoo.minegui.component;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.behavior.Behavior;
import tytoo.minegui.contraint.HeightConstraint;
import tytoo.minegui.contraint.WidthConstraint;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;
import tytoo.minegui.contraint.constraints.Constraints;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MGComponent<T extends MGComponent<T>> {
    protected final Constraints constraints = new Constraints(this);
    protected final List<Behavior<? super T>> behaviors = new ArrayList<>();
    protected List<MGComponent<?>> children = new LinkedList<>();
    protected MGComponent<T> parent;
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

    @SuppressWarnings("unchecked")
    public void render() {
        behaviors.forEach(b -> b.preRender((T) this));
        preRender();
        renderChildren();
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

    public void applyLayoutBeforeDraw() {
        float parentWidth = getParentWidth();
        float parentHeight = getParentHeight();
        float width = constraints.computeWidth(parentWidth);
        float height = constraints.computeHeight(parentHeight);
        float x = constraints.computeX(parentWidth, width);
        float y = constraints.computeY(parentHeight, height);
        setMeasuredSize(width, height);
        ImGui.setCursorPos(x, y);
        ImGui.pushItemWidth(width);
        ImGui.setNextItemWidth(width);
    }

    protected void postRender() {

    }

    public List<MGComponent<?>> getChildren() {
        return this.children;
    }

    public void addChild(MGComponent<?> child) {
        if (child == null) {
            return;
        }
        this.children.add(child);
    }

    public void addChildren(List<MGComponent<?>> children) {
        if (children == null || children.isEmpty()) {
            return;
        }
        this.children.addAll(children);
    }

    public void removeChild(MGComponent<?> child) {
        if (child == null) {
            return;
        }
        this.children.remove(child);
    }

    public void removeAllChildren() {
        this.children.clear();
    }

    @Nullable
    public MGComponent<?> getParent() {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    public T setParent(@Nullable MGComponent<?> parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = (MGComponent<T>) parent;
        if (this.parent != null) {
            this.parent.addChild(this);
        }
        return self();
    }

    public T parent(@Nullable MGComponent<?> parent) {
        return setParent(parent);
    }

    public T goFirst() {
        if (this.parent != null) {
            this.parent.children.remove(this);
            this.parent.children.addLast(this);
        }
        return self();
    }

    @SuppressWarnings("unchecked")
    public T behavior(Behavior<? super T> behavior) {
        behaviors.add(behavior);
        behavior.onAttach((T) this);
        return self();
    }

    public T x(XConstraint constraint) {
        constraints().setX(constraint);
        return self();
    }

    public T y(YConstraint constraint) {
        constraints().setY(constraint);
        return self();
    }

    public T width(WidthConstraint constraint) {
        constraints().setWidth(constraint);
        return self();
    }

    public T height(HeightConstraint constraint) {
        constraints().setHeight(constraint);
        return self();
    }

    public T size(float width, float height) {
        return width(Constraints.pixels(width))
                .height(Constraints.pixels(height));
    }

    public T pos(float x, float y) {
        return x(Constraints.pixels(x))
                .y(Constraints.pixels(y));
    }

    public T center() {
        return x(Constraints.center())
                .y(Constraints.center());
    }

}