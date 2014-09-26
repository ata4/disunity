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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @param <T>
 */
public abstract class TreeNode<T extends TreeNode> implements Iterable<T> {
    
    protected final List<T> children = new ArrayList<>();

    public List<T> getChildren() {
        return children;
    }

    @Override
    public Iterator<T> iterator() {
        return children.iterator();
    }
}
