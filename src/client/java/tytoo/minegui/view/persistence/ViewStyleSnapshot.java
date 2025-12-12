package tytoo.minegui.view.persistence;

import java.util.Objects;

public record ViewStyleSnapshot(ViewPersistenceRequest request, String snapshotJson, boolean deleted) {
    public ViewStyleSnapshot {
        Objects.requireNonNull(request, "request");
    }

    public static ViewStyleSnapshot deleted(ViewPersistenceRequest request) {
        return new ViewStyleSnapshot(request, null, true);
    }
}
