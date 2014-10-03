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

import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectDataNode extends LazyLoadingTreeNode {
    
    private final ObjectData objectData;

    public ObjectDataNode(JTree tree, ObjectData objectData) {
        super(tree, objectData);
        this.objectData = objectData;
    }

    @Override
    protected void doLoad() {
        try {
            FieldNode fieldNode = objectData.getInstance();

            for (FieldNode childFieldNode : fieldNode) {
                addFieldNode(this, childFieldNode);
            }
        } catch (RuntimeTypeException ex) {
            add(new DefaultMutableTreeNode(ex));
        }
    }
    
    private void addFieldNode(DefaultMutableTreeNode root, FieldNode fieldNode) {
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
    
}
