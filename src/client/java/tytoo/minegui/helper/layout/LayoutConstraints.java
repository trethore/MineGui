package tytoo.minegui.helper.layout;

import tytoo.minegui.helper.constraint.ConstraintTarget;
import tytoo.minegui.helper.constraint.constraints.Constraints;

import java.util.Objects;
import java.util.Optional;

public final class LayoutConstraints {
    private final Constraints constraints;
    private final Float rawX;
    private final Float rawY;
    private final Float widthOverride;
    private final Float heightOverride;
    private final ConstraintTarget targetOverride;

    private LayoutConstraints(
            Constraints constraints,
            Float rawX,
            Float rawY,
            Float widthOverride,
            Float heightOverride,
            ConstraintTarget targetOverride) {
        this.constraints = constraints;
        this.rawX = sanitizeCoordinate(rawX);
        this.rawY = sanitizeCoordinate(rawY);
        this.widthOverride = sanitizeLength(widthOverride);
        this.heightOverride = sanitizeLength(heightOverride);
        this.targetOverride = targetOverride;
    }

    public static LayoutConstraints empty() {
        return builder().build();
    }

    public static LayoutConstraints of(Constraints constraints) {
        return builder().constraints(constraints).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Float sanitizeCoordinate(Float value) {
        if (value == null) {
            return null;
        }
        if (!Float.isFinite(value)) {
            return null;
        }
        return value;
    }

    private static Float sanitizeLength(Float value) {
        if (value == null) {
            return null;
        }
        if (!Float.isFinite(value)) {
            return null;
        }
        if (value < 0f) {
            return null;
        }
        return value;
    }

    public Optional<Constraints> constraints() {
        return Optional.ofNullable(constraints);
    }

    public Optional<Float> rawX() {
        return Optional.ofNullable(rawX);
    }

    public Optional<Float> rawY() {
        return Optional.ofNullable(rawY);
    }

    public Optional<Float> widthOverride() {
        return Optional.ofNullable(widthOverride);
    }

    public Optional<Float> heightOverride() {
        return Optional.ofNullable(heightOverride);
    }

    public Optional<ConstraintTarget> targetOverride() {
        return Optional.ofNullable(targetOverride);
    }

    Constraints directConstraints() {
        return constraints;
    }

    float rawXOrNaN() {
        return rawX != null ? rawX : Float.NaN;
    }

    float rawYOrNaN() {
        return rawY != null ? rawY : Float.NaN;
    }

    float widthOverrideOrZero() {
        return widthOverride != null ? widthOverride : 0f;
    }

    float heightOverrideOrZero() {
        return heightOverride != null ? heightOverride : 0f;
    }

    ConstraintTarget targetOrDefault(ConstraintTarget fallback) {
        return targetOverride != null ? targetOverride : fallback;
    }

    public static final class Builder {
        private Constraints constraints;
        private Float rawX;
        private Float rawY;
        private Float widthOverride;
        private Float heightOverride;
        private ConstraintTarget targetOverride;

        private Builder() {
        }

        public Builder constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public Builder rawX(float rawX) {
            this.rawX = rawX;
            return this;
        }

        public Builder rawY(float rawY) {
            this.rawY = rawY;
            return this;
        }

        public Builder width(float width) {
            this.widthOverride = width;
            return this;
        }

        public Builder height(float height) {
            this.heightOverride = height;
            return this;
        }

        public Builder target(ConstraintTarget target) {
            this.targetOverride = target;
            return this;
        }

        public LayoutConstraints build() {
            if (constraints == null && rawX == null && rawY == null) {
                return new LayoutConstraints(null, null, null, widthOverride, heightOverride, targetOverride);
            }
            return new LayoutConstraints(
                    constraints,
                    rawX,
                    rawY,
                    widthOverride,
                    heightOverride,
                    targetOverride != null ? Objects.requireNonNull(targetOverride, "target") : null
            );
        }
    }
}
