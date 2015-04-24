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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @param <T>
 */
public class Node<T extends Node<T>> implements Iterable<T> {
    
    private T parent;
    private final List<T> children = new ArrayList<>();
    
    public T parent() {
        return parent;
    }
    
    protected void parent(T parent) {
        this.parent = parent;
    }
    
    public int size() {
        return children.size();
    }
    
    public boolean isEmpty() {
        return children.isEmpty();
    }
    
    public void add(T node) {
        node.parent((T) this);
        children.add(node);
    }
    
    public boolean remove(T node) {
        boolean removed = children.remove(node);
        if (removed) {
            node.parent(null);
        }
        return removed;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.children);
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
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
        return true;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl(children.iterator());
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
