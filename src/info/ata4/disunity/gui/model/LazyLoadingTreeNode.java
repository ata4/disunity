/*
 ** 2014 October 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.model;

import info.ata4.log.LogUtils;
import info.ata4.util.progress.DummyProgress;
import info.ata4.util.progress.Progress;
import java.awt.Cursor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class LazyLoadingTreeNode extends DefaultMutableTreeNode implements TreeWillExpandListener {
    
    private static final Logger L = LogUtils.getLogger();
    
    protected final JTree tree;
    private boolean loaded;
    private Progress progress = new DummyProgress();

    public LazyLoadingTreeNode(JTree tree) {
        super();
        this.tree = tree;
        tree.addTreeWillExpandListener(this);
    }

    public LazyLoadingTreeNode(JTree tree, Object userObject) {
        super(userObject);
        this.tree = tree;
        tree.addTreeWillExpandListener(this);
    }

    public LazyLoadingTreeNode(JTree tree, Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
        this.tree = tree;
        tree.addTreeWillExpandListener(this);
    }
    
    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Progress getProgress() {
        return progress;
    }

    @Override
    public boolean isLeaf() {
        if (!loaded) {
            return false;
        } else {
            return super.isLeaf();
        }
    }

    public void load() {
        if (loaded) {
            return;
        }
        
        L.log(Level.FINE, "Loading {0}", getUserObject());
        
        try {
            tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            doLoad();
            loaded = true;
        } finally {
            tree.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    protected abstract void doLoad();

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        if (!loaded && event.getPath() != null && event.getPath().getLastPathComponent() == this) {
            load();

            // don't receive further events after the node has been loaded
            tree.removeTreeWillExpandListener(this);
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }
}
