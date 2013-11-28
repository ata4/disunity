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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetObjectPath implements Struct {
    
    public int pathID;
    public int offset;
    public int length;
    public int classID1;
    public int classID2;

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
}