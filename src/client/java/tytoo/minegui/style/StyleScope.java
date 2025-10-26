package tytoo.minegui.style;

import java.util.Objects;

public final class StyleScope implements AutoCloseable {
    private final StyleManager.StyleScope delegate;

    private StyleScope(StyleManager.StyleScope delegate) {
        this.delegate = delegate;
    }

    public static StyleScope push(MGStyleDelta delta) {
        Objects.requireNonNull(delta, "delta");
        StyleManager.StyleScope scope = StyleManager.current().pushRaw(delta);
        return new StyleScope(scope);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
