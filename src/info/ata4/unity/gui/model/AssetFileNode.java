/*
 ** 2014 October 02
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
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.AssetHeader;
import info.ata4.unity.assetbundle.BundleEntryBuffered;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileNode extends LazyLoadingTreeNode implements StructNode {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final Path file;
    private final BundleEntryBuffered bundleEntry;
    private AssetHeader header;
    
    public AssetFileNode(JTree tree, Path file) {
        super(tree, file);
        this.file = file;
        this.bundleEntry = null;
    }

    public AssetFileNode(JTree tree, BundleEntryBuffered bundleEntry) {
        super(tree, bundleEntry);
        this.file = null;
        this.bundleEntry = bundleEntry;
    }

    public Path getFile() {
        return file;
    }

    public BundleEntryBuffered getBundleEntry() {
        return bundleEntry;
    }
    
    public AssetHeader getHeader() {
        return header;
    }

    @Override
    protected void doLoad() {
        try {
            AssetFile asset = new AssetFile();
            if (file != null) {
                asset.load(file);
            } else {
                asset.load(bundleEntry.getReader());
            }
            
            header = asset.getHeader();
            
            Map<String, DefaultMutableTreeNode> nodeCategories = new TreeMap<>();
            for (ObjectData objectData : asset.getObjects()) {
                try {
                    String fieldNodeType = objectData.getTypeTree().getType().getTypeName();

                    if (!nodeCategories.containsKey(fieldNodeType)) {
                        DefaultMutableTreeNode nodeCategory = new DefaultMutableTreeNode(fieldNodeType);
                        nodeCategories.put(fieldNodeType, nodeCategory);
                    }

                    nodeCategories.get(fieldNodeType).add(new ObjectDataNode(tree, objectData));
                } catch (RuntimeTypeException ex) {
                    L.log(Level.WARNING, "Can't deserialize object " + objectData, ex);
                    add(new DefaultMutableTreeNode(ex));
                }
            }

            for (DefaultMutableTreeNode treeNode : nodeCategories.values()) {
                add(treeNode);
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't load asset data", ex);
            add(new DefaultMutableTreeNode(ex));
        }
    }

    @Override
    public void getStructs(List<Struct> list) {
        if (bundleEntry != null) {
            list.add(bundleEntry.getInfo());
        }
        
        list.add(header);
    }

}
