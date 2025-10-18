package tytoo.minegui.component.traits;

import tytoo.minegui.contraint.HeightConstraint;
import tytoo.minegui.contraint.WidthConstraint;
import tytoo.minegui.contraint.constraints.Constraints;

public interface Sizable<T extends Sizable<T>> {
    Constraints constraints();

    T self();

    default T width(WidthConstraint constraint) {
        constraints().setWidth(constraint);
        return self();
    }

    default T width(float value) {
        return width(Constraints.pixels(value));
    }

    default T height(HeightConstraint constraint) {
        constraints().setHeight(constraint);
        return self();
    }

    default T height(float value) {
        return height(Constraints.pixels(value));
    }

    default T minWidth(WidthConstraint constraint) {
        constraints().setMinWidth(constraint);
        return self();
    }

    default T minWidth(float value) {
        return minWidth(Constraints.pixels(value));
    }

    default T maxWidth(WidthConstraint constraint) {
        constraints().setMaxWidth(constraint);
        return self();
    }

    default T maxWidth(float value) {
        return maxWidth(Constraints.pixels(value));
    }

    default T minHeight(HeightConstraint constraint) {
        constraints().setMinHeight(constraint);
        return self();
    }

    default T minHeight(float value) {
        return minHeight(Constraints.pixels(value));
    }

    default T maxHeight(HeightConstraint constraint) {
        constraints().setMaxHeight(constraint);
        return self();
    }

    default T maxHeight(float value) {
        return maxHeight(Constraints.pixels(value));
    }

    default T size(float width, float height) {
        return width(Constraints.pixels(width))
                .height(Constraints.pixels(height));
    }

    default T dimensions(WidthConstraint width, HeightConstraint height) {
        return width(width).height(height);
    }

    default T dimensions(float width, float height) {
        return width(width).height(height);
    }
}
