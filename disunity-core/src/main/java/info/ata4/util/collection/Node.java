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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @param <T>
 */
public abstract class Node<T extends Node> implements Collection<T> {
    
    private T parent;
    private final List<T> children = new ArrayList<>();
    
    public T parent() {
        return parent;
    }
    
    protected void parent(T parent) {
        this.parent = parent;
    }
    
    private void childrenParent(Collection<T> col, Node parent) {
        for (T child : col) {
            child.parent(parent);
        }
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
    public Iterator<T> iterator() {
        return new IteratorImpl(children.iterator());
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
    public boolean add(T e) {
        e.parent(this);
        return children.add(e);
    }

    @Override
    public boolean remove(Object o) {
        boolean r = children.remove(o);
        if (r && o instanceof Node) {
            ((Node) o).parent(null);
        }
        return r;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return children.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean b = children.addAll(c);
        childrenParent(children, this);
        return b;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        boolean b = children.addAll(index, c);
        childrenParent(children, this);
        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        List<T> childrenRemoved = new ArrayList<>(children);
        boolean b = children.removeAll(c);
        childrenRemoved.removeAll(children);
        childrenParent(childrenRemoved, null);
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<T> childrenRemoved = new ArrayList<>(children);
        boolean b = children.retainAll(c);
        childrenRemoved.removeAll(children);
        childrenParent(childrenRemoved, null);
        return b;
    }

    @Override
    public void clear() {
        childrenParent(children, null);
        children.clear();
    }

    @Override
    public int hashCode() {
        return children.hashCode();
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
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
        return true;
    }
    
    private class IteratorImpl implements Iterator<T> {
        
        private final Iterator<T> proxy;
        private T current;
        
        private IteratorImpl(Iterator<T> proxy) {
            this.proxy = proxy;
        }

        @Override
        public boolean hasNext() {
            return proxy.hasNext();
        }

        @Override
        public T next() {
            current = proxy.next();
            return current;
        }

        @Override
        public void remove() {
            proxy.remove();
            if (current != null) {
                current.parent(null);
            }
        }
    }
}
