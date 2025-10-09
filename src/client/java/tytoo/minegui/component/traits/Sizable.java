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

    default T height(HeightConstraint constraint) {
        constraints().setHeight(constraint);
        return self();
    }

    default T size(float width, float height) {
        return width(Constraints.pixels(width))
                .height(Constraints.pixels(height));
    }
}
