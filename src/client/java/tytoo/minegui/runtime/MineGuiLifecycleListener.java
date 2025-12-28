package tytoo.minegui.runtime;

@SuppressWarnings("unused")
public interface MineGuiLifecycleListener {
    default void onContextReady(MineGuiContext context) {
    }

    default void onPreRender(MineGuiContext context) {
    }

    default void onPostRender(MineGuiContext context) {
    }

    default void onShutdown(MineGuiContext context) {
    }
}
