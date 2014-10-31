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

import info.ata4.unity.gui.util.FieldNodeUtils;
import info.ata4.unity.rtti.FieldTypeDatabase;
import info.ata4.unity.asset.FieldTypeNode;
import info.ata4.unity.rtti.FieldTypeNodeComparator;
import info.ata4.unity.util.UnityVersion;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeDatabaseNode extends DefaultMutableTreeNode {

    public FieldTypeDatabaseNode(FieldTypeDatabase db) {
        super(Paths.get(FieldTypeDatabase.FILENAME));
        
        // first step: create a mapping between all versions and their sets of
        // field type nodes
        Map<UnityVersion, Set<FieldTypeNode>> versionNodes = new TreeMap<>();

        Map<Pair<Integer, UnityVersion>, FieldTypeNode> map = db.getFieldTypeMap();
        for (Map.Entry<Pair<Integer, UnityVersion>, FieldTypeNode> entry : map.entrySet()) {
            UnityVersion version = entry.getKey().getValue();
            
            if (!versionNodes.containsKey(version)) {
                versionNodes.put(version, new TreeSet<>(new FieldTypeNodeComparator()));
            }
            
            versionNodes.get(version).add(entry.getValue());
        }
        
        // second step: convert the map to a node structure
        for (Map.Entry<UnityVersion, Set<FieldTypeNode>> entry : versionNodes.entrySet()) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(entry.getKey());
            
            for (FieldTypeNode typeNode : entry.getValue()) {
                FieldNodeUtils.convertFieldTypeNode(treeNode, typeNode);
            }
            
            add(treeNode);
        }
    }
}
