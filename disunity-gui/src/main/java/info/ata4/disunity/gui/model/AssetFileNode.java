/*
 ** 2014 October 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.model;

import info.ata4.disunity.gui.util.FieldNodeUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.BaseClass;
import info.ata4.unity.asset.FileIdentifier;
import info.ata4.unity.asset.TypeNode;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileNode extends DefaultMutableTreeNode {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final JTree tree;
    
    public AssetFileNode(JTree tree, AssetFile assetFile) {
        super(assetFile);
        
        this.tree = tree;
        
        if (!assetFile.isStandalone()) {
            addTypes(assetFile);
        }
        
        addObjects(assetFile);
        addExternals(assetFile);
    }
    
    private void addObjects(AssetFile asset) {
        Map<String, DefaultMutableTreeNode> nodeCategories = new TreeMap<>();
        for (ObjectData objectData : asset.objects()) {
            try {
                String fieldNodeType = objectData.typeTree().type().typeName();

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

        DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode("Objects");

        for (DefaultMutableTreeNode treeNode : nodeCategories.values()) {
            objectNode.add(treeNode);
        }

        add(objectNode);
    }
    
    private void addExternals(AssetFile asset) {
        List<FileIdentifier> externals = asset.externals();
        if (asset.externals().isEmpty()) {
            return;
        }
        
        DefaultMutableTreeNode refNode = new DefaultMutableTreeNode("Externals");
        
        for (FileIdentifier external : externals) {
            if (external.assetFile() != null) {
                refNode.add(new AssetFileNode(tree, external.assetFile()));
            } else {
                refNode.add(new DefaultMutableTreeNode(external));
            }
        }
        
        add(refNode);
    }
    
    private void addTypes(AssetFile asset) {
        if (asset.isStandalone()) {
            return;
        }
        
        DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode("Types");
        Set<TypeNode> typeNodes = new TreeSet<>(new TypeNodeComparator());
        
        for (BaseClass baseClass : asset.typeTree().values()) {
            typeNodes.add(baseClass.typeTree());
        }
        
        for (TypeNode fieldNode : typeNodes) {
            FieldNodeUtils.convertFieldTypeNode(typeNode, fieldNode);
        }
 
        add(typeNode);
    }
}
