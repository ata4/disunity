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
import info.ata4.io.Struct;
import info.ata4.unity.util.UnityClass;
import java.io.IOException;

/**
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFile::ObjectInfo
 */
public class ObjectInfo implements Struct {
    
    // Object data offset
    private long offset;
    
    // Object data size
    private long length;
    
    // Type ID, equal to classID if it's not a MonoBehaviour
    private int typeID;
    
    // Class ID, probably something else in asset format <=5
    private int classID;
    
    // set to 1 if destroyed object instances are stored?
    private short isDestroyed;

    @Override
    public void read(DataReader in) throws IOException {
        offset = in.readUnsignedInt();
        length = in.readUnsignedInt();
        typeID = in.readInt();
        classID = in.readShort();
        isDestroyed = in.readShort();
        
        assert typeID == classID || (classID == 114 && typeID < 0);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(offset);
        out.writeUnsignedInt(length);
        out.writeInt(typeID);
        out.writeShort(classID);
        out.writeShort(isDestroyed);
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    public boolean isScript() {
        return typeID < 0;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }
    
    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }
    
    public UnityClass getUnityClass() {
        return new UnityClass(classID);
    }
    
    @Override
    public String toString() {
        return getUnityClass().toString();
    }
}