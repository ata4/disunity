/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle.struct;

import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleHeader implements Struct {
    
    // always 0?
    public int unknown1;
    
    // major version byte 
    public byte fileVersion;
    
    // file version
    public String version;
    
    // engine revision
    public String revision;
    
    // size of the whole file
    public int fileSize;
    
    // offset to the bundle data
    public int dataOffset;
    
    // unknown flags
    public int flag1;
    public int flag2;
    
    // compressed data size
    public int dataSizeCompressed;
    
    // uncompressed data size, equal to dataSizeCompressed in UnityRaw
    public int dataSizeUncompressed;
    
    // always equal to fileSize?
    public int fileSize2;
    
    // possible values: 52
    public int unknown2;
    
    @Override
    public void read(DataInputReader in) throws IOException {
        unknown1 = in.readInt();
        fileVersion = in.readByte();
        version = in.readStringNull(255);
        revision = in.readStringNull(255);
        fileSize = in.readInt();
        dataOffset = in.readInt();
        flag1 = in.readInt();
        flag2 = in.readInt();
        dataSizeCompressed = in.readInt();
        dataSizeUncompressed = in.readInt();
        fileSize2 = in.readInt();
        unknown2 = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
