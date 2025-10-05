package tytoo.minegui.component.traits;

public interface Disableable<T extends Disableable<T>> {
    boolean isDisabled();

    void setDisabled(boolean disabled);

    default T disabled(boolean disabled) {
        setDisabled(disabled);
        return self();
    }

    default T enabled(boolean enabled) {
        setDisabled(!enabled);
        return self();
    }

    T self();
}
