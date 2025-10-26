package tytoo.minegui.helper.constraint;

public interface ConstraintTarget {
    ConstraintTarget EMPTY = new ConstraintTarget() {
        @Override
        public float measuredWidth() {
            return 0f;
        }

        @Override
        public float measuredHeight() {
            return 0f;
        }
    };

    static ConstraintTarget of(float measuredWidth, float measuredHeight) {
        return new SimpleConstraintTarget(measuredWidth, measuredHeight);
    }

    default Dimensions dimensions() {
        return new Dimensions(measuredWidth(), measuredHeight());
    }

    float measuredWidth();

    float measuredHeight();

    record Dimensions(float measuredWidth, float measuredHeight) implements ConstraintTarget {
        public Dimensions {
            if (!Float.isFinite(measuredWidth) || measuredWidth < 0f) {
                measuredWidth = 0f;
            }
            if (!Float.isFinite(measuredHeight) || measuredHeight < 0f) {
                measuredHeight = 0f;
            }
        }

        @Override
        public float measuredWidth() {
            return measuredWidth;
        }

        @Override
        public float measuredHeight() {
            return measuredHeight;
        }
    }

    final class SimpleConstraintTarget implements ConstraintTarget {
        private final float measuredWidth;
        private final float measuredHeight;

        private SimpleConstraintTarget(float measuredWidth, float measuredHeight) {
            this.measuredWidth = Float.isFinite(measuredWidth) ? Math.max(0f, measuredWidth) : 0f;
            this.measuredHeight = Float.isFinite(measuredHeight) ? Math.max(0f, measuredHeight) : 0f;
        }

        @Override
        public float measuredWidth() {
            return measuredWidth;
        }

        @Override
        public float measuredHeight() {
            return measuredHeight;
        }
    }
}
