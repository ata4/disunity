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
    
    // Script ID as negative number, equal to classID if it's not a MonoBehaviour
    private int classID1;
    
    // Class ID, probably something else in asset format <=5
    private int classID2;

    @Override
    public void read(DataInputReader in) throws IOException {
        pathID = in.readInt();
        offset = in.readInt();
        length = in.readInt();
        classID1 = in.readInt();
        classID2 = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(pathID);
        out.writeInt(offset);
        out.writeInt(length);
        out.writeInt(classID1);
        out.writeInt(classID2);
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
        return classID1 < 0;
    }

    public int getClassID1() {
        return classID1;
    }

    public void setClassID1(int classID1) {
        this.classID1 = classID1;
    }
    
    public int getClassID2() {
        return classID2;
    }

    public void setClassID2(int classID2) {
        this.classID2 = classID2;
    }
    
    public int getClassID() {
        return classID1 > 0 ? classID1 : classID2;
    }
    
    @Override
    public String toString() {
        String className = ClassID.getNameForID(getClassID(), true);
        return String.format("Object #%d (ClassID: %d, Class: %s)", getPathID(), getClassID(), className);
    }
}