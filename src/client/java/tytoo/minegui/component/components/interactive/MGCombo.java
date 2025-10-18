package tytoo.minegui.component.components.interactive;

import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
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

public final class MGCombo<T> extends MGComponent<MGCombo<T>> implements Disableable<MGCombo<T>>,
        Scalable<MGCombo<T>>, Sizable<MGCombo<T>>, Stateful<T, MGCombo<T>> {

    private static final ComponentPool<MGCombo<?>> POOL =
            new ComponentPool<>(MGCombo::createRaw, MGCombo::resetRaw);

    private final String defaultLabel = "";
    private final List<T> literalItems = new ArrayList<>();
    private Supplier<List<T>> itemsSupplier = this::literalItemsView;
    private Function<T, String> itemFormatter = MGCombo::defaultFormat;
    private Supplier<String> emptyPreviewSupplier = () -> "";
    private String label = defaultLabel;
    private boolean disabled;
    private float scale = 1.0f;
    private int comboFlags = ImGuiComboFlags.None;
    private int localIndex = -1;
    @Nullable
    private T localValue;
    @Nullable
    private State<T> valueState;
    @Nullable
    private State<Integer> indexState;
    @Nullable
    private Supplier<String> previewSupplier;
    @Nullable
    private Consumer<T> onValueChange;
    @Nullable
    private Consumer<T> onValueCommit;
    @Nullable
    private IntConsumer onIndexChange;
    @Nullable
    private IntConsumer onIndexCommit;

    private MGCombo() {
    }

    public static <V> MGCombo<V> of() {
        @SuppressWarnings("unchecked")
        MGCombo<V> combo = (MGCombo<V>) POOL.acquire();
        return combo;
    }

    public static <V> MGCombo<V> of(Collection<V> items) {
        MGCombo<V> combo = of();
        combo.items(items);
        return combo;
    }

    public static MGCombo<String> of(String... items) {
        MGCombo<String> combo = of();
        combo.items(items);
        return combo;
    }

    private static MGCombo<?> createRaw() {
        return new MGCombo<>();
    }

    private static void resetRaw(MGCombo<?> combo) {
        combo.reset();
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
        itemFormatter = MGCombo::defaultFormat;
        emptyPreviewSupplier = () -> "";
        label = defaultLabel;
        disabled = false;
        scale = 1.0f;
        comboFlags = ImGuiComboFlags.None;
        localIndex = -1;
        localValue = null;
        valueState = null;
        indexState = null;
        previewSupplier = null;
        onValueChange = null;
        onValueCommit = null;
        onIndexChange = null;
        onIndexCommit = null;
    }

    public MGCombo<T> items(Collection<T> items) {
        literalItems.clear();
        if (items != null && !items.isEmpty()) {
            literalItems.addAll(items);
        }
        itemsSupplier = this::literalItemsView;
        return self();
    }

    @SafeVarargs
    public final MGCombo<T> items(T... items) {
        literalItems.clear();
        if (items != null && items.length > 0) {
            Collections.addAll(literalItems, items);
        }
        itemsSupplier = this::literalItemsView;
        return self();
    }

    public MGCombo<T> items(Supplier<List<T>> supplier) {
        itemsSupplier = supplier != null ? supplier : this::literalItemsView;
        return self();
    }

    public MGCombo<T> formatter(Function<T, String> formatter) {
        itemFormatter = formatter != null ? formatter : MGCombo::defaultFormat;
        return self();
    }

    public MGCombo<T> emptyPreview(Supplier<String> supplier) {
        emptyPreviewSupplier = supplier != null ? supplier : () -> "";
        return self();
    }

    public MGCombo<T> preview(Supplier<String> supplier) {
        previewSupplier = supplier;
        return self();
    }

    public MGCombo<T> label(@Nullable String label) {
        this.label = label != null ? visibleLabel(label) : defaultLabel;
        return self();
    }

    public MGCombo<T> flags(int flags) {
        comboFlags = flags;
        return self();
    }

    public MGCombo<T> addFlags(int flags) {
        comboFlags |= flags;
        return self();
    }

    public MGCombo<T> removeFlags(int flags) {
        comboFlags &= ~flags;
        return self();
    }

    public MGCombo<T> onChange(Consumer<T> consumer) {
        onValueChange = consumer;
        return self();
    }

    public MGCombo<T> onCommit(Consumer<T> consumer) {
        onValueCommit = consumer;
        return self();
    }

    public MGCombo<T> onIndexChange(IntConsumer consumer) {
        onIndexChange = consumer;
        return self();
    }

    public MGCombo<T> onIndexCommit(IntConsumer consumer) {
        onIndexCommit = consumer;
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

    public MGCombo<T> selectIndex(int index) {
        this.localIndex = index;
        if (valueState == null) {
            localValue = null;
        }
        return self();
    }

    public MGCombo<T> selectValue(@Nullable T value) {
        this.localValue = value;
        if (indexState == null) {
            localIndex = -1;
        }
        return self();
    }

    public MGCombo<T> indexState(State<Integer> state) {
        this.indexState = state;
        return self();
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
        int resolvedIndex = resolveSelectedIndex(items);
        T resolvedValue = resolvedIndex >= 0 && resolvedIndex < items.size() ? items.get(resolvedIndex) : null;

        String preview = resolvePreview(resolvedValue);

        boolean scaled = scale != 1.0f;
        float appliedScale = scaled ? scale : 1.0f;
        float paddingX = ImGui.getStyle().getFramePaddingX();
        float paddingY = ImGui.getStyle().getFramePaddingY();
        float textWidth = ImGui.calcTextSize(preview).x * appliedScale;
        float textHeight = ImGui.calcTextSize(preview).y * appliedScale;
        float frameHeight = ImGui.getFrameHeight() * appliedScale;
        float baseWidth = textWidth + paddingX * 2.0f + frameHeight;
        float baseHeight = Math.max(textHeight + paddingY * 2.0f, frameHeight);
        String widgetLabel = widgetLabel(this.label);

        withLayout(baseWidth, baseHeight, (width, height) -> {
            if (disabled) {
                ImGui.beginDisabled(true);
            }
            if (scaled) {
                ImGuiUtils.pushWindowFontScale(scale);
            }
            try {
                if (ImGui.beginCombo(widgetLabel, preview, comboFlags)) {
                    renderItems(items, resolvedIndex);
                    renderChildren();
                    ImGui.endCombo();
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
        List<T> items = itemsSupplier != null ? itemsSupplier.get() : literalItems;
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
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
            int idx = indexOfValue(items, value);
            if (idx >= 0) {
                localIndex = idx;
                localValue = value;
                return idx;
            }
        }
        if (localIndex >= 0 && localIndex < items.size()) {
            return localIndex;
        }
        if (localValue != null) {
            int idx = indexOfValue(items, localValue);
            if (idx >= 0) {
                localIndex = idx;
                return idx;
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

    private String resolvePreview(@Nullable T value) {
        if (previewSupplier != null) {
            String supplied = previewSupplier.get();
            if (supplied != null) {
                return supplied;
            }
        }
        if (value != null) {
            return formatItem(value);
        }
        String fallback = emptyPreviewSupplier.get();
        return fallback != null ? fallback : "";
    }

    private void renderItems(List<T> items, int currentIndex) {
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            String itemLabel = formatItem(item);
            boolean selected = i == currentIndex;
            if (ImGui.selectable(itemLabel, selected)) {
                applySelection(i, item);
            }
            if (selected) {
                ImGui.setItemDefaultFocus();
            }
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
