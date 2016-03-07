/*
 ** 2014 September 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @param <T>
 */
public class Node<T> implements Collection<Node<T>> {

    private T data;
    private Node<T> parent;
    private final List<Node<T>> children = new ArrayList<>();

    public Node() {
    }

    public Node(T data) {
        this.data = data;
    }

    public Node(Collection<Node<T>> children) {
        if (children != null) {
            this.children.addAll(children);
        }
    }

    public Node(T data, Collection<Node<T>> children) {
        this.data = data;
        if (children != null) {
            this.children.addAll(children);
        }
    }

    public T data() {
        return data;
    }

    public void data(T data) {
        this.data = data;
    }

    public Node<T> parent() {
        return parent;
    }

    private void parent(Node<T> parent) {
        this.parent = parent;
    }

    public void forEachData(Consumer<T> action) {
        Objects.requireNonNull(action);
        action.accept(data());
        children.forEach(node -> node.forEachData(action));
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return children.contains(o);
    }

    @Override
    public Iterator<Node<T>> iterator() {
        return Collections.unmodifiableList(children).iterator();
    }

    @Override
    public Spliterator<Node<T>> spliterator() {
        return Spliterators.spliterator(Collections.unmodifiableList(children),
                Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    @Override
    public Object[] toArray() {
        return children.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return children.toArray(a);
    }

    @Override
    public boolean add(Node<T> node) {
        boolean added = children.add(node);
        if (added) {
            node.parent(this);
        }
        return added;
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = children.remove(o);
        if (removed) {
            ((Node<T>) o).parent(null);
        }
        return removed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return children.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Node<T>> c) {
        boolean added = children.addAll(c);
        if (added) {
            forEach(node -> node.parent(this));
        }
        return added;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        List<Node<T>> childrenOld = new ArrayList<>(children);
        boolean removed = children.removeAll(c);
        if (removed) {
            childrenOld.removeAll(children);
            childrenOld.forEach(node -> node.parent(null));
        }
        return removed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<Node<T>> childrenOld = new ArrayList<>(children);
        boolean retained = children.retainAll(c);
        if (retained) {
            childrenOld.removeAll(children);
            childrenOld.forEach(node -> node.parent(null));
        }
        return retained;
    }

    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.data);
        hash = 43 * hash + Objects.hashCode(this.children);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node<?> other = (Node<?>) obj;
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
        return true;
    }
}
