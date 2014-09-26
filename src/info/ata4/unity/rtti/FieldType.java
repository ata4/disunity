/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.rtti;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import java.util.Objects;

/**
 * Class that contains the runtime type of a single field.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldType implements Struct {
    
    public static final int FLAG_FORCE_ALIGN = 0x4000;
    
    // field type string
    private String type;
    
    // field name string
    private String name;
    
    // size of the field value in bytes or -1 if the field contains sub-fields only
    private int size;
    
    // field index for the associated parent field
    private int index;
    
    // set to 1 if "type" is "Array" or "TypelessData"
    private int isArray;
    
    // observed values: 1-5, 8
    private int version;
    
    // field flags
    // observed values:
    // 0x1
    // 0x10
    // 0x800
    // 0x4000
    // 0x8000
    // 0x200000
    // 0x400000
    private int metaFlag;
    
    public boolean isForceAlign() {
        return (metaFlag & FLAG_FORCE_ALIGN) != 0;
    }
    
    public void setForceAlign(boolean forceAlign) {
        if (forceAlign) {
            metaFlag |= FLAG_FORCE_ALIGN;
        } else {
            metaFlag &= ~FLAG_FORCE_ALIGN;
        }
    }

    public String getTypeName() {
        return type;
    }

    public void setTypeName(String type) {
        this.type = type;
    }

    public String getFieldName() {
        return name;
    }

    public void setFieldName(String name) {
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
        return isArray;
    }

    public void setArrayFlag(int arrayFlag) {
        this.isArray = arrayFlag;
    }

    public int getFlags1() {
        return version;
    }

    public void setFlags1(int flags1) {
        this.version = flags1;
    }

    public int getFlags2() {
        return metaFlag;
    }

    public void setFlags2(int flags2) {
        this.metaFlag = flags2;
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
        isArray = in.readInt();
        version = in.readInt();
        metaFlag = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeStringNull(type);
        out.writeStringNull(name);
        out.writeInt(size);
        out.writeInt(index);
        out.writeInt(isArray);
        out.writeInt(version);
        out.writeInt(metaFlag);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldType other = (FieldType) obj;
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
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.type);
        hash = 31 * hash + Objects.hashCode(this.name);
        hash = 31 * hash + this.size;
        hash = 31 * hash + this.index;
        hash = 31 * hash + this.isArray;
        return hash;
    }
}