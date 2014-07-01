/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset.bundle.struct;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import info.ata4.unity.util.UnityVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Header structure for Unity asset bundles.
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
    private int format;
    
    // player version string
    // 2.x.x for Unity 2
    // 3.x.x for Unity 3/4
    private UnityVersion versionPlayer;
    
    // engine version string
    private UnityVersion versionEngine;
    
    // equal to file size, except for some rare cases
    private int fileSize1;
    
    // offset to the bundle data or size of the bundle header
    private int dataOffset;
    
    // equal to 1 or number of levelX + mainData assets
    private int unknown1;
    
    // list of compressed and uncompressed offsets
    private List<Pair<Integer, Integer>> offsetMap = new ArrayList<>();
    
    // equal to file size, sometimes equal to uncompressed data size without the header
    private int fileSize2;
    
    // offset to the first asset file within the data area? equals compressed
    // file size if fileSize2 contains the uncompressed data size
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
        int assets = in.readInt();
        
        assert unknown1 == assets || unknown1 == 1;
        
        for (int i = 0; i < assets; i++) {
            offsetMap.add(new ImmutablePair(in.readInt(), in.readInt()));
        }
        
        if (format >= 2) {
            fileSize2 = in.readInt();
            assert fileSize1 <= fileSize2;
        }
        
        if (format >= 3) {
            unknown2 = in.readInt();
        }
        
        in.readByte();
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
        out.writeInt(offsetMap.size());
        
        for (Pair<Integer, Integer> offset : offsetMap) {
            out.writeInt(offset.getLeft());
            out.writeInt(offset.getRight());
        }
        
        if (format >= 2) {
            out.writeInt(fileSize2);
        }
        
        if (format >= 3) {
            out.writeInt(unknown2);
        }
        
        out.writeByte(0);
    }
    
    public int getSize() {
        int size = 0;
        size += signature.length() + 1;
        size += 4;
        size += versionPlayer.toString().length() + 1;
        size += versionEngine.toString().length() + 1;
        size += 16;
        size += offsetMap.size() * 8;
        
        if (format >= 2) {
            size += 4;
        }
        
        if (format >= 3) {
            size += 4;
        }
        
        size++;
        
        return size;
    }
    
    public boolean hasValidSignature() {
        return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW);
    }
    
    public boolean setCompressed(boolean compressed) {
        return signature.equals(compressed ? SIGNATURE_WEB : SIGNATURE_RAW);
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
    
    public List<Pair<Integer, Integer>> getOffsetMap() {
        return offsetMap;
    }

    public int getUnknown1() {
        return unknown1;
    }

    public void setUnknown1(int unknown1) {
        this.unknown1 = unknown1;
    }
    
    public int getUnknown2() {
        return unknown2;
    }

    public void setUnknown2(int unknown2) {
        this.unknown2 = unknown2;
    }

    public int getFileSize1() {
        return fileSize1;
    }

    public void setFileSize1(int fileSize1) {
        this.fileSize1 = fileSize1;
    }

    public int getFileSize2() {
        return fileSize2;
    }

    public void setFileSize2(int fileSize2) {
        this.fileSize2 = fileSize2;
    }
}
