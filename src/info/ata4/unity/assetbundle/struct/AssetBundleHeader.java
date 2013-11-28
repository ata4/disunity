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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleHeader implements Struct {
    
    private static final Logger L = Logger.getLogger(AssetBundleHeader.class.getName());

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
        L.log(Level.FINEST, "unknown1 = {0}", unknown1);
        
        fileVersion = in.readByte();
        L.log(Level.FINEST, "fileVersion = {0}", fileVersion);
        
        version = in.readStringNull(255);
        L.log(Level.FINEST, "version = {0}", version);
        
        revision = in.readStringNull(255);
        L.log(Level.FINEST, "revision = {0}", revision);
        
        fileSize = in.readInt();
        L.log(Level.FINEST, "fileSize = {0}", fileSize);
        
        dataOffset = in.readInt();
        L.log(Level.FINEST, "dataOffset = {0}", dataOffset);
        
        flag1 = in.readInt();
        L.log(Level.FINEST, "flag1 = {0}", flag1);
        
        flag2 = in.readInt();
        L.log(Level.FINEST, "flag2 = {0}", flag2);
        
        dataSizeCompressed = in.readInt();
        L.log(Level.FINEST, "dataSizeCompressed = {0}", dataSizeCompressed);
        
        dataSizeUncompressed = in.readInt();
        L.log(Level.FINEST, "dataSizeUncompressed = {0}", dataSizeUncompressed);
        
        fileSize2 = in.readInt();
        L.log(Level.FINEST, "fileSize2 = {0}", fileSize2);
        
        unknown2 = in.readInt();
        L.log(Level.FINEST, "unknown2 = {0}", unknown2);
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
