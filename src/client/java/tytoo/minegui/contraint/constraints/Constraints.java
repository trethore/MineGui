package tytoo.minegui.contraint.constraints;

import tytoo.minegui.component.MGComponent;
import tytoo.minegui.contraint.HeightConstraint;
import tytoo.minegui.contraint.WidthConstraint;
import tytoo.minegui.contraint.XConstraint;
import tytoo.minegui.contraint.YConstraint;

@SuppressWarnings("unused")
public class Constraints {
    private final MGComponent<?> component;

    private XConstraint x = new PixelConstraint(0);
    private YConstraint y = new PixelConstraint(0);
    private WidthConstraint width = new PixelConstraint(0);
    private HeightConstraint height = new PixelConstraint(0);

    public Constraints(MGComponent<?> component) {
        this.component = component;
    }

    public static CenterConstraint center() {
        return new CenterConstraint();
    }

    public static PixelConstraint pixels(float value) {
        return new PixelConstraint(value);
    }

    public static AspectRatioConstraint aspect(float ratio) {
        return new AspectRatioConstraint(ratio);
    }

    public static RelativeConstraint relative(float value) {
        return new RelativeConstraint(value, 0f);
    }

    public static RelativeConstraint relative(float value, float offset) {
        return new RelativeConstraint(value, offset);
    }

    public XConstraint getXConstraint() {
        return x;
    }

    public YConstraint getYConstraint() {
        return y;
    }

    public WidthConstraint getWidthConstraint() {
        return width;
    }

    public HeightConstraint getHeightConstraint() {
        return height;
    }

    public float computeX(float parentWidth, float componentWidth) {
        return x.calculateX(component, parentWidth, componentWidth);
    }

    public void setX(XConstraint x) {
        this.x = x;
    }

    public float computeY(float parentHeight, float componentHeight) {
        return y.calculateY(component, parentHeight, componentHeight);
    }

    public void setY(YConstraint y) {
        this.y = y;
    }

    public float computeWidth(float parentWidth) {
        return width.calculateWidth(component, parentWidth);
    }

    public void setWidth(WidthConstraint width) {
        this.width = width;
    }

    public float computeHeight(float parentHeight) {
        return height.calculateHeight(component, parentHeight);
    }

    public void setHeight(HeightConstraint height) {
        this.height = height;
    }

    public MGComponent<?> getComponent() {
        return component;
    }

}
