/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset.struct;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class that contains the runtime type of a single field.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeField implements Struct {
    
    public static final int FLAG_FORCE_ALIGN = 0x4000;
    
    // child fields
    private final List<TypeField> children = new ArrayList<>();

    // field type string
    private String type;
    
    // field name string
    private String name;
    
    // size of the field value in bytes or -1 if the field contains sub-fields only
    private int size;
    
    // field index for the associated parent field
    private int index;
    
    // set to 1 if "type" is "Array" or "TypelessData"
    private int arrayFlag;
    
    // observed values: 1-5, 8
    private int flags1;
    
    // field flags
    // observed values:
    // 0x1
    // 0x10
    // 0x800
    // 0x4000
    // 0x8000
    // 0x200000
    // 0x400000
    private int flags2;
    
    public boolean isForceAlign() {
        return (flags2 & FLAG_FORCE_ALIGN) != 0;
    }
    
    public void setForceAlign(boolean forceAlign) {
        if (forceAlign) {
            flags2 |= FLAG_FORCE_ALIGN;
        } else {
            flags2 &= ~FLAG_FORCE_ALIGN;
        }
    }
    
    public List<TypeField> getChildren() {
        return children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getArrayFlag() {
        return arrayFlag;
    }

    public void setArrayFlag(int arrayFlag) {
        this.arrayFlag = arrayFlag;
    }

    public int getFlags1() {
        return flags1;
    }

    public void setFlags1(int flags1) {
        this.flags1 = flags1;
    }

    public int getFlags2() {
        return flags2;
    }

    public void setFlags2(int flags2) {
        this.flags2 = flags2;
    }
    
    @Override
    public String toString() {
        return type + ":" + name;
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        type = in.readStringNull(256);
        name = in.readStringNull(256);
        size = in.readInt();
        index = in.readInt();
        arrayFlag = in.readInt();
        flags1 = in.readInt();
        flags2 = in.readInt();

        int numChildren = in.readInt();
        for (int i = 0; i < numChildren; i++) {
            TypeField fn = new TypeField();
            fn.read(in);
            children.add(fn);
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeStringNull(type);
        out.writeStringNull(name);
        out.writeInt(size);
        out.writeInt(index);
        out.writeInt(arrayFlag);
        out.writeInt(flags1);
        out.writeInt(flags2);
        
        out.writeInt(children.size());
        for (TypeField subField : children) {
            subField.write(out);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeField other = (TypeField) obj;
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
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
        if (this.arrayFlag != other.arrayFlag) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.children);
        hash = 31 * hash + Objects.hashCode(this.type);
        hash = 31 * hash + Objects.hashCode(this.name);
        hash = 31 * hash + this.size;
        hash = 31 * hash + this.index;
        hash = 31 * hash + this.arrayFlag;
        return hash;
    }
}