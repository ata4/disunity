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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @param <T>
 */
public abstract class TreeNode<T extends TreeNode> implements Collection<T> {
    
    protected T parent;
    protected final List<T> children = new ArrayList<>();
    
    public T getParent() {
        return parent;
    }
    
    protected void setParent(T parent) {
        this.parent = parent;
    }
    
    private void setChildrenParent(Collection<T> col, TreeNode parent) {
        for (T child : col) {
            child.setParent(parent);
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
        return children.iterator();
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
        e.setParent(this);
        return children.add(e);
    }

    @Override
    public boolean remove(Object o) {
        boolean r = children.remove(o);
        if (r && o instanceof TreeNode) {
            ((TreeNode) o).setParent(null);
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
        setChildrenParent(children, this);
        return b;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        boolean b = children.addAll(index, c);
        setChildrenParent(children, this);
        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        List<T> childrenRemoved = new ArrayList<>(children);
        boolean b = children.removeAll(c);
        childrenRemoved.removeAll(children);
        setChildrenParent(childrenRemoved, null);
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<T> childrenRemoved = new ArrayList<>(children);
        boolean b = children.retainAll(c);
        childrenRemoved.removeAll(children);
        setChildrenParent(childrenRemoved, null);
        return b;
    }

    @Override
    public void clear() {
        setChildrenParent(children, null);
        children.clear();
    }
}
