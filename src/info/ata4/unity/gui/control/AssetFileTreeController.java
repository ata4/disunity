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

import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.unity.gui.model.AssetBundleFileNode;
import info.ata4.unity.gui.model.AssetFileNode;
import info.ata4.unity.gui.model.FieldTypeDatabaseNode;
import info.ata4.unity.gui.model.LazyLoadingTreeNode;
import info.ata4.unity.gui.util.progress.ProgressTask;
import info.ata4.unity.gui.view.AssetFileTreeCellRenderer;
import info.ata4.unity.gui.view.AssetFileTreeNodeInfo;
import info.ata4.unity.rtti.FieldTypeDatabase;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeController {
    
    private final Component parent;
    private final JTree tree;

    public AssetFileTreeController(Component parent, JTree tree, JTextPane text) {
        this.parent = parent;
        this.tree = tree;
        
        tree.setCellRenderer(new AssetFileTreeCellRenderer());
        tree.addMouseListener(new MouseAdapterImpl());
        tree.addTreeSelectionListener(new AssetFileTreeNodeInfo(text));
    }
    
    public void load(Path file) throws IOException {
        new AssetFileLoadTask(parent, file).execute();
    }
    
    public void load(FieldTypeDatabase db) {
        tree.setModel(new DefaultTreeModel(new FieldTypeDatabaseNode(db)));
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
            tree.setModel(null);
            
            LazyLoadingTreeNode node;

            if (AssetBundleUtils.isAssetBundle(file)) {
                node = new AssetBundleFileNode(tree, file);
            } else {
                node = new AssetFileNode(tree, file);
            }

            node.setProgress(progress);
            node.load();

            tree.setModel(new DefaultTreeModel(node));

            return null;
        }
        
    }
}
