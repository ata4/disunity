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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleHeader implements Struct {
    
    public static final String SIGNATURE_WEB = "UnityWeb";
    public static final String SIGNATURE_RAW = "UnityRaw";
    
    // UnityWeb or UnityRaw
    public String signature;
    
    // file version
    // 3 in Unity 3.5 and 4
    // 2 in Unity 2.6 to 3.4
    // 1 in Unity 1 to 2.6
    public byte fileVersion;
    
    // engine version string
    // 2.x.x in Unity 2
    // 3.x.x in Unity 3/4
    public String version;
    
    // full engine version string
    public String revision;
    
    // size of the whole file, most of the time
    public int fileSize;
    
    // offset to the bundle data or size of the bundle header
    public int dataOffset;
    
    // equal to assets2 or 1
    public int assets1;
    
    // number of asset files?
    public int assets2;
    
    @Override
    public void read(DataInputReader in) throws IOException {
        signature = in.readStringFixed(8);
        
        // padding bytes presumably
        int dummy = in.readInt();
        assert dummy == 0;
        
        fileVersion = in.readByte();
        version = in.readStringNull(255);
        revision = in.readStringNull(255);
        fileSize = in.readInt();
        dataOffset = in.readInt();
        
        assets1 = in.readInt();
        assets2 = in.readInt();
        
        assert assets1 == assets2 || assets1 == 1;
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean hasValidSignature() {
        return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW);
    }
    
    public boolean isCompressed() {
        return signature.equals(SIGNATURE_WEB);
    }
}
