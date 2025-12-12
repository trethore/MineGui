package tytoo.minegui.view.persistence;

import java.util.Locale;

final class ViewSlug {
    private ViewSlug() {
    }

    static String fromId(String viewId) {
        String base = viewId != null ? viewId : "";
        String normalized = base
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
        if (normalized.isBlank()) {
            return "view";
        }
        return normalized;
    }
}
