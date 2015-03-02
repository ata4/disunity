/*
 ** 2014 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.view;

import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface DelegateTreeCellRenderer<E> {
    
    Class<E> getTreeCellType();
    
    void configureTreeCellRenderer(DefaultTreeCellRenderer renderer, E userData,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus);
}
