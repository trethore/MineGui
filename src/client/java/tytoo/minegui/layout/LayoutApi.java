package tytoo.minegui.layout;

public interface LayoutApi {
    StackLayoutBuilder vertical();

    StackLayoutBuilder row();

    GridLayoutBuilder grid();

    default LayoutTemplate template(LayoutRenderable renderable) {
        return new LayoutTemplate(LayoutNodes.render(renderable));
    }

    void render(LayoutTemplate template);

    void render(LayoutTemplate template, Runnable body);
}
