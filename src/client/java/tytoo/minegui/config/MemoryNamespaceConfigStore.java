package tytoo.minegui.config;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryNamespaceConfigStore implements NamespaceConfigStore {
    private final Map<String, NamespaceConfig> configs = new ConcurrentHashMap<>();

    @Override
    public NamespaceConfig load(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        return configs.computeIfAbsent(namespace, NamespaceConfig::defaults);
    }

    @Override
    public void save(NamespaceConfig config) {
        Objects.requireNonNull(config, "config");
        configs.put(config.namespace(), config);
    }
}
