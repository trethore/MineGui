package tytoo.minegui.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MGContainer implements MGComponent {

    protected final List<MGComponent> children = new ArrayList<>();

    public void add(MGComponent component) {
        this.children.add(component);
    }

    public void addAll(MGComponent... components) {
        Collections.addAll(this.children, components);
    }

    public void remove(MGComponent component) {
        this.children.remove(component);
    }

    public void clear() {
        this.children.clear();
    }

    @Override
    public void render() {
        children.forEach(MGComponent::render);
    }
}