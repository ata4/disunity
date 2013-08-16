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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectPathTable extends ArrayList<ObjectPath> implements Struct {
    
    private static final Logger L = Logger.getLogger(ObjectPathTable.class.getName());
    
    @Override
    public void read(DataInputReader in) throws IOException {
        int entries = in.readInt();
        L.log(Level.FINEST, "entries = {0}", entries);
        
        for (int i = 0; i < entries; i++) {
            ObjectPath path = new ObjectPath();
            path.read(in);
            add(path);
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        int entries = size();
        out.writeInt(entries);
        L.log(Level.FINEST, "entries = {0}", entries);
        
        for (ObjectPath path : this) {
            path.write(out);
        }
    }    
}
