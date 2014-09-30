/*
 ** 2014 September 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.control;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.ObjectPath;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.unity.assetbundle.BufferedEntry;
import info.ata4.unity.gui.model.AssetFileTreeModel;
import info.ata4.unity.gui.util.progress.ProgressTask;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.ObjectData;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeController {
    
    private static final Logger L = LogUtils.getLogger();

    private final Component parent;
    private final JTree tree;
    private AssetFileTreeModel model;

    public AssetFileTreeController(Component parent, JTree tree) {
        this.parent = parent;
        this.tree = tree;
        
        tree.addTreeWillExpandListener(new TreeWillExpandListenerImpl());
        tree.addMouseListener(new MouseAdapterImpl());
    }
    
    public void load(Path file) throws IOException {
        new AssetFileLoadTask(parent, file).execute();
    }
    
    private void busyState() {
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    private void idleState() {
        parent.setCursor(Cursor.getDefaultCursor());
    }
    
    private class TreeWillExpandListenerImpl implements TreeWillExpandListener {
    
        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            Object obj = event.getPath().getLastPathComponent();
            if (!(obj instanceof DefaultMutableTreeNode)) {
                return;
            }

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) obj;

            Object userObj = treeNode.getUserObject();
            if (model.isAssetBundleEntryNodeUnloaded(treeNode)) {
                BufferedEntry entry = (BufferedEntry) userObj;

                // clear node
                treeNode.removeAllChildren();

                // load the asset
                L.log(Level.FINE, "Lazy-loading asset for entry {0}", entry);

                try {
                    busyState();
                    AssetFile asset = new AssetFile();
                    asset.load(entry.getReader());
                    model.addAssetNodes(treeNode, asset);
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Can't load asset", ex);
                    treeNode.add(new DefaultMutableTreeNode(ex));
                } finally {
                    idleState();
                }

                model.setAssetBundleEntryNodeLoaded(treeNode);
            } else if (model.isObjectDataNodeUnloaded(treeNode)) {
                ObjectData objectData = (ObjectData) userObj;
                ObjectPath objectPath = objectData.getPath();
                FieldNode fieldNode = objectData.getInstance();

                L.log(Level.FINE, "Lazy-loading object {0}", objectPath);

                treeNode.removeAllChildren();
                for (FieldNode childFieldNode : fieldNode) {
                    model.addFieldNode(treeNode, childFieldNode);
                }

                model.setObjectDataNodeLoaded(treeNode);
            }
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        }
    }
    
    private class MouseAdapterImpl extends MouseAdapter {

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

                AssetFileTreePopup popup = new AssetFileTreePopup(selRow, selPath);
                if (popup.getComponentCount() > 0) {
                    popup.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }
        }
    }
    
    private class AssetFileLoadTask extends ProgressTask<Void, Void> {
        
        private final Path file;
        
        private AssetFileLoadTask(Component parent, Path file) {
            super(parent, "Loading " + file.getFileName(), "");
            this.file = file;
            
            monitor.setMillisToDecideToPopup(500);
            monitor.setMillisToPopup(0);
        }

        @Override
        protected Void doInBackground() throws Exception {
            busyState();
            
            tree.setModel(null);
            
            try {
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(file);
                model = new AssetFileTreeModel(root);

                if (AssetBundleUtils.isAssetBundle(file)) {
                    List<BufferedEntry> entries = AssetBundleUtils.buffer(file, progress);

                    model.addAssetBundleNodes(root, entries);
                } else {
                    AssetFile asset = new AssetFile();
                    asset.load(file);

                    model.addAssetNodes(root, asset);
                }

                tree.setModel(model);
            } finally {
                idleState();
            }
            
            return null;
        }
        
    }
}
