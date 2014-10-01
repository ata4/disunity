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

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.BufferedEntry;
import info.ata4.unity.assetbundle.StreamedEntry;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.FieldTypeNode;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeModel extends DefaultTreeModel {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final Set<DefaultMutableTreeNode> unloadedObjectDataNodes = new HashSet<>();
    private final Set<DefaultMutableTreeNode> unloadedAssetBundleEntryNodes = new HashSet<>();
    
    public AssetFileTreeModel(TreeNode root) {
        super(root);
    }
    
    public void addAssetBundleNodes(DefaultMutableTreeNode root, List<BufferedEntry> entries) throws IOException {
        for (BufferedEntry entry : entries) {
            DefaultMutableTreeNode current = root;

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

            DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(entry);
            if (entry.getInfo().isAsset()) {
                entryNode.add(new DefaultMutableTreeNode());
                unloadedAssetBundleEntryNodes.add(entryNode);
            }

            current.add(entryNode);
        }
    }
    
    public void addAssetNodes(DefaultMutableTreeNode root, AssetFile asset) {
        Map<String, DefaultMutableTreeNode> nodeCategories = new TreeMap<>();
        for (ObjectData objectData : asset.getObjects()) {
            try {
                String fieldNodeType = objectData.getTypeTree().getType().getTypeName();
                
                if (!nodeCategories.containsKey(fieldNodeType)) {
                    DefaultMutableTreeNode nodeCategory = new DefaultMutableTreeNode(fieldNodeType);
                    nodeCategories.put(fieldNodeType, nodeCategory);
                }
                
                DefaultMutableTreeNode objectDataNode = new DefaultMutableTreeNode(objectData);
                objectDataNode.add(new DefaultMutableTreeNode());
                unloadedObjectDataNodes.add(objectDataNode);
                nodeCategories.get(fieldNodeType).add(objectDataNode);
            } catch (RuntimeTypeException ex) {
                L.log(Level.WARNING, "Can't deserialize object " + objectData, ex);
                root.add(new DefaultMutableTreeNode(ex));
            }
        }
        
        for (DefaultMutableTreeNode treeNode : nodeCategories.values()) {
            root.add(treeNode);
        }
    }
    
    public void addFieldNode(DefaultMutableTreeNode root, FieldNode fieldNode) {
        Object fieldValue = fieldNode.getValue();
        DefaultMutableTreeNode treeNode = null;
        
        if (fieldValue instanceof FieldNode) {
            addFieldNode(root, (FieldNode) fieldValue);
        } else if (fieldValue instanceof List) {
            treeNode = new DefaultMutableTreeNode(fieldNode);
            List fieldList = (List) fieldValue;

            for (Object item : fieldList) {
                if (item instanceof FieldNode) {
                    addFieldNode(treeNode, (FieldNode) item);
                } else {
                    treeNode.add(new DefaultMutableTreeNode(item));
                }
            }
        } else {
            treeNode = new DefaultMutableTreeNode(fieldNode);
        }
        
        if (treeNode != null) {
            for (FieldNode childFieldNode : fieldNode) {
                addFieldNode(treeNode, childFieldNode);
            }
            
            root.add(treeNode);
        }
    }
    
    public void addFieldTypeNode(DefaultMutableTreeNode root, FieldTypeNode fieldTypeNode) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(fieldTypeNode);

        for (FieldTypeNode childFieldNode : fieldTypeNode) {
            addFieldTypeNode(treeNode, childFieldNode);
        }

        root.add(treeNode);
    }
    
    public boolean isObjectDataNodeUnloaded(DefaultMutableTreeNode node) {
        return unloadedObjectDataNodes.contains(node);
    }
    
    public void setObjectDataNodeLoaded(DefaultMutableTreeNode node) {
        unloadedObjectDataNodes.remove(node);
    }
    
    public boolean isAssetBundleEntryNodeUnloaded(DefaultMutableTreeNode node) {
        return unloadedAssetBundleEntryNodes.contains(node);
    }
    
    public void setAssetBundleEntryNodeLoaded(DefaultMutableTreeNode node) {
        unloadedAssetBundleEntryNodes.remove(node);
    }
}
