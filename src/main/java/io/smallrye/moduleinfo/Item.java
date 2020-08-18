package io.smallrye.moduleinfo;

class Item<T> {
    private T item;

    public Item() {
    }

    public Item(final T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }

    public T getOrDefault(final T defVal) {
        final T item = this.item;
        return item == null ? defVal : item;
    }

    public void set(final T item) {
        this.item = item;
    }
}
