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
public class AssetHeader implements Struct {
    
    private static final Logger L = Logger.getLogger(AssetHeader.class.getName());

    // size of the structure data
    public int treeSize;
    
    // size of the whole asset file
    public int fileSize;
    
    // 5 = 2.0
    // 6 = 2.6
    // 7 = ???
    // 8 = 3.1 - 3.4
    // 9 = 3.5 - 4.1
    public int format;
    
    // apparently the offset to the serialized data
    public int dataOffset;
    
    // always 0?
    public int unknown;

    @Override
    public void read(DataInputReader in) throws IOException {
        treeSize = in.readInt();
        L.log(Level.FINEST, "treeSize = {0}", treeSize);
        
        fileSize = in.readInt();
        L.log(Level.FINEST, "fileSize = {0}", fileSize);
        
        format = in.readInt();
        L.log(Level.FINEST, "format = {0}", format);
        
        dataOffset = in.readInt();
        L.log(Level.FINEST, "dataOffset = {0}", dataOffset);
        
        unknown = in.readInt();
        L.log(Level.FINEST, "unknown = {0}", unknown);
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(treeSize);
        L.log(Level.FINEST, "treeSize = {0}", treeSize);
        
        out.writeInt(fileSize);
        L.log(Level.FINEST, "fileSize = {0}", fileSize);
        
        out.writeInt(format);
        L.log(Level.FINEST, "format = {0}", format);
        
        out.writeInt(dataOffset);
        L.log(Level.FINEST, "dataOffset = {0}", dataOffset);
        
        out.writeInt(unknown);
        L.log(Level.FINEST, "unknown = {0}", unknown);
    }
}
