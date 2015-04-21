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
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
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
            FieldNode fieldNode = objectData.instance();

            for (FieldNode childFieldNode : fieldNode) {
                FieldNodeUtils.convertFieldNode(this, childFieldNode);
            }
        } catch (RuntimeTypeException ex) {
            add(new DefaultMutableTreeNode(ex));
        }
    }
}
