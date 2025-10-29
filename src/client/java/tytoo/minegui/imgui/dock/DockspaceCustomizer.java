package tytoo.minegui.imgui.dock;

@FunctionalInterface
public interface DockspaceCustomizer {
    static DockspaceCustomizer noop() {
        return state -> {
        };
    }

    void customize(DockspaceRenderState state);

    default DockspaceCustomizer andThen(DockspaceCustomizer next) {
        if (next == null) {
            return this;
        }
        return state -> {
            customize(state);
            next.customize(state);
        };
    }
}
