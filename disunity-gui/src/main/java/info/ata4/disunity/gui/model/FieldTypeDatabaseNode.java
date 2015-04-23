/*
 ** 2014 October 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.model;

import info.ata4.disunity.gui.util.FieldNodeUtils;
import info.ata4.unity.asset.TypeNode;
import info.ata4.unity.util.TypeTreeDatabase;
import info.ata4.unity.util.TypeTreeDatabaseEntry;
import info.ata4.unity.util.TypeTreeUtils;
import info.ata4.unity.util.UnityVersion;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeDatabaseNode extends DefaultMutableTreeNode {

    public FieldTypeDatabaseNode() {
        super(Paths.get(TypeTreeDatabase.FILE_NAME));
        
        // first step: create a mapping between all versions and their sets of
        // field type nodes
        Map<UnityVersion, Set<TypeNode>> versionNodes = new TreeMap<>();

        for (TypeTreeDatabaseEntry entry : TypeTreeUtils.getDatabase().getEntries()) {
            UnityVersion version = entry.unityVersion();
            
            if (!versionNodes.containsKey(version)) {
                versionNodes.put(version, new TreeSet<>(new TypeNodeComparator()));
            }
            
            versionNodes.get(version).add(entry.typeNode());
        }
        
        // second step: convert the map to a node structure
        for (Map.Entry<UnityVersion, Set<TypeNode>> entry : versionNodes.entrySet()) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(entry.getKey());
            
            for (TypeNode typeNode : entry.getValue()) {
                FieldNodeUtils.convertFieldTypeNode(treeNode, typeNode);
            }
            
            add(treeNode);
        }
    }
}
