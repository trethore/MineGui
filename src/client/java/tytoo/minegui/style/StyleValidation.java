package tytoo.minegui.style;

import java.util.Objects;

final class StyleValidation {
    private StyleValidation() {
    }

    static void validateDescriptor(StyleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        descriptor.values().validate("StyleDescriptor");
    }

    static void validateDelta(StyleDelta delta) {
        Objects.requireNonNull(delta, "delta");
        delta.values().validate("StyleDelta");
    }
}
