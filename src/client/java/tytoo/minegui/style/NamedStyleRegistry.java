package tytoo.minegui.style;

import tytoo.minegui.MineGuiCore;
import tytoo.minegui.util.ResourceId;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public final class NamedStyleRegistry {
    private static final NamedStyleRegistry INSTANCE = new NamedStyleRegistry();

    private final ConcurrentMap<ResourceId, StyleDescriptor> descriptors = new ConcurrentHashMap<>();

    private NamedStyleRegistry() {
    }

    public static NamedStyleRegistry getInstance() {
        return INSTANCE;
    }

    public void registerDescriptor(ResourceId key, StyleDescriptor descriptor) {
        if (key == null || descriptor == null) {
            return;
        }
        descriptors.put(key, descriptor);
        StyleManager.registerDescriptor(key, descriptor);
    }

    public void registerPreset(ResourceId key, Consumer<StyleDescriptor.Builder> builderConsumer, StyleDescriptor base) {
        if (builderConsumer == null || base == null) {
            return;
        }
        StyleDescriptor.Builder builder = StyleDescriptor.builder().fromDescriptor(base);
        builderConsumer.accept(builder);
        registerDescriptor(key, builder.build());
    }

    public Optional<StyleDescriptor> getDescriptor(ResourceId key) {
        if (key == null) {
            return Optional.empty();
        }
        StyleDescriptor descriptor = descriptors.get(key);
        if (descriptor != null) {
            return Optional.of(descriptor);
        }
        return Optional.empty();
    }

    public Collection<ResourceId> keys() {
        return descriptors.keySet();
    }

    public Map<ResourceId, StyleDescriptor> snapshot() {
        return Map.copyOf(descriptors);
    }

    public void registerBasePresets(StyleDescriptor baseDescriptor) {
        if (baseDescriptor == null) {
            return;
        }
        ResourceId defaultId = ResourceId.of(MineGuiCore.ID, "default");
        registerDescriptor(defaultId, duplicate(baseDescriptor));

        registerPreset(ResourceId.of(MineGuiCore.ID, "compact"), builder -> {
            builder.windowPadding(baseDescriptor.getWindowPadding().x() * 0.7f, baseDescriptor.getWindowPadding().y() * 0.7f);
            builder.framePadding(baseDescriptor.getFramePadding().x() * 0.6f, baseDescriptor.getFramePadding().y() * 0.6f);
            builder.itemSpacing(Math.max(4.0f, baseDescriptor.getItemSpacing().x() * 0.6f),
                    Math.max(2.0f, baseDescriptor.getItemSpacing().y() * 0.6f));
            builder.windowRounding(Math.max(2.0f, baseDescriptor.getWindowRounding() * 0.5f));
            builder.frameRounding(Math.max(1.0f, baseDescriptor.getFrameRounding() * 0.5f));
        }, baseDescriptor);

        registerPreset(ResourceId.of(MineGuiCore.ID, "spacious"), builder -> {
            builder.windowPadding(baseDescriptor.getWindowPadding().x() + 6.0f, baseDescriptor.getWindowPadding().y() + 6.0f);
            builder.framePadding(baseDescriptor.getFramePadding().x() + 4.0f, baseDescriptor.getFramePadding().y() + 4.0f);
            builder.itemSpacing(baseDescriptor.getItemSpacing().x() + 6.0f, baseDescriptor.getItemSpacing().y() + 4.0f);
            builder.windowRounding(baseDescriptor.getWindowRounding() + 2.0f);
            builder.frameRounding(baseDescriptor.getFrameRounding() + 1.0f);
        }, baseDescriptor);
    }

    public void clear() {
        descriptors.clear();
    }

    private StyleDescriptor duplicate(StyleDescriptor descriptor) {
        return StyleDescriptor.builder().fromDescriptor(descriptor).build();
    }
}
