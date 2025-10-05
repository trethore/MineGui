package tytoo.minegui.component.traits;

import tytoo.minegui.state.State;

import java.util.function.Supplier;

public interface Textable<T extends Textable<T>> {
    Supplier<String> getTextSupplier();

    void setTextSupplier(Supplier<String> supplier);

    default T text(String text) {
        setTextSupplier(() -> text);
        return self();
    }

    default T bindText(State<String> state) {
        setTextSupplier(state::get);
        return self();
    }

    default String getText() {
        return getTextSupplier().get();
    }

    T self();
}
