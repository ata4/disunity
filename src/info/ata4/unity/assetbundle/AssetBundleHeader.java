/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

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
 * Structure for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity UnityWebStreamHeader
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
    private int streamVersion;
    
    // player version string
    // 2.x.x for Unity 2
    // 3.x.x for Unity 3/4
    private UnityVersion unityVersion;
    
    // engine version string
    private UnityVersion unityRevision;
    
    // minimum number of bytes to read for streamed bundles,
    // equal to completeFileSize for normal bundles
    private int minimumStreamedBytes;
    
    // offset to the bundle data or size of the bundle header
    private int headerSize;
    
    // equal to 1 if it's a streamed bundle, number of levelX + mainData assets
    // otherwise
    private int numberOfLevelsToDownload;
    
    // list of compressed and uncompressed offsets
    private List<Pair<Integer, Integer>> levelByteEnd = new ArrayList<>();
    
    // equal to file size, sometimes equal to uncompressed data size without the header
    private int completeFileSize;
    
    // offset to the first asset file within the data area? equals compressed
    // file size if completeFileSize contains the uncompressed data size
    private int unknown;
    
    @Override
    public void read(DataInputReader in) throws IOException {
        signature = in.readStringNull();
        streamVersion = in.readInt();
        unityVersion = new UnityVersion(in.readStringNull());
        unityRevision = new UnityVersion(in.readStringNull());
        minimumStreamedBytes = in.readInt();
        headerSize = in.readInt();
        
        numberOfLevelsToDownload = in.readInt();
        int numberOfLevels = in.readInt();
        
        assert numberOfLevelsToDownload == numberOfLevels || numberOfLevelsToDownload == 1;
        
        for (int i = 0; i < numberOfLevels; i++) {
            levelByteEnd.add(new ImmutablePair(in.readInt(), in.readInt()));
        }
        
        if (streamVersion >= 2) {
            completeFileSize = in.readInt();
            assert minimumStreamedBytes <= completeFileSize;
        }
        
        if (streamVersion >= 3) {
            unknown = in.readInt();
        }
        
        in.readByte();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeStringNull(signature);
        out.writeInt(streamVersion);
        out.writeStringNull(unityVersion.toString());
        out.writeStringNull(unityRevision.toString());
        out.writeInt(minimumStreamedBytes);
        out.writeInt(headerSize);
        
        out.writeInt(numberOfLevelsToDownload);
        out.writeInt(levelByteEnd.size());
        
        for (Pair<Integer, Integer> offset : levelByteEnd) {
            out.writeInt(offset.getLeft());
            out.writeInt(offset.getRight());
        }
        
        if (streamVersion >= 2) {
            out.writeInt(completeFileSize);
        }
        
        if (streamVersion >= 3) {
            out.writeInt(unknown);
        }
        
        out.writeByte(0);
    }
    
    public boolean hasValidSignature() {
        return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW);
    }
    
    public void setCompressed(boolean compressed) {
        signature = compressed ? SIGNATURE_WEB : SIGNATURE_RAW;
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
        return streamVersion;
    }

    public void setFormat(byte format) {
        this.streamVersion = format;
    }

    public UnityVersion getUnityVersion() {
        return unityVersion;
    }

    public void setUnityVersion(UnityVersion version) {
        this.unityVersion = version;
    }

    public UnityVersion getUnityRevision() {
        return unityRevision;
    }

    public void setUnityRevision(UnityVersion revision) {
        this.unityRevision = revision;
    }

    public int getDataOffset() {
        return headerSize;
    }

    public void setDataOffset(int dataOffset) {
        this.headerSize = dataOffset;
    }
    
    public List<Pair<Integer, Integer>> getLevelOffsets() {
        return levelByteEnd;
    }

    public int getNumberOfLevels() {
        return numberOfLevelsToDownload;
    }

    public void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevelsToDownload = numberOfLevels;
    }

    public int getCompleteFileSize() {
        return completeFileSize;
    }

    public void setCompleteFileSize(int completeFileSize) {
        this.completeFileSize = completeFileSize;
    }

    public int getMinimumStreamedBytes() {
        return minimumStreamedBytes;
    }

    public void setMinimumStreamedBytes(int minimumStreamedBytes) {
        this.minimumStreamedBytes = minimumStreamedBytes;
    }
}
