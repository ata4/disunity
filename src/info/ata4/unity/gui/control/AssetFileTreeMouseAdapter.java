/*
 ** 2014 September 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeMouseAdapter extends MouseAdapter {
    
    private final JTree tree;

    public AssetFileTreeMouseAdapter(JTree tree) {
        this.tree = tree;
    }

    @Override
    public void mousePressed(MouseEvent ev) {
        if (ev.isPopupTrigger()) {
            showMenu(ev);
        }
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
        if (ev.isPopupTrigger()) {
            showMenu(ev);
        }
    }

    private void showMenu(MouseEvent ev) {
        int selRow = tree.getRowForLocation(ev.getX(), ev.getY());
        TreePath selPath = tree.getPathForLocation(ev.getX(), ev.getY());
        
        if (selPath == null) {
            return;
        }
        
        AssetFileTreePopup popup = new AssetFileTreePopup(selRow, selPath);
        if (popup.getComponentCount() > 0) {
            popup.show(ev.getComponent(), ev.getX(), ev.getY());
        }
    }
}
