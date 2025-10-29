package tytoo.minegui.persistence;

public record ViewPersistenceRequest(
        String namespace,
        String viewId,
        String scopedId
) {
    public ViewPersistenceRequest {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace cannot be null or blank");
        }
        if (viewId == null || viewId.isBlank()) {
            throw new IllegalArgumentException("viewId cannot be null or blank");
        }
        if (scopedId == null || scopedId.isBlank()) {
            throw new IllegalArgumentException("scopedId cannot be null or blank");
        }
    }
}
