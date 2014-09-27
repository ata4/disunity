/*
 ** 2014 September 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.model;

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.ObjectPath;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeModel extends DefaultTreeModel {
    
    private DefaultMutableTreeNode rootNode;

    public AssetFileTreeModel(Path file) throws IOException {
        super(null);
        
        rootNode = new DefaultMutableTreeNode(file);
        root = rootNode;
        
        if (AssetBundleUtils.isAssetBundle(file)) {
            try (AssetBundleReader assetBundle = new AssetBundleReader(file)) {
                addAssetBundle(rootNode, assetBundle);
            }
        } else {
            AssetFile asset = new AssetFile();
            asset.load(file);

            addAsset(rootNode, asset);
        }
    }
    
    public AssetFileTreeModel(AssetFile asset) {
        super(new DefaultMutableTreeNode(asset.getSourceFile()));
        addAsset(rootNode, asset);
    }
    
    private void addAssetBundle(DefaultMutableTreeNode root, AssetBundleReader assetBundle) throws IOException {
        for (AssetBundleEntry entry : assetBundle) {
            DefaultMutableTreeNode current = root;

            // create folders in case the name contains path separators
            String[] parts = StringUtils.split(entry.getName(), '/');
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
            
            DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(entry);
            
            if (!FilenameUtils.getExtension(entry.getName()).equals("dll")) {
                if (entry.getLength() < Integer.MAX_VALUE) {
                    ByteBuffer bb = ByteBuffer.allocateDirect((int) entry.getLength());
                    InputStream is = entry.getInputStream();
                    OutputStream os = new ByteBufferOutputStream(bb);
                    IOUtils.copyLarge(is, os);
                    bb.flip();
                    
                    AssetFile asset = new AssetFile();
                    asset.load(DataInputReader.newReader(bb));
                    
                    addAsset(entryNode, asset);
                } else {
                    // TODO
                }
            }

            current.add(entryNode);
        }
    }
    
    private void addAsset(DefaultMutableTreeNode root, AssetFile asset) {
        Map<String, DefaultMutableTreeNode> nodeCategories = new TreeMap<>();
        for (ObjectData objectData : asset.getObjects()) {
            try {
                ObjectPath path = objectData.getPath();
                FieldNode fieldNode = objectData.getInstance();
                String fieldNodeType = fieldNode.getType().getTypeName();
                
                if (!nodeCategories.containsKey(fieldNodeType)) {
                    DefaultMutableTreeNode nodeCategory = new DefaultMutableTreeNode(fieldNodeType);
                    nodeCategories.put(fieldNodeType, nodeCategory);
                }
                
                DefaultMutableTreeNode objectDataNode = new DefaultMutableTreeNode(objectData);
                
                for (FieldNode childFieldNode : fieldNode) {
                    objectDataNode.add(convertNode(childFieldNode, path));
                }
                
                nodeCategories.get(fieldNodeType).add(objectDataNode);
            } catch (RuntimeTypeException ex) {
                ex.printStackTrace();
            }
        }
        
        for (DefaultMutableTreeNode treeNode : nodeCategories.values()) {
            root.add(treeNode);
        }
    }
    
    private DefaultMutableTreeNode convertNode(FieldNode fieldNode, ObjectPath path) {
        Object fieldValue = fieldNode.getValue();
        DefaultMutableTreeNode treeNode;
        
        if (fieldValue instanceof FieldNode) {
            treeNode = convertNode((FieldNode) fieldValue, path);
        } else if (fieldValue instanceof List) {
            List fieldList = (List) fieldValue;
            treeNode = new DefaultMutableTreeNode(fieldNode);
            
            for (Object item : fieldList) {
                if (item instanceof FieldNode) {
                    treeNode.add(convertNode((FieldNode) item, path));
                } else {
                    treeNode.add(new DefaultMutableTreeNode(item));
                }
            }
        } else {
            treeNode = new DefaultMutableTreeNode(fieldNode);
        }
        
        for (FieldNode childFieldNode : fieldNode) {
            treeNode.add(convertNode(childFieldNode, path));
        }
        
        return treeNode;
    }
    
}
