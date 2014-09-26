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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import info.ata4.unity.util.ClassID;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectPath implements Struct {
    
    // Path ID, normally in sequence to the file position
    private int pathID;
    
    // Object data offset
    private int offset;
    
    // Object data size
    private int length;
    
    // Type ID, equal to classID if it's not a MonoBehaviour
    private int typeID;
    
    // Class ID, probably something else in asset format <=5
    private int classID;
    
    // set to 1 if destroyed object instances are stored?
    private short isDestroyed;

    @Override
    public void read(DataInputReader in) throws IOException {
        pathID = in.readInt();
        offset = in.readInt();
        length = in.readInt();
        typeID = in.readInt();
        classID = in.readShort();
        isDestroyed = in.readShort();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(pathID);
        out.writeInt(offset);
        out.writeInt(length);
        out.writeInt(typeID);
        out.writeShort(classID);
        out.writeShort(isDestroyed);
    }

    public int getPathID() {
        return pathID;
    }

    public void setPathID(int pathID) {
        this.pathID = pathID;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
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
    
    @Override
    public String toString() {
        String className = ClassID.getNameForID(getClassID(), true);
        return String.format("Object #%d (ClassID: %d, Class: %s)", getPathID(), getClassID(), className);
    }
}