/*
 ** 2013 June 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct.asset;

import info.ata4.unity.asset.AssetFormat;
import info.ata4.unity.serdes.SerializedInput;
import info.ata4.unity.struct.Struct;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetStruct implements Struct {
    
    protected SerializedInput in;
    protected final AssetFormat formatInfo;
    
    public AssetStruct(AssetFormat formatInfo) {
        this.formatInfo = formatInfo;
    }
    
    @Override
    public final void read(DataInputReader in) throws IOException {
        this.in = new SerializedInput(in);
        readData();
    }

    @Override
    public final void write(DataOutputWriter out) throws IOException {
        // no write implementations yet, there's research to be done first
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public abstract void readData() throws IOException;
}
