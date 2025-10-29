package tytoo.minegui.helper;

import tytoo.minegui.helper.layout.HStack;
import tytoo.minegui.helper.layout.VStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class UI {
    private UI() {
    }

    public static void withVStack(Consumer<VStack> body) {
        withVStack(null, body);
    }

    public static void withVStack(VStack.Options options, Consumer<VStack> body) {
        Objects.requireNonNull(body, "body");
        try (VStack layout = VStack.begin(options)) {
            body.accept(layout);
        }
    }

    public static <T> T withVStackResult(Function<VStack, T> body) {
        return withVStackResult(null, body);
    }

    public static <T> T withVStackResult(VStack.Options options, Function<VStack, T> body) {
        Objects.requireNonNull(body, "body");
        try (VStack layout = VStack.begin(options)) {
            return body.apply(layout);
        }
    }

    public static void withVStackItem(VStack layout, Runnable body) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(body, "body");
        try (VStack.ItemScope ignore = layout.next()) {
            body.run();
        }
    }

    public static void withVStackItem(VStack layout, VStack.ItemRequest request, Runnable body) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(body, "body");
        try (VStack.ItemScope ignore = layout.next(request)) {
            body.run();
        }
    }

    public static <T> T withVStackItemResult(VStack layout, Supplier<T> body) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(body, "body");
        try (VStack.ItemScope ignore = layout.next()) {
            return body.get();
        }
    }

    public static <T> T withVStackItemResult(VStack layout, VStack.ItemRequest request, Supplier<T> body) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(body, "body");
        try (VStack.ItemScope ignore = layout.next(request)) {
            return body.get();
        }
    }

    public static void withHStack(HStack.Options options, Consumer<HStack> body) {
        Objects.requireNonNull(body, "body");
        try (HStack stack = HStack.begin(options)) {
            body.accept(stack);
        }
    }

    public static <T> T withHStackResult(Function<HStack, T> body) {
        return withHStackResult(null, body);
    }

    public static <T> T withHStackResult(HStack.Options options, Function<HStack, T> body) {
        Objects.requireNonNull(body, "body");
        try (HStack stack = HStack.begin(options)) {
            return body.apply(stack);
        }
    }

    public static void withHItem(HStack stack, Runnable body) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(body, "body");
        try (HStack.ItemScope ignore = stack.next()) {
            body.run();
        }
    }

    public static void withHItem(HStack stack, HStack.ItemRequest request, Runnable body) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(body, "body");
        try (HStack.ItemScope ignore = stack.next(request)) {
            body.run();
        }
    }

    public static <T> T withHItemResult(HStack stack, Supplier<T> body) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(body, "body");
        try (HStack.ItemScope ignore = stack.next()) {
            return body.get();
        }
    }

    public static <T> T withHItemResult(HStack stack, HStack.ItemRequest request, Supplier<T> body) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(body, "body");
        try (HStack.ItemScope ignore = stack.next(request)) {
            return body.get();
        }
    }
}
