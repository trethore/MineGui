package tytoo.minegui.config;

public record PersistenceFlags(
        boolean config,
        boolean layouts,
        boolean styles
) {

    public static PersistenceFlags all() {
        return new PersistenceFlags(true, true, true);
    }

    public static PersistenceFlags none() {
        return new PersistenceFlags(false, false, false);
    }

    public boolean isAnyEnabled() {
        return config || layouts || styles;
    }

    public PersistenceFlags withConfig(boolean value) {
        return new PersistenceFlags(value, layouts, styles);
    }

    public PersistenceFlags withLayouts(boolean value) {
        return new PersistenceFlags(config, value, styles);
    }

    public PersistenceFlags withStyles(boolean value) {
        return new PersistenceFlags(config, layouts, value);
    }
}
