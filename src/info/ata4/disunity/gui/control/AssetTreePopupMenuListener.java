/*
 ** 2014 October 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetTreePopupMenuListener extends MouseAdapter {
    
    private final JTree tree;
    
    public AssetTreePopupMenuListener(JTree tree) {
        this.tree = tree;
    }

    @Override
    public void mousePressed(MouseEvent ev) {
        onClick(ev);
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
        onClick(ev);
    }

    private void onClick(MouseEvent ev) {
        int selRow = tree.getRowForLocation(ev.getX(), ev.getY());
        tree.setSelectionRow(selRow);

        if (ev.isPopupTrigger()) {
            TreePath selPath = tree.getPathForLocation(ev.getX(), ev.getY());

            if (selPath == null) {
                return;
            }

            AssetTreePopupMenu popup = new AssetTreePopupMenu(selRow, selPath);
            if (popup.getComponentCount() > 0) {
                popup.show(ev.getComponent(), ev.getX(), ev.getY());
            }
        }
    }
}