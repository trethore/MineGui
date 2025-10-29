package tytoo.minegui.persistence;

public record ViewStyleSnapshot(ViewPersistenceRequest request, String snapshotJson, boolean deleted) {
    public ViewStyleSnapshot {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (!deleted && snapshotJson == null) {
            throw new IllegalArgumentException("snapshotJson cannot be null when not deleted");
        }
    }

    public static ViewStyleSnapshot present(ViewPersistenceRequest request, String snapshotJson) {
        return new ViewStyleSnapshot(request, snapshotJson, false);
    }

    public static ViewStyleSnapshot deleted(ViewPersistenceRequest request) {
        return new ViewStyleSnapshot(request, null, true);
    }
}
