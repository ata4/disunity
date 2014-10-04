/*
 ** 2014 October 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.model;

import info.ata4.io.Struct;
import info.ata4.log.LogUtils;
import info.ata4.unity.assetbundle.AssetBundleHeader;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.unity.assetbundle.BundleEntryBuffered;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleFileNode extends LazyLoadingTreeNode implements StructNode {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final Path file;
    private AssetBundleHeader header;
    
    public AssetBundleFileNode(JTree tree, Path file) {
        super(tree, file);
        this.file = file;
    }

    public AssetBundleHeader getHeader() {
        return header;
    }

    @Override
    protected void doLoad() {
        try (AssetBundleReader reader = new AssetBundleReader(file)) {
            header = reader.getHeader();
            
            for (BundleEntryBuffered entry : AssetBundleUtils.buffer(reader, getProgress())) {
                DefaultMutableTreeNode current = this;

                // create folders in case the name contains path separators
                String[] parts = StringUtils.split(entry.getInfo().getName(), '/');
                for (int i = 0; i < parts.length - 1; i++) {
                    DefaultMutableTreeNode folderNode = null;
                    String folderName = parts[i];

                    // look for existing folder node
                    for (int j = 0; j < current.getChildCount(); j++) {
                        DefaultMutableTreeNode child = ((DefaultMutableTreeNode) current.getChildAt(j));
                        if (child.getUserObject().equals(folderName)) {
                            folderNode = child;
                            break;
                        }
                    }

                    // create and add folder node if required
                    if (folderNode == null) {
                        folderNode = new DefaultMutableTreeNode(folderName);
                        current.add(folderNode);
                    }

                    // move one level up
                    current = folderNode;
                }

                if (entry.getInfo().isAsset()) {
                    current.add(new AssetFileNode(tree, entry));
                } else {
                    current.add(new StructMutableTreeNode(entry, entry.getInfo()));
                }
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't load asset bundle file " + file, ex);
            add(new DefaultMutableTreeNode(ex));
        }
    }

    @Override
    public void getStructs(List<Struct> list) {
        list.add(header);
    }
}
