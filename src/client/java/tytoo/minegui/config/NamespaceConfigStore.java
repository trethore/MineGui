package tytoo.minegui.config;

public interface NamespaceConfigStore {
    NamespaceConfig load(String namespace);

    void save(NamespaceConfig config);
}
