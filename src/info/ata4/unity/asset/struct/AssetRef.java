/*
 ** 2013 July 12
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
public class AssetRef implements Struct {
    
    public byte[] guid = new byte[16];
    public String filePath;
    public String assetPath;
    public int type;

    @Override
    public void read(DataInputReader in) throws IOException {
        in.readFully(guid);
        type = in.readInt();
        filePath = in.readStringNull();
        assetPath = in.readStringNull();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.write(guid);
        out.writeInt(type);
        out.writeStringNull(filePath);
        out.writeStringNull(assetPath);
    }
}
