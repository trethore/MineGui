package tytoo.minegui.helper.constraint.constraints;

import tytoo.minegui.helper.constraint.*;

@SuppressWarnings("unused")
public class Constraints {
    private ConstraintTarget target;

    private XConstraint x = new PixelConstraint(0);
    private YConstraint y = new PixelConstraint(0);
    private WidthConstraint width = new PixelConstraint(0);
    private HeightConstraint height = new PixelConstraint(0);
    private WidthConstraint minWidth;
    private WidthConstraint maxWidth;
    private HeightConstraint minHeight;
    private HeightConstraint maxHeight;

    public Constraints() {
        this(ConstraintTarget.EMPTY);
    }

    public Constraints(ConstraintTarget target) {
        this.target = target != null ? target : ConstraintTarget.EMPTY;
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
        return x.calculateX(target, parentWidth, componentWidth);
    }

    public void setX(XConstraint x) {
        this.x = x;
    }

    public float computeY(float parentHeight, float componentHeight) {
        return y.calculateY(target, parentHeight, componentHeight);
    }

    public void setY(YConstraint y) {
        this.y = y;
    }

    public float computeWidth(float parentWidth) {
        return width.calculateWidth(target, parentWidth);
    }

    public void setWidth(WidthConstraint width) {
        this.width = width;
    }

    public float computeHeight(float parentHeight) {
        return height.calculateHeight(target, parentHeight);
    }

    public void setHeight(HeightConstraint height) {
        this.height = height;
    }

    public WidthConstraint getMinWidthConstraint() {
        return minWidth;
    }

    public void setMinWidth(WidthConstraint minWidth) {
        this.minWidth = minWidth;
    }

    public void setMinWidth(float value) {
        setMinWidth(pixels(value));
    }

    public WidthConstraint getMaxWidthConstraint() {
        return maxWidth;
    }

    public void setMaxWidth(WidthConstraint maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMaxWidth(float value) {
        setMaxWidth(pixels(value));
    }

    public HeightConstraint getMinHeightConstraint() {
        return minHeight;
    }

    public void setMinHeight(HeightConstraint minHeight) {
        this.minHeight = minHeight;
    }

    public void setMinHeight(float value) {
        setMinHeight(pixels(value));
    }

    public HeightConstraint getMaxHeightConstraint() {
        return maxHeight;
    }

    public void setMaxHeight(HeightConstraint maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setMaxHeight(float value) {
        setMaxHeight(pixels(value));
    }

    public float clampWidth(float value, float parentWidth) {
        float normalized = normalizeFinite(value);
        float min = evaluateWidthConstraint(minWidth, parentWidth);
        float max = evaluateWidthConstraint(maxWidth, parentWidth);
        float lowerBound = Float.isFinite(min) ? min : Float.NEGATIVE_INFINITY;
        float upperBound = Float.isFinite(max) ? max : Float.POSITIVE_INFINITY;
        if (lowerBound > upperBound) {
            upperBound = lowerBound;
        }
        normalized = Math.max(normalized, lowerBound);
        normalized = Math.min(normalized, upperBound);
        return normalized;
    }

    public float clampHeight(float value, float parentHeight) {
        float normalized = normalizeFinite(value);
        float min = evaluateHeightConstraint(minHeight, parentHeight);
        float max = evaluateHeightConstraint(maxHeight, parentHeight);
        float lowerBound = Float.isFinite(min) ? min : Float.NEGATIVE_INFINITY;
        float upperBound = Float.isFinite(max) ? max : Float.POSITIVE_INFINITY;
        if (lowerBound > upperBound) {
            upperBound = lowerBound;
        }
        normalized = Math.max(normalized, lowerBound);
        normalized = Math.min(normalized, upperBound);
        return normalized;
    }

    private float evaluateWidthConstraint(WidthConstraint constraint, float parentWidth) {
        if (constraint == null) {
            return Float.NaN;
        }
        float result = constraint.calculateWidth(target, parentWidth);
        return Float.isFinite(result) ? result : Float.NaN;
    }

    private float evaluateHeightConstraint(HeightConstraint constraint, float parentHeight) {
        if (constraint == null) {
            return Float.NaN;
        }
        float result = constraint.calculateHeight(target, parentHeight);
        return Float.isFinite(result) ? result : Float.NaN;
    }

    private float normalizeFinite(float value) {
        if (Float.isFinite(value)) {
            return value;
        }
        return 0f;
    }

    public ConstraintTarget getTarget() {
        return target;
    }

    public void setTarget(ConstraintTarget target) {
        this.target = target != null ? target : ConstraintTarget.EMPTY;
    }

    public void reset() {
        x = new PixelConstraint(0);
        y = new PixelConstraint(0);
        width = new PixelConstraint(0);
        height = new PixelConstraint(0);
        minWidth = null;
        maxWidth = null;
        minHeight = null;
        maxHeight = null;
    }

}
