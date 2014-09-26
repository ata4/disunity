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

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.FieldType;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.asset.ObjectPath;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.unity.rtti.RuntimeTypeException;
import info.ata4.util.collection.TreeNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeModel extends DefaultTreeModel {

    public AssetFileTreeModel(Path file) throws IOException {
        super(new DefaultMutableTreeNode(file));
        
        if (AssetBundleUtils.isAssetBundle(file)) {
            try (AssetBundleReader assetBundle = new AssetBundleReader(file)) {
                addAssetBundle(assetBundle);
            }
        } else {
            AssetFile asset = new AssetFile();
            asset.load(file);

            addAsset(asset);
        }
    }
    
    public AssetFileTreeModel(AssetFile asset) {
        super(new DefaultMutableTreeNode(asset.getSourceFile()));
        addAsset(asset);
    }
    
    private void addAssetBundle(AssetBundleReader assetBundle) throws IOException {
        for (AssetBundleEntry entry : assetBundle.getEntries()) {
            DefaultMutableTreeNode current = (DefaultMutableTreeNode) root;

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

            current.add(new DefaultMutableTreeNode(entry));
        }
    }
    
    private void addAsset(AssetFile asset) {
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
        
        DefaultMutableTreeNode rootMutable = (DefaultMutableTreeNode) root;
        for (DefaultMutableTreeNode treeNode : nodeCategories.values()) {
            rootMutable.add(treeNode);
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
