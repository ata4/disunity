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
import info.ata4.unity.util.UnityVersion;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleHeader implements Struct {
    
    public static final String SIGNATURE_WEB = "UnityWeb";
    public static final String SIGNATURE_RAW = "UnityRaw";
    
    // UnityWeb or UnityRaw
    private String signature;
    
    // file version
    // 3 in Unity 3.5 and 4
    // 2 in Unity 2.6 to 3.4
    // 1 in Unity 1 to 2.5
    private byte format;
    
    // player version string
    // 2.x.x for Unity 2
    // 3.x.x for Unity 3/4
    private UnityVersion versionPlayer;
    
    // engine version string
    private UnityVersion versionEngine;
    
    // size of the whole file, most of the time
    private int fileSize;
    
    // offset to the bundle data or size of the bundle header
    private int dataOffset;
    
    // equal to assets2 or 1
    private int assets1;
    
    // number of asset files?
    private int assets2;
    
    @Override
    public void read(DataInputReader in) throws IOException {
        signature = in.readStringFixed(8);
        
        // padding bytes presumably
        int dummy = in.readInt();
        assert dummy == 0;
        
        format = in.readByte();
        versionPlayer = new UnityVersion(in.readStringNull(255));
        versionEngine = new UnityVersion(in.readStringNull(255));
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public byte getFormat() {
        return format;
    }

    public void setFormat(byte format) {
        this.format = format;
    }

    public UnityVersion getPlayerVersion() {
        return versionPlayer;
    }

    public void setPlayerVersion(UnityVersion version) {
        this.versionPlayer = version;
    }

    public UnityVersion getEngineVersion() {
        return versionEngine;
    }

    public void setEngineVersion(UnityVersion revision) {
        this.versionEngine = revision;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public int getAssetCount1() {
        return assets1;
    }

    public void setAssetCount1(int assets1) {
        this.assets1 = assets1;
    }

    public int getAssetCount2() {
        return assets2;
    }

    public void setAssetCount2(int assets2) {
        this.assets2 = assets2;
    }
}
