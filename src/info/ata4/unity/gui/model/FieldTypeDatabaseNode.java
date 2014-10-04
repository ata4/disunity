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

import info.ata4.unity.rtti.FieldTypeDatabase;
import info.ata4.unity.rtti.FieldTypeMap;
import info.ata4.unity.rtti.FieldTypeNode;
import info.ata4.unity.util.UnityVersion;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeDatabaseNode extends DefaultMutableTreeNode {

    public FieldTypeDatabaseNode(FieldTypeDatabase db) {
        super(Paths.get("structdb.dat"));
        
        Map<UnityVersion, DefaultMutableTreeNode> versionNodes = new TreeMap<>();

        FieldTypeMap map = db.getFieldTypeMap();
        for (Map.Entry<Pair<Integer, UnityVersion>, FieldTypeNode> entry : map.entrySet()) {
            UnityVersion version = entry.getKey().getValue();
            
            if (!versionNodes.containsKey(version)) {
                versionNodes.put(version, new DefaultMutableTreeNode(version));
            }
            
            addFieldTypeNode(versionNodes.get(version), entry.getValue());
        }
        
        for (DefaultMutableTreeNode node : versionNodes.values()) {
            add(node);
        }
    }
    
    private void addFieldTypeNode(DefaultMutableTreeNode root, FieldTypeNode fieldTypeNode) {
        DefaultMutableTreeNode treeNode = new StructMutableTreeNode(fieldTypeNode, fieldTypeNode.getType());

        for (FieldTypeNode childFieldNode : fieldTypeNode) {
            addFieldTypeNode(treeNode, childFieldNode);
        }

        root.add(treeNode);
    }
}
