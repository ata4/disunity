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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
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
    private long minimumStreamedBytes;
    
    // offset to the bundle data or size of the bundle header
    private int headerSize;
    
    // equal to 1 if it's a streamed bundle, number of levelX + mainData assets
    // otherwise
    private int numberOfLevelsToDownload;
    
    // list of compressed and uncompressed offsets
    private List<Pair<Long, Long>> levelByteEnd = new ArrayList<>();
    
    // equal to file size, sometimes equal to uncompressed data size without the header
    private long completeFileSize;
    
    // offset to the first asset file within the data area? equals compressed
    // file size if completeFileSize contains the uncompressed data size
    private long dataHeaderSize;
    
    @Override
    public void read(DataReader in) throws IOException {
        signature = in.readStringNull();
        streamVersion = in.readInt();
        unityVersion = new UnityVersion(in.readStringNull());
        unityRevision = new UnityVersion(in.readStringNull());
        minimumStreamedBytes = in.readUnsignedInt();
        headerSize = in.readInt();
        
        numberOfLevelsToDownload = in.readInt();
        int numberOfLevels = in.readInt();
        
        assert numberOfLevelsToDownload == numberOfLevels || numberOfLevelsToDownload == 1;
        
        for (int i = 0; i < numberOfLevels; i++) {
            levelByteEnd.add(new ImmutablePair(in.readUnsignedInt(), in.readUnsignedInt()));
        }
        
        if (streamVersion >= 2) {
            completeFileSize = in.readUnsignedInt();
            assert minimumStreamedBytes <= completeFileSize;
        }
        
        if (streamVersion >= 3) {
            dataHeaderSize = in.readUnsignedInt();
        }
        
        in.readByte();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(signature);
        out.writeInt(streamVersion);
        out.writeStringNull(unityVersion.toString());
        out.writeStringNull(unityRevision.toString());
        out.writeUnsignedInt(minimumStreamedBytes);
        out.writeInt(headerSize);
        
        out.writeInt(numberOfLevelsToDownload);
        out.writeInt(levelByteEnd.size());
        
        for (Pair<Long, Long> offset : levelByteEnd) {
            out.writeUnsignedInt(offset.getLeft());
            out.writeUnsignedInt(offset.getRight());
        }
        
        if (streamVersion >= 2) {
            out.writeUnsignedInt(completeFileSize);
        }
        
        if (streamVersion >= 3) {
            out.writeUnsignedInt(dataHeaderSize);
        }
        
        out.writeUnsignedByte(0);
    }
    
    public boolean hasValidSignature() {
        return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW);
    }
    
    public void compressed(boolean compressed) {
        signature = compressed ? SIGNATURE_WEB : SIGNATURE_RAW;
    }
    
    public boolean compressed() {
        return signature.equals(SIGNATURE_WEB);
    }

    public String signature() {
        return signature;
    }

    public void signature(String signature) {
        this.signature = signature;
    }

    public int streamVersion() {
        return streamVersion;
    }

    public void streamVersion(int format) {
        this.streamVersion = format;
    }

    public UnityVersion unityVersion() {
        return unityVersion;
    }

    public void unityVersion(UnityVersion version) {
        this.unityVersion = version;
    }

    public UnityVersion unityRevision() {
        return unityRevision;
    }

    public void unityRevision(UnityVersion revision) {
        this.unityRevision = revision;
    }

    public int headerSize() {
        return headerSize;
    }

    public void headerSize(int dataOffset) {
        this.headerSize = dataOffset;
    }
    
    public List<Pair<Long, Long>> levelByteEnd() {
        return levelByteEnd;
    }
    
    public int numberOfLevels() {
        return levelByteEnd.size();
    }

    public int numberOfLevelsToDownload() {
        return numberOfLevelsToDownload;
    }

    public void numberOfLevelsToDownload(int numberOfLevelsToDownload) {
        this.numberOfLevelsToDownload = numberOfLevelsToDownload;
    }

    public long completeFileSize() {
        return completeFileSize;
    }

    public void completeFileSize(long completeFileSize) {
        this.completeFileSize = completeFileSize;
    }

    public long minimumStreamedBytes() {
        return minimumStreamedBytes;
    }

    public void minimumStreamedBytes(long minimumStreamedBytes) {
        this.minimumStreamedBytes = minimumStreamedBytes;
    }

    public long dataHeaderSize() {
        return dataHeaderSize;
    }

    public void dataHeaderSize(long dataHeaderSize) {
        this.dataHeaderSize = dataHeaderSize;
    }
}
