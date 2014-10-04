/*
 ** 2014 October 04
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.util;

import info.ata4.unity.gui.model.StructMutableTreeNode;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.FieldTypeNode;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldNodeUtils {
    
    private FieldNodeUtils() {
    }
    
    public static void convertFieldTypeNode(DefaultMutableTreeNode root, FieldTypeNode fieldTypeNode) {
        DefaultMutableTreeNode treeNode = new StructMutableTreeNode(fieldTypeNode, fieldTypeNode.getType());

        for (FieldTypeNode childFieldNode : fieldTypeNode) {
            convertFieldTypeNode(treeNode, childFieldNode);
        }

        root.add(treeNode);
    }
    
    public static void convertFieldNode(DefaultMutableTreeNode root, FieldNode fieldNode) {
        Object fieldValue = fieldNode.getValue();
        
        if (fieldValue instanceof FieldNode) {
            convertFieldNode(root, (FieldNode) fieldValue);
            return;
        } 
        
        DefaultMutableTreeNode treeNode = new StructMutableTreeNode(fieldNode, fieldNode.getType());
        
        if (fieldValue instanceof List) {
            List fieldList = (List) fieldValue;

            for (Object item : fieldList) {
                if (item instanceof FieldNode) {
                    convertFieldNode(treeNode, (FieldNode) item);
                } else {
                    treeNode.add(new DefaultMutableTreeNode(item));
                }
            }
        }
        
        for (FieldNode childFieldNode : fieldNode) {
            convertFieldNode(treeNode, childFieldNode);
        }

        root.add(treeNode);
    }
}
