/*
 ** 2014 September 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import info.ata4.util.collection.Node;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeNode extends Node<FieldTypeNode> implements Struct {
    
    private FieldType type = new FieldType();

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType field) {
        this.type = field;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        type.read(in);
        
        int numChildren = in.readInt();
        for (int i = 0; i < numChildren; i++) {
            FieldTypeNode child = new FieldTypeNode();
            child.read(in);
            children.add(child);
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        type.write(out);
        
        int numChildren = children.size();
        out.writeInt(numChildren);
        for (FieldTypeNode child : this) {
            child.write(out);
        }
    }
}
