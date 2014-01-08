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

import info.ata4.unity.util.ClassID;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetObjectPath implements Struct {
    
    // Path ID, normally in sequence to the file position
    private int pathID;
    
    // Object data offset
    private int offset;
    
    // Object data size
    private int length;
    
    // Script ID as negative number, equal to classID if it's not a MonoBehaviour
    private int scriptID;
    
    // Class ID
    private int classID;

    @Override
    public void read(DataInputReader in) throws IOException {
        pathID = in.readInt();
        offset = in.readInt();
        length = in.readInt();
        scriptID = in.readInt();
        classID = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(pathID);
        out.writeInt(offset);
        out.writeInt(length);
        out.writeInt(scriptID);
        out.writeInt(classID);
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
        return scriptID < 0;
    }

    public int getScriptID() {
        return scriptID;
    }

    public void setScriptID(int scriptID) {
        this.scriptID = scriptID;
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