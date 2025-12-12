package tytoo.minegui.view.persistence;

import java.util.Optional;

public interface ViewPersistenceAdapter {
    Optional<String> loadSharedLayout(String namespace);

    void saveSharedLayout(String namespace, String payload);

    Optional<String> loadLayout(ViewPersistenceRequest request);

    void saveLayout(ViewPersistenceRequest request, String payload);

    Optional<String> loadStyle(ViewPersistenceRequest request);

    void saveStyle(ViewPersistenceRequest request, ViewStyleSnapshot snapshot);
}
