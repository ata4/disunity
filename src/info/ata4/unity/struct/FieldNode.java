/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct;

import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldNode extends ArrayList<FieldNode> implements Struct {
    
    private static final Logger L = Logger.getLogger(FieldNode.class.getName());

    public String type;
    public String name;
    public int size;
    public int index;
    public int isArray;
    public int unknown1;
    public int flags;

    @Override
    public void read(DataInputReader in) throws IOException {
        type = in.readStringNull(256);
        L.log(Level.FINEST, "type = {0}", type);
        
        name = in.readStringNull(256);
        L.log(Level.FINEST, "name = {0}", name);
        
        size = in.readInt();
        L.log(Level.FINEST, "size = {0}", size);
        
        index = in.readInt();
        L.log(Level.FINEST, "index = {0}", index);
        
        isArray = in.readInt();
        L.log(Level.FINEST, "isArray = {0}", isArray);
        
        unknown1 = in.readInt();
        L.log(Level.FINEST, "unknown1 = {0}", unknown1);
        
        flags = in.readInt();
        L.log(Level.FINEST, "flags = {0}", flags);
        
        int subFields = in.readInt();
        L.log(Level.FINEST, "subFields = {0}", subFields);
        
        for (int i = 0; i < subFields; i++) {
            FieldNode fn = new FieldNode();
            fn.read(in);
            add(fn);
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeStringNull(type);
        L.log(Level.FINEST, "type = {0}", type);
        
        out.writeStringNull(name);
        L.log(Level.FINEST, "name = {0}", name);
        
        out.writeInt(size);
        L.log(Level.FINEST, "size = {0}", size);
        
        out.writeInt(index);
        L.log(Level.FINEST, "index = {0}", index);
        
        out.writeInt(isArray);
        L.log(Level.FINEST, "isArray = {0}", isArray);
        
        out.writeInt(unknown1);
        L.log(Level.FINEST, "unknown1 = {0}", unknown1);
        
        out.writeInt(flags);
        L.log(Level.FINEST, "flags = {0}", flags);
        
        int subFields = size();
        out.writeInt(subFields);
        L.log(Level.FINEST, "subFields = {0}", subFields);
        
        for (FieldNode subField : this) {
            subField.write(out);
        }
    }

    @Override
    public String toString() {
        return type + ":" + name;
    }
    
   @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldNode other = (FieldNode) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (this.isArray != other.isArray) {
            return false;
        }
        if (this.unknown1 != other.unknown1) {
            return false;
        }
        if (this.flags != other.flags) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + this.size;
        hash = 47 * hash + this.index;
        hash = 47 * hash + this.isArray;
        hash = 47 * hash + this.unknown1;
        hash = 47 * hash + this.flags;
        return hash;
    }
}