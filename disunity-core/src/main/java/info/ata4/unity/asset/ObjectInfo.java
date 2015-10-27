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
import info.ata4.unity.util.UnityClass;
import info.ata4.unity.util.UnityStruct;
import java.io.IOException;

/**
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFile::ObjectInfo
 */
public class ObjectInfo extends UnityStruct {
    
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
    
    public ObjectInfo(VersionInfo versionInfo) {
        super(versionInfo);
    }

    @Override
    public void read(DataReader in) throws IOException {
        offset = in.readUnsignedInt();
        length = in.readUnsignedInt();
        typeID = in.readInt();
        classID = in.readShort();
        isDestroyed = in.readShort();
        if (versionInfo.assetVersion()>=15)
            in.readInt();

        assert typeID == classID || (classID == 114 && typeID < 0);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(offset);
        out.writeUnsignedInt(length);
        out.writeInt(typeID);
        out.writeShort((short) classID);
        out.writeShort(isDestroyed);
    }

    public long offset() {
        return offset;
    }

    public void offset(long offset) {
        this.offset = offset;
    }

    public long length() {
        return length;
    }

    public void length(long length) {
        this.length = length;
    }
    
    public boolean isScript() {
        return typeID < 0;
    }

    public int typeID() {
        return typeID;
    }

    public void typeID(int typeID) {
        this.typeID = typeID;
    }
    
    public int classID() {
        return classID;
    }

    public void classID(int classID) {
        this.classID = classID;
    }
    
    public UnityClass unityClass() {
        return new UnityClass(classID);
    }
    
    @Override
    public String toString() {
        return unityClass().toString();
    }
}