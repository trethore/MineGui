package tytoo.minegui.component.traits;

public interface Repeatable<T extends Repeatable<T>> {
    boolean isRepeatable();

    void setRepeatable(boolean repeatable);

    default T repeatable(boolean repeatable) {
        setRepeatable(repeatable);
        return self();
    }

    default T repeatable() {
        return repeatable(true);
    }

    T self();
}
