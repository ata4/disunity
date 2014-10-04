/*
 ** 2014 October 04
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.model;

import info.ata4.io.Struct;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StructMutableTreeNode extends DefaultMutableTreeNode implements StructNode {
    
    private Struct struct;

    public StructMutableTreeNode() {
        super();
    }

    public StructMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public StructMutableTreeNode(Struct struct) {
        super(struct);
        this.struct = struct;
    }

    public StructMutableTreeNode(Object userObject, Struct struct) {
        super(userObject);
        this.struct = struct;
    }

    public Struct getStruct() {
        return struct;
    }

    public void setStruct(Struct struct) {
        this.struct = struct;
    }

    @Override
    public void getStructs(List<Struct> list) {
        list.add(struct);
    }
    
}
