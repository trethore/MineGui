package tytoo.minegui.view.persistence;

import java.util.Objects;

public record ViewPersistenceRequest(String namespace, String viewId, String slug) {
    public ViewPersistenceRequest {
        namespace = Objects.requireNonNull(namespace, "namespace");
        viewId = Objects.requireNonNull(viewId, "viewId");
        slug = Objects.requireNonNull(slug, "slug");
    }
}
