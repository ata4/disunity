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

import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.Struct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFieldType extends ArrayList<AssetFieldType> implements Struct {
    
    public static final int FLAG_FORCE_ALIGN = 0x4000;

    // field type string
    public String type;
    
    // field name string
    public String name;
    
    // size of the field value in bytes or -1 if the field contains sub-fields only
    public int size;
    
    // field index for the associated parent field
    public int index;
    
    // set to 1 if "type" is "Array" or "TypelessData"
    public int arrayFlag;
    
    // observed values: 1-5, 8
    public int flags1;
    
    // field flags
    // observed values:
    // 0x1
    // 0x10
    // 0x800
    // 0x4000
    // 0x8000
    // 0x200000
    // 0x400000
    public int flags2;
    
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
    
    @Override
    public void read(DataInputReader in) throws IOException {
        type = in.readStringNull(256);
        name = in.readStringNull(256);
        size = in.readInt();
        index = in.readInt();
        arrayFlag = in.readInt();
        flags1 = in.readInt();
        flags2 = in.readInt();

        int children = in.readInt();

        for (int i = 0; i < children; i++) {
            AssetFieldType fn = new AssetFieldType();
            fn.read(in);
            add(fn);
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
        
        int children = size();
        out.writeInt(children);
        
        for (AssetFieldType subField : this) {
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
        final AssetFieldType other = (AssetFieldType) obj;
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
//        if (this.flags1 != other.flags1) {
//            return false;
//        }
//        if (this.flags2 != other.flags2) {
//            return false;
//        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + this.size;
        hash = 47 * hash + this.index;
        hash = 47 * hash + this.arrayFlag;
//        hash = 47 * hash + this.flags1;
//        hash = 47 * hash + this.flags2;
        return hash;
    }
}