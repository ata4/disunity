/*
 ** 2013 June 17
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectTable implements Struct {
    
    private static final Logger L = Logger.getLogger(ObjectTable.class.getName());
    
    private List<ObjectPath> paths = new ArrayList<>();
    private List<AssetRef> refs = new ArrayList<>();
    public byte unknown;

    @Override
    public void read(DataInputReader in) throws IOException {
        int pathEntries = in.readInt();
        L.log(Level.FINEST, "pathEntries = {0}", pathEntries);
        
        for (int i = 0; i < pathEntries; i++) {
            ObjectPath path = new ObjectPath();
            path.read(in);
            paths.add(path);
        }
        
        int refEntries = in.readInt();
        L.log(Level.FINEST, "refEntries = {0}", refEntries);
        
        for (int i = 0; i < refEntries; i++) {
            AssetRef ref = new AssetRef();
            ref.read(in);
            refs.add(ref);
        }
        
        unknown = in.readByte();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        int pathEntries = paths.size();
        out.writeInt(pathEntries);
        L.log(Level.FINEST, "pathEntries = {0}", pathEntries);
        
        for (ObjectPath path : paths) {
            path.write(out);
        }
        
        int refEntries = refs.size();
        out.writeInt(refEntries);
        L.log(Level.FINEST, "refEntries = {0}", refEntries);
        
        for (AssetRef ref : refs) {
            ref.write(out);
        }
        
        out.writeByte(unknown);
    }

    public List<ObjectPath> getPaths() {
        return paths;
    }

    public List<AssetRef> getRefs() {
        return refs;
    }
    
}
