/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct;

import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectPath implements Struct {
    
    private static final Logger L = Logger.getLogger(ObjectPath.class.getName());

    public int pathID;
    public int offset;
    public int length;
    public int classID1;
    public int classID2;

    @Override
    public void read(DataInputReader in) throws IOException {
        pathID = in.readInt();
        L.log(Level.FINEST, "pathID = {0}", pathID);
        
        offset = in.readInt();
        L.log(Level.FINEST, "offset = {0}", offset);
        
        length = in.readInt();
        L.log(Level.FINEST, "length = {0}", length);
        
        classID1 = in.readInt();
        L.log(Level.FINEST, "classID1 = {0}", classID1);
        
        classID2 = in.readInt();
        L.log(Level.FINEST, "classID2 = {0}", classID2);
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(pathID);
        L.log(Level.FINEST, "pathID = {0}", pathID);
        
        out.writeInt(offset);
        L.log(Level.FINEST, "offset = {0}", offset);
        
        out.writeInt(length);
        L.log(Level.FINEST, "length = {0}", length);
        
        out.writeInt(classID1);
        L.log(Level.FINEST, "classID1 = {0}", classID1);
        
        out.writeInt(classID2);
        L.log(Level.FINEST, "classID2 = {0}", classID2);
    }
}