package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.callback.ImListClipperCallback;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.ComponentPool;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.traits.Disableable;
import tytoo.minegui.component.traits.Scalable;
import tytoo.minegui.component.traits.Sizable;
import tytoo.minegui.component.traits.Stateful;
import tytoo.minegui.state.State;
import tytoo.minegui.utils.ImGuiUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class MGListBox<T> extends MGComponent<MGListBox<T>>
        implements Disableable<MGListBox<T>>, Scalable<MGListBox<T>>, Sizable<MGListBox<T>>, Stateful<T, MGListBox<T>> {

    private static final int DEFAULT_VISIBLE_ITEMS = 7;
    private static final ComponentPool<MGListBox<?>> POOL =
            new ComponentPool<>(MGListBox::createRaw, MGListBox::resetRaw);

    private final List<T> literalItems = new ArrayList<>();
    private Supplier<List<T>> itemsSupplier = this::literalItemsView;
    private Function<T, String> itemFormatter = MGListBox::defaultFormat;
    private String label = "";
    private boolean disabled;
    private boolean clipperEnabled = true;
    private float scale = 1.0f;
    private int heightInItems = -1;
    private int localIndex = -1;
    @Nullable
    private T localValue;
    @Nullable
    private State<T> valueState;
    @Nullable
    private State<Integer> indexState;
    @Nullable
    private Consumer<T> onValueChange;
    @Nullable
    private Consumer<T> onValueCommit;
    @Nullable
    private IntConsumer onIndexChange;
    @Nullable
    private IntConsumer onIndexCommit;

    private MGListBox() {
    }

    public static <V> MGListBox<V> of() {
        @SuppressWarnings("unchecked")
        MGListBox<V> listBox = (MGListBox<V>) POOL.acquire();
        return listBox;
    }

    public static <V> MGListBox<V> of(Collection<V> items) {
        MGListBox<V> listBox = of();
        listBox.items(items);
        return listBox;
    }

    public static MGListBox<String> of(String... items) {
        MGListBox<String> listBox = of();
        listBox.items(items);
        return listBox;
    }

    private static MGListBox<?> createRaw() {
        return new MGListBox<>();
    }

    private static void resetRaw(MGListBox<?> listBox) {
        listBox.reset();
    }

    private static String defaultFormat(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        String formatted = value.toString();
        return formatted != null ? formatted : "";
    }

    private List<T> literalItemsView() {
        return literalItems;
    }

    private void reset() {
        literalItems.clear();
        itemsSupplier = this::literalItemsView;
        itemFormatter = MGListBox::defaultFormat;
        label = "";
        disabled = false;
        clipperEnabled = true;
        scale = 1.0f;
        heightInItems = -1;
        localIndex = -1;
        localValue = null;
        valueState = null;
        indexState = null;
        onValueChange = null;
        onValueCommit = null;
        onIndexChange = null;
        onIndexCommit = null;
    }

    public MGListBox<T> items(Collection<T> items) {
        literalItems.clear();
        if (items != null && !items.isEmpty()) {
            literalItems.addAll(items);
        }
        itemsSupplier = this::literalItemsView;
        return self();
    }

    @SafeVarargs
    public final MGListBox<T> items(T... items) {
        literalItems.clear();
        if (items != null && items.length > 0) {
            Collections.addAll(literalItems, items);
        }
        itemsSupplier = this::literalItemsView;
        return self();
    }

    public MGListBox<T> items(Supplier<List<T>> supplier) {
        itemsSupplier = supplier != null ? supplier : this::literalItemsView;
        return self();
    }

    public MGListBox<T> formatter(Function<T, String> formatter) {
        itemFormatter = formatter != null ? formatter : MGListBox::defaultFormat;
        return self();
    }

    public MGListBox<T> label(@Nullable String label) {
        this.label = label != null ? label : "";
        return self();
    }

    public MGListBox<T> heightInItems(int count) {
        heightInItems = count;
        return self();
    }

    public MGListBox<T> useClipper(boolean enabled) {
        clipperEnabled = enabled;
        return self();
    }

    public MGListBox<T> selectIndex(int index) {
        localIndex = index;
        if (valueState == null) {
            localValue = null;
        }
        return self();
    }

    public MGListBox<T> selectValue(@Nullable T value) {
        localValue = value;
        if (indexState == null) {
            localIndex = -1;
        }
        return self();
    }

    public MGListBox<T> onChange(Consumer<T> consumer) {
        onValueChange = consumer;
        return self();
    }

    public MGListBox<T> onCommit(Consumer<T> consumer) {
        onValueCommit = consumer;
        return self();
    }

    public MGListBox<T> onIndexChange(IntConsumer consumer) {
        onIndexChange = consumer;
        return self();
    }

    public MGListBox<T> onIndexCommit(IntConsumer consumer) {
        onIndexCommit = consumer;
        return self();
    }

    public MGListBox<T> indexState(State<Integer> state) {
        indexState = state;
        return self();
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    @Nullable
    public State<T> getState() {
        return valueState;
    }

    @Override
    public void setState(@Nullable State<T> state) {
        valueState = state;
    }

    @Override
    protected void renderComponent() {
        List<T> items = resolveItems();
        int selectedIndex = resolveSelectedIndex(items);

        boolean scaled = scale != 1.0f;
        float appliedScale = scaled ? scale : 1.0f;
        float preferredWidth = computePreferredWidth(items, appliedScale);
        float preferredHeight = computePreferredHeight(items, appliedScale);
        String widgetLabel = widgetLabel(label);

        withLayout(preferredWidth, preferredHeight, (width, height) -> {
            if (disabled) {
                ImGui.beginDisabled(true);
            }
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            try {
                if (ImGui.beginListBox(widgetLabel, width, height)) {
                    renderItems(items, selectedIndex);
                    renderChildren();
                    ImGui.endListBox();
                }
            } finally {
                if (scaled) {
                    ImGuiUtils.popWindowFontScale();
                }
                if (disabled) {
                    ImGui.endDisabled();
                }
            }
        });
    }

    private List<T> resolveItems() {
        List<T> supplied = itemsSupplier != null ? itemsSupplier.get() : literalItems;
        if (supplied == null) {
            return Collections.emptyList();
        }
        return supplied;
    }

    private int resolveSelectedIndex(List<T> items) {
        if (indexState != null) {
            Integer indexValue = indexState.get();
            if (indexValue != null) {
                int normalized = normalizeIndex(indexValue, items.size());
                localIndex = normalized;
                if (normalized < 0 && valueState == null) {
                    localValue = null;
                }
            }
        }
        if (valueState != null) {
            T value = valueState.get();
            int valueIndex = indexOfValue(items, value);
            if (valueIndex >= 0) {
                localIndex = valueIndex;
                localValue = value;
                return valueIndex;
            }
        }
        if (localIndex >= 0 && localIndex < items.size()) {
            return localIndex;
        }
        if (localValue != null) {
            int valueIndex = indexOfValue(items, localValue);
            if (valueIndex >= 0) {
                localIndex = valueIndex;
                return valueIndex;
            }
        }
        return -1;
    }

    private int normalizeIndex(int index, int size) {
        if (size <= 0) {
            return -1;
        }
        if (index < 0 || index >= size) {
            return -1;
        }
        return index;
    }

    private int indexOfValue(List<T> items, @Nullable T value) {
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i), value)) {
                return i;
            }
        }
        return -1;
    }

    private float computePreferredWidth(List<T> items, float appliedScale) {
        float maxWidth = 0.0f;
        for (T item : items) {
            String labelText = formatItem(item);
            float width = ImGui.calcTextSize(labelText).x;
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        float scrollbar = ImGui.getStyle().getScrollbarSize();
        float padding = ImGui.getStyle().getFramePaddingX() * 2.0f;
        float baseWidth = maxWidth + scrollbar + padding;
        return Math.max(1.0f, baseWidth * appliedScale);
    }

    private float computePreferredHeight(List<T> items, float appliedScale) {
        float itemHeight = ImGui.getTextLineHeightWithSpacing() * appliedScale;
        int visibleItems = resolveVisibleItems(items.size());
        float padding = ImGui.getStyle().getFramePaddingY() * 2.0f;
        float height = visibleItems * itemHeight + padding;
        return Math.max(itemHeight + padding, height);
    }

    private int resolveVisibleItems(int totalItems) {
        if (heightInItems > 0) {
            return heightInItems;
        }
        if (totalItems <= 0) {
            return DEFAULT_VISIBLE_ITEMS;
        }
        return Math.min(DEFAULT_VISIBLE_ITEMS, totalItems);
    }

    private void renderItems(List<T> items, int currentIndex) {
        if (items.isEmpty()) {
            return;
        }
        if (clipperEnabled) {
            ImGuiListClipper.forEach(items.size(), new ImListClipperCallback() {
                @Override
                public void accept(int index) {
                    if (index >= 0 && index < items.size()) {
                        renderItem(items, currentIndex, index);
                    }
                }
            });
        } else {
            for (int i = 0; i < items.size(); i++) {
                renderItem(items, currentIndex, i);
            }
        }
    }

    private void renderItem(List<T> items, int currentIndex, int index) {
        T item = items.get(index);
        String itemLabel = formatItem(item);
        boolean selected = index == currentIndex;
        if (ImGui.selectable(itemLabel, selected)) {
            applySelection(index, item);
        }
        if (selected) {
            ImGui.setItemDefaultFocus();
        }
    }

    private String formatItem(@Nullable T value) {
        String formatted = itemFormatter.apply(value);
        return formatted != null ? formatted : "";
    }

    private void applySelection(int index, @Nullable T value) {
        LocalSelection oldSelection = snapshotSelection();
        updateIndexSelection(index);
        updateValueSelection(value);
        LocalSelection newSelection = snapshotSelection();
        if (!Objects.equals(oldSelection, newSelection)) {
            if (onIndexChange != null) {
                onIndexChange.accept(index);
            }
            if (onValueChange != null) {
                onValueChange.accept(value);
            }
            if (onIndexCommit != null) {
                onIndexCommit.accept(index);
            }
            if (onValueCommit != null) {
                onValueCommit.accept(value);
            }
        }
    }

    private void updateIndexSelection(int index) {
        if (indexState != null) {
            indexState.set(index);
        }
        localIndex = index;
    }

    private void updateValueSelection(@Nullable T value) {
        if (valueState != null) {
            valueState.set(value);
        }
        localValue = value;
    }

    private LocalSelection snapshotSelection() {
        return new LocalSelection(localIndex, localValue);
    }

    private record LocalSelection(int index, @Nullable Object value) {
    }
}
