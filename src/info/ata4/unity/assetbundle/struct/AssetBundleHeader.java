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
import info.ata4.util.collection.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleHeader implements Struct {
    
    public static final String SIGNATURE_WEB = "UnityWeb";
    public static final String SIGNATURE_RAW = "UnityRaw";
    
    // UnityWeb or UnityRaw
    private String signature = SIGNATURE_WEB;
    
    // file version
    // 3 in Unity 3.5 and 4
    // 2 in Unity 2.6 to 3.4
    // 1 in Unity 1 to 2.5
    private int format;
    
    // player version string
    // 2.x.x for Unity 2
    // 3.x.x for Unity 3/4
    private UnityVersion versionPlayer;
    
    // engine version string
    private UnityVersion versionEngine;
    
    // always equal to file size?
    private int fileSize1;
    
    // offset to the bundle data
    private int dataOffset;
    
    // either 1 or number of chunks
    private int unknown1;
    
    // list of chunk sizes, paired as compressed and uncompressed
    private List<Pair<Integer, Integer>> chunkSizes = new ArrayList<>();
    
    // always equal to file size?
    private int fileSize2;
    
    // offset of the first asset file within the data area?
    private int unknown2;
    
    @Override
    public void read(DataInputReader in) throws IOException {
        signature = in.readStringNull();
        format = in.readInt();
        versionPlayer = new UnityVersion(in.readStringNull());
        versionEngine = new UnityVersion(in.readStringNull());
        fileSize1 = in.readInt();
        dataOffset = in.readInt();
        
        unknown1 = in.readInt();
        int chunks = in.readInt();
        
        assert unknown1 == chunks || unknown1 == 1;
        
        for (int i = 0; i < chunks; i++) {
            chunkSizes.add(new Pair(in.readInt(), in.readInt()));
        }
        
        if (format >= 2) {
            fileSize2 = in.readInt();
        }
        
        if (format >= 3) {
            unknown2 = in.readInt();
        }
        
        byte b = in.readByte();
        assert b == 0;
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeStringNull(signature);
        out.writeInt(format);
        out.writeStringNull(versionPlayer.toString());
        out.writeStringNull(versionEngine.toString());
        out.writeInt(fileSize1);
        out.writeInt(dataOffset);
        
        out.writeInt(unknown1);
        out.writeInt(chunkSizes.size());
        
        for (Pair<Integer, Integer> chunk : chunkSizes) {
            out.writeInt(chunk.getLeft());
            out.writeInt(chunk.getRight());
        }
        
        if (format >= 2) {
            out.writeInt(fileSize2);
        }
        
        if (format >= 3) {
            out.writeInt(unknown2);
        }
        
        out.writeByte(0);
    }
    
    public boolean hasValidSignature() {
        return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getFormat() {
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

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }
}
