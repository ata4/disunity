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
import info.ata4.unity.struct.Struct;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetStruct implements Struct {
    
    private static final int ALIGN = 4;
    
    private DataInputReader in;
    private int booleans;
    
    protected final AssetFormat formatInfo;
    
    public AssetStruct(AssetFormat formatInfo) {
        this.formatInfo = formatInfo;
    }
    
    @Override
    public final void read(DataInputReader in) throws IOException {
        this.in = in;
        readData();
    }

    @Override
    public final void write(DataOutputWriter out) throws IOException {
        // no write implementations yet, there's research to be done first
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public abstract void readData() throws IOException;
    
    protected boolean readBoolean() throws IOException {
        booleans++;
        return in.readBoolean();
    }
    
    private void doPadding() throws IOException {
        if (booleans > 0) {
            in.align(booleans, ALIGN);
            booleans = 0;
        }
    }
    
    protected int readInt() throws IOException {
        doPadding();
        return in.readInt();
    }
    
    protected float readFloat() throws IOException {
        doPadding();
        return in.readFloat();
    }
    
    protected double readDouble() throws IOException {
        doPadding();
        return in.readDouble();
    }
    
    protected byte[] readByteArray() throws IOException {
        doPadding();
        int len = in.readInt();
        byte[] data = new byte[len];
        in.readFully(data);
        in.align(len, ALIGN);
        return data;
    }
    
    protected String readString() throws IOException {
        return new String(readByteArray(), "UTF8");
    }
    
    protected void readObject(AssetStruct obj) throws IOException {
        doPadding();
        obj.read(in);
    }
}
