package tytoo.minegui.persistence;

import java.util.Collection;
import java.util.Optional;

public interface ViewPersistenceAdapter {
    Optional<String> loadLayout(ViewPersistenceRequest request);

    void saveLayout(ViewPersistenceRequest request, String iniContent);

    Optional<String> loadStyleSnapshot(ViewPersistenceRequest request);

    boolean storeStyleSnapshot(ViewStyleSnapshot snapshot);

    default int exportStyleSnapshots(Collection<ViewStyleSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return 0;
        }
        int exported = 0;
        for (ViewStyleSnapshot snapshot : snapshots) {
            if (snapshot == null) {
                continue;
            }
            if (storeStyleSnapshot(snapshot) && !snapshot.deleted()) {
                exported++;
            }
        }
        return exported;
    }
}
