package tytoo.minegui;

public record MineGuiInitializationOptions(boolean loadGlobalConfig, String configNamespace) {
    public MineGuiInitializationOptions {
        configNamespace = normalizeNamespace(configNamespace);
    }

    public static MineGuiInitializationOptions defaults() {
        return new MineGuiInitializationOptions(true, MineGuiCore.ID);
    }

    public static MineGuiInitializationOptions skipGlobalConfig() {
        return new MineGuiInitializationOptions(false, MineGuiCore.ID);
    }

    private static String normalizeNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return MineGuiCore.ID;
        }
        return namespace;
    }

    public MineGuiInitializationOptions withNamespace(String namespace) {
        return new MineGuiInitializationOptions(loadGlobalConfig, namespace);
    }

    public MineGuiInitializationOptions withLoadGlobalConfig(boolean loadGlobalConfig) {
        return new MineGuiInitializationOptions(loadGlobalConfig, configNamespace);
    }
}
