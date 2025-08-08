package tytoo.minegui.component;

import imgui.ImGui;
import tytoo.minegui.contraint.constraints.Constraints;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MGComponent<T extends MGComponent<T>> {
    protected final Constraints constraints = new Constraints(this);
    protected List<MGComponent<?>> children = new LinkedList<>();
    protected MGComponent<T> parent;
    protected float measuredWidth = 0f;
    protected float measuredHeight = 0f;

    public MGComponent() {

    }

    @SuppressWarnings("unchecked")
    protected T self() {
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
        preRender();
        renderChildren();
        postRender();
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

    public MGComponent<?> getParent() {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    public void setParent(MGComponent<?> parent) {
        this.parent = (MGComponent<T>) parent;
        this.parent.addChild(this);
    }

    public T goFirst() {
        if (this.parent != null) {
            this.parent.children.remove(this);
            this.parent.children.addLast(this);
        }
        return self();
    }

}