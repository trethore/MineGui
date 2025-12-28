package tytoo.minegui.view;

@FunctionalInterface
public interface VisibilityListener {
    void onVisibilityChanged(View view, boolean visible);
}
