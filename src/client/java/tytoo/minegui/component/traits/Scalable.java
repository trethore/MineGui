package tytoo.minegui.component.traits;

public interface Scalable<T extends Scalable<T>> {
    float getScale();

    void setScale(float scale);

    default T scale(float scale) {
        setScale(scale);
        return self();
    }

    T self();
}
