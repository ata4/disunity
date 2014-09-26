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
import info.ata4.unity.rtti.RuntimeTypeException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeModel extends DefaultTreeModel {
    
    public AssetFileTreeModel(AssetFile asset) {
        super(new DefaultMutableTreeNode(asset.getSourceFile()));
        
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
