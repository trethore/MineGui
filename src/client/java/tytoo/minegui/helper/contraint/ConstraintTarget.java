package tytoo.minegui.helper.contraint;

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

    float measuredWidth();

    float measuredHeight();
}
