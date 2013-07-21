/*
 ** 2013 July 12
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
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetRef implements Struct {
    
    private static final Logger L = Logger.getLogger(AssetRef.class.getName());
    
    public byte unknown;
    public byte[] guid = new byte[16];
    public String path;
    public int type;

    @Override
    public void read(DataInputReader in) throws IOException {
        unknown = in.readByte();
        L.log(Level.FINEST, "unknown = {0}", unknown);
        
        in.readFully(guid);
        L.log(Level.FINEST, "guid = {0}", DatatypeConverter.printHexBinary(guid));
        
        type = in.readInt();
        L.log(Level.FINEST, "type = {0}", type);
        
        path = in.readStringNull();
        L.log(Level.FINEST, "path = {0}", path);
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeByte(unknown);
        out.write(guid);
        out.writeInt(type);
        out.writeStringNull(path);
    }
}
