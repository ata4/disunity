/*
 ** 2013 August 16
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
public class ExternalReferenceTable extends ArrayList<ExternalReference> implements Struct {

    private static final Logger L = Logger.getLogger(ExternalReferenceTable.class.getName());
    
    public byte unknown;
    
    @Override
    public void read(DataInputReader in) throws IOException {
        int entries = in.readInt();
        L.log(Level.FINEST, "entries = {0}", entries);
        
        unknown = in.readByte();
        
        for (int i = 0; i < entries; i++) {
            ExternalReference ref = new ExternalReference();
            ref.read(in);
            add(ref);
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        int entries = size();
        out.writeInt(entries);
        L.log(Level.FINEST, "entries = {0}", entries);
        
        for (ExternalReference ref : this) {
            ref.write(out);
        }
        
        out.writeByte(unknown); 
    }
}
