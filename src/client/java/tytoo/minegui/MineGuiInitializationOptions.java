package tytoo.minegui;

public record MineGuiInitializationOptions(boolean loadGlobalConfig, boolean ignoreGlobalConfig,
                                           String configNamespace) {
    public MineGuiInitializationOptions {
        configNamespace = normalizeNamespace(configNamespace);
    }

    public MineGuiInitializationOptions(boolean loadGlobalConfig, String configNamespace) {
        this(loadGlobalConfig, false, configNamespace);
    }

    public static MineGuiInitializationOptions defaults() {
        return new MineGuiInitializationOptions(true, false, MineGuiCore.ID);
    }

    public static MineGuiInitializationOptions skipGlobalConfig() {
        return new MineGuiInitializationOptions(false, false, MineGuiCore.ID);
    }

    public static MineGuiInitializationOptions ignoringGlobalConfig() {
        return new MineGuiInitializationOptions(false, true, MineGuiCore.ID);
    }

    private static String normalizeNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return MineGuiCore.ID;
        }
        return namespace;
    }

    public MineGuiInitializationOptions withNamespace(String namespace) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, namespace);
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace);
    }

    public MineGuiInitializationOptions withIgnoreGlobalConfig(boolean ignoreGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, ignoreGlobalConfig, configNamespace);
    }
}
