package vip.Resolute.util.player;

import java.util.List;

public class Manager<T> {

    protected final List<T> elements;

    public Manager(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getElements() {
        return elements;
    }
}
