/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.unity.util.UnityStruct;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for objects that contain the runtime type of a single field.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity TypeTree
 */
public class Type extends UnityStruct {
    
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
    private boolean isArray;
    
    // type version, starts with 1 and is incremented when the type
    // information is updated in a new Unity release
    //
    // equal to serializedVersion in YAML format files
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
    
    // Unity 5+, the level of this type within the type tree
    private int treeLevel;
    
    // Unity 5+, offset to the type string
    private int typeOffset;
    
    // Unity 5+, offset to the name string
    private int nameOffset;
    
    public Type(VersionInfo versionInfo) {
        super(versionInfo);
    }
    
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

    public String typeName() {
        return type;
    }

    public void typeName(String type) {
        this.type = type;
    }

    public String fieldName() {
        return name;
    }

    public void fieldName(String name) {
        this.name = name;
    }

    public int size() {
        return size;
    }

    public void size(int size) {
        this.size = size;
    }

    public int index() {
        return index;
    }

    public void index(int index) {
        this.index = index;
    }

    public boolean isArray() {
        return isArray;
    }

    public void isArray(boolean isArray) {
        this.isArray = isArray;
    }

    public int version() {
        return version;
    }

    public void version(int flags1) {
        this.version = flags1;
    }

    public int metaFlag() {
        return metaFlag;
    }

    public void metaFlag(int flags2) {
        this.metaFlag = flags2;
    }

    public String type() {
        return type;
    }

    public void type(String type) {
        this.type = type;
    }

    public int treeLevel() {
        return treeLevel;
    }

    public void treeLevel(int treeLevel) {
        this.treeLevel = treeLevel;
    }

    public int typeOffset() {
        return typeOffset;
    }

    public void typeOffset(int typeOffset) {
        this.typeOffset = typeOffset;
    }

    public int nameOffset() {
        return nameOffset;
    }

    public void nameOffset(int nameOffset) {
        this.nameOffset = nameOffset;
    }
    
    @Override
    public void read(DataReader in) throws IOException {
        if (versionInfo.assetVersion() > 13) {
            version = in.readShort();
            treeLevel = in.readUnsignedByte();
            isArray = in.readBoolean();
            typeOffset = in.readInt();
            nameOffset = in.readInt();
            size = in.readInt();
            index = in.readInt();
            metaFlag = in.readInt();
        } else {
            type = in.readStringNull(256);
            name = in.readStringNull(256);
            size = in.readInt();
            index = in.readInt();
            isArray = in.readInt() == 1;
            version = in.readInt();
            metaFlag = in.readInt();
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(type);
        out.writeStringNull(name);
        out.writeInt(size);
        out.writeInt(index);
        out.writeInt(isArray ? 1 : 0);
        out.writeInt(version);
        out.writeInt(metaFlag);
    }
    
    @Override
    public String toString() {
        return String.format("%s%s %s [v: %d, f: 0x%x, s: %d]",
                StringUtils.repeat("  ", treeLevel()), typeName(), fieldName(),
                version(), metaFlag(), size());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Type other = (Type) obj;
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
        hash = 31 * hash + (this.isArray ? 1 : 0);
        return hash;
    }
}