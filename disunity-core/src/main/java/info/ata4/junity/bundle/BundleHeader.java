/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.Struct;
import info.ata4.junity.UnityVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Structure for Unity asset bundles.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity UnityWebStreamHeader
 */
public class BundleHeader implements Struct {

    public static final String SIGNATURE_WEB = "UnityWeb";
    public static final String SIGNATURE_RAW = "UnityRaw";
    public static final String SIGNATURE_FS  = "UnityFS";

    // UnityWeb or UnityRaw
    private String signature;

    // file version
    // 6 in Unity 5.3+ (UnityFS files)
    // 3 in Unity 3.5 and 4
    // 2 in Unity 2.6 to 3.4
    // 1 in Unity 1 to 2.5
    private int streamVersion;

    // player version string
    // 2.x.x for Unity 2
    // 3.x.x for Unity 3/4
    // 5.x.x for Unity 5
    private UnityVersion unityVersion = new UnityVersion();

    // engine version string
    private UnityVersion unityRevision = new UnityVersion();

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

    // (UnityFS) length of the possibly-compressed (LZMA, LZ4) bundle data header
    private int compressedDataHeaderSize;

    // (UnityFS) flags
    //  0x100 = <unknown>
    //   0x80 = data header at end of file
    //   0x40 = entry info present
    //   0x3f = low six bits are data header compression method
    //             0 = none
    //             1 = LZMA
    //             3 = LZ4
    private int flags;

    @Override
    public void read(DataReader in) throws IOException {
        signature = in.readStringNull();
        streamVersion = in.readInt();
        unityVersion = new UnityVersion(in.readStringNull());
        unityRevision = new UnityVersion(in.readStringNull());

        if (signature.equals(SIGNATURE_FS)) {
            // FS signature
            // Expect streamVersion == 6
            completeFileSize = in.readLong();
            compressedDataHeaderSize = in.readInt();
            dataHeaderSize = in.readInt();
            flags = in.readInt();

            headerSize = (int) in.position();

            if ((flags & 0x80) == 0) {
                // The data header is part of the bundle header
                headerSize += compressedDataHeaderSize;
            }
            // else it's at the end of the file
        } else {
            // Web or Raw signature
            minimumStreamedBytes = in.readUnsignedInt();
            headerSize = in.readInt();

            numberOfLevelsToDownload = in.readInt();
            int numberOfLevels = in.readInt();

            levelByteEnd.clear();
            for (int i = 0; i < numberOfLevels; i++) {
                levelByteEnd.add(new ImmutablePair(in.readUnsignedInt(), in.readUnsignedInt()));
            }

            if (streamVersion >= 2) {
                completeFileSize = in.readUnsignedInt();
            }

            if (streamVersion >= 3) {
                dataHeaderSize = in.readUnsignedInt();
            }

            in.readByte();
        }
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
        return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW)
                || signature.equals(SIGNATURE_FS);
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
        this.unityVersion = Objects.requireNonNull(version);
    }

    public UnityVersion unityRevision() {
        return unityRevision;
    }

    public void unityRevision(UnityVersion revision) {
        this.unityRevision = Objects.requireNonNull(revision);
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

    public int compressedDataHeaderSize() { return compressedDataHeaderSize; }

    public int dataHeaderCompressionScheme() { return (flags & 0x3f); }

    public boolean dataHeaderAtEndOfFile() { return (flags & 0x80) != 0; }

    public boolean entryInfoPresent() { return (signature.equals(SIGNATURE_FS)) ? ((flags & 0x40) != 0) : true; }
}
