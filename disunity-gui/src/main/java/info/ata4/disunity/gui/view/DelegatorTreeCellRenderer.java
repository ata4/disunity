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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DelegatorTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private final List<DelegateTreeCellRenderer> delegates = new ArrayList<>();
    
    public void addDelegate(DelegateTreeCellRenderer delegate) {
        delegates.add(delegate);
    }
    
    public void removeDelegate(DelegateTreeCellRenderer delegate) {
        delegates.remove(delegate);
    }
    
    public void clearDelegates() {
        delegates.clear();
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();
            
            for (DelegateTreeCellRenderer delegate : delegates) {
                if (delegate.getTreeCellType().isInstance(userObject)) {
                    delegate.configureTreeCellRenderer(this, userObject, sel,
                            expanded, leaf, row, hasFocus);
                }
            }
        }
        
        return this;
    }
}
